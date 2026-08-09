#!/bin/bash
awslocal s3 mb s3://bucket-site-teste
awslocal sqs create-queue --queue-name queue-site-teste