#!/bin/sh

echo "Creating SQS Queues"

awslocal sqs create-queue --queue-name communication-sqs-email
awslocal sqs create-queue --queue-name communication-sqs-sms

echo "Init setup S3 bucket on LocalStack"

echo "Creating bucket on S3 with name communication-d-1"

awslocal s3 mb s3://communication-d-1/emails/
awslocal s3 mb s3://communication-d-1/sms/

echo "Copying files into the bucket"

awslocal s3 cp /docker-entrypoint-initaws.d/redefine-password.html s3://communication-d-1/emails/
awslocal s3 cp /docker-entrypoint-initaws.d/welcome.html s3://communication-d-1/emails/
awslocal s3 cp /docker-entrypoint-initaws.d/password-updated.html s3://communication-d-1/emails/
awslocal s3 cp /docker-entrypoint-initaws.d/new-access.html s3://communication-d-1/emails/
awslocal s3 cp /docker-entrypoint-initaws.d/redefine-password.txt s3://communication-d-1/sms/

echo "List file on bucket"

awslocal s3 ls s3://communication-d-1 --recursive --human-readable --summarize

echo "Finish setup S3 bucket on LocalStack"