variable "task_cpu" {
  type = number
  default = 2048
}

variable "task_memory" {
  type = number
  default = 4096
}

variable "ecr_image" {
  type = string
}
