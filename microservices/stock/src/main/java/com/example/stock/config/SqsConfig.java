package com.example.stock.config;

import org.springframework.cloud.aws.messaging.config.annotation.EnableSqs;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;


@Configuration
@EnableSqs
public class SqsConfig {

	    @Bean
	    @Primary
	    public AmazonSQSAsync amazonSQSAsync() {
	        return AmazonSQSAsyncClientBuilder.standard()
	                .withCredentials(credentialsProvider())
	                .build();
	    }

	    @Bean
	    public AWSCredentialsProvider credentialsProvider() {
	        return new DefaultAWSCredentialsProviderChain();
	    }

	    @Bean
	    public QueueMessagingTemplate queueMessagingTemplate() {
	        return new QueueMessagingTemplate(amazonSQSAsync());
	    }  
}
