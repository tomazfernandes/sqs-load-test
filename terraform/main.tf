provider "aws" {
  region  = "us-east-1"
  default_tags {
    tags = {
      Name         = "sqs-load-test-tag"
    }
  }
}

terraform {
  backend "s3" {
    bucket         = "sqs-load-test-tf-state"
    key            = "state/terraform.tfstate"
    region         = "us-east-1"
    kms_key_id     = "alias/terraform-bucket-key"
    encrypt        = true
    dynamodb_table = "terraform-state"
  }
}

module "load_balancer" {
  source = "./modules/load-balancer"
  subnet_a = module.network.default_subnet_a_id
  subnet_b = module.network.default_subnet_b_id
  subnet_c = module.network.default_subnet_c_id
  default_vpc_id = module.network.default_vpc_id
  lb_security_group_id = module.security_group.load_balancer_security_group_id
}

module "security_group" {
  source = "./modules/security-group"
}

module "network" {
  source = "./modules/network"
}

module "ecs" {
  source = "./modules/ecs"
  target_group_arn = module.load_balancer.aws_lb_target_group_arn
  subnet_a = module.network.default_subnet_a_id
  subnet_b = module.network.default_subnet_b_id
  subnet_c = module.network.default_subnet_c_id
  service_security_group_id = module.security_group.service_security_group_id
  task_cpu = var.task_cpu
  task_memory = var.task_memory
  ecr_image = var.ecr_image
}

module "state" {
  source = "./modules/state"
  state_bucket_name = "sqs-load-test-tf-state"
}