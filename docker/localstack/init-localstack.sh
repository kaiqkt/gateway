#!/bin/sh

echo "Creating SQS Queues"

awslocal sqs create-queue --queue-name communication-sqs-email
awslocal sqs create-queue --queue-name communication-sqs-sms
awslocal sqs create-queue --queue-name communication-sqs-push

echo "Init setup S3 bucket on LocalStack"

echo "Creating bucket on S3 with name communication-d-1"

awslocal s3 mb s3://communication-d-1/emails/
awslocal s3 mb s3://communication-d-1/sms/
awslocal s3 mb s3://communication-d-1/push/

echo "Copying files into the bucket"

awslocal s3 cp /etc/localstack/init/ready.d/redefine-password.html s3://communication-d-1/emails/
awslocal s3 cp /etc/localstack/init/ready.d/welcome.html s3://communication-d-1/emails/
awslocal s3 cp /etc/localstack/init/ready.d/password-updated.html s3://communication-d-1/emails/
awslocal s3 cp /etc/localstack/init/ready.d/new-access.html s3://communication-d-1/emails/
awslocal s3 cp /etc/localstack/init/ready.d/redefine-password.txt s3://communication-d-1/sms/
awslocal s3 cp /etc/localstack/init/ready.d/new-purchase.txt s3://communication-d-1/push/

echo "List file on bucket"

awslocal s3 ls s3://communication-d-1 --recursive --human-readable --summarize

echo "Finish setup S3 bucket on LocalStack"