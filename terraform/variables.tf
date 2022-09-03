variable "task_cpu" {
  type = number
  default = 512
}

variable "task_memory" {
  type = number
  default = 1024
}

variable "ecr_image" {
  type = string
}