# main.tf - Provision EC2 + S3 on AWS
provider "aws" {
  region = "ap-south-1"  # Mumbai - good for India-based roles
}

resource "aws_instance" "web" {
  ami           = "ami-0f58b397bc5c1f2e8"
  instance_type = "t2.micro"

  tags = {
    Name = "devops-resume-project"
  }
}

resource "aws_s3_bucket" "app_bucket" {
  bucket = "my-devops-project-bucket"

  tags = {
    Environment = "dev"
  }
}
