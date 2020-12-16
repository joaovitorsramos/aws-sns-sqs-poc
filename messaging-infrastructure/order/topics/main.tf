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


/*====
Create topics 
======*/
resource "aws_sns_topic" "orderCreated" {
  name = "${var.environment}-orderCreated"
  tags = {
    Environment = "${var.environment}-orderCreated-topic"
  }
}
