variable "target_group_arn" {
  type = string
}

variable "subnet_a" {
  type = string
}

variable "subnet_b" {
  type = string
}

variable "subnet_c" {
  type = string
}

variable "service_security_group_id" {
  type = string
}

variable "task_cpu" {
  type = number
}

variable "task_memory" {
  type = number
}

variable "ecr_image" {
  type = string
}