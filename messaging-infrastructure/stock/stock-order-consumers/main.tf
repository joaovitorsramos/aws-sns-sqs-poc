/*====
Variables used across all modules
======*/
terraform {
  required_providers {
    aws = "~> 2.68"
  }
}

provider "aws" {
  region  = var.region
}

data "aws_caller_identity" "current" {}

/*====
Query for existing topic
======*/
data "aws_sns_topic" "orderCreated" {
  name = "${var.environment}-orderCreated"
}

/*====
Create queues 
======*/

//------------------------------- Order Created Consumer --------------------------------------//
resource "aws_sqs_queue" "stockConsumerForOrderCreated" {
  depends_on       = [aws_sqs_queue.stockConsumerForOrderCreated_DLQ]
  name             = "${var.environment}-stockConsumerForOrderCreated"
  redrive_policy   = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.stockConsumerForOrderCreated_DLQ.arn
    maxReceiveCount     = 4
  })

  tags = {
    Environment = "${var.environment}-stockConsumerForOrderCreated-queue"
  }
}

resource "aws_sqs_queue" "stockConsumerForOrderCreated_DLQ" {
  name           = "${var.environment}-stockConsumerForOrderCreated_DLQ"
  
  tags = {
    Environment = "${var.environment}-stockConsumerForOrderCreated_DLQ-queue"
  }
}

resource "aws_sqs_queue_policy" "stockConsumerForOrderCreatedPolicy" {
  queue_url      = aws_sqs_queue.stockConsumerForOrderCreated.id
  depends_on     = [aws_sqs_queue.stockConsumerForOrderCreated]

  policy = <<POLICY
{
  "Version": "2012-10-17",
  "Id": "sqspolicy",
  "Statement": [
    {
      "Sid": "send-messages",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "sqs:SendMessage",
      "Resource": "${aws_sqs_queue.stockConsumerForOrderCreated.arn}",
      "Condition": {
        "ArnEquals": {
          "aws:SourceArn": "${data.aws_sns_topic.orderCreated.arn}"
        }
      }
    },
    {
      "Sid": "read-messages",
      "Effect": "Allow",
      "Principal": {
        "AWS": "arn:aws:iam::${data.aws_caller_identity.current.account_id}:root"
      },
      "Action": "SQS:*",
      "Resource": "${aws_sqs_queue.stockConsumerForOrderCreated.arn}"
    }
  ]
}
POLICY
}

resource "aws_sns_topic_subscription" "orderCreatedSqsTarget" {
  topic_arn = data.aws_sns_topic.orderCreated.arn
  protocol  = "sqs"
  endpoint  = aws_sqs_queue.stockConsumerForOrderCreated.arn
}

//-------------------------- End of Order Created Consumer --------------------------------------//