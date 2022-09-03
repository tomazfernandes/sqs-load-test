resource "aws_ecr_repository" "sqs_load_test_ecr_repo" {
  name = "sqs-load-test-ecr-repo"
}

resource "aws_ecs_cluster" "sqs_load_test_cluster" {
  name = "sqs-load-test-cluster"
}

resource "aws_ecs_service" "sqs_load_test_service" {
  name = "sqs-load-test-service"
  cluster = aws_ecs_cluster.sqs_load_test_cluster.id
  task_definition = aws_ecs_task_definition.sqs_load_test_task.arn
  launch_type = "FARGATE"
  desired_count = 1

  network_configuration {
    subnets = [
      var.subnet_a,
      var.subnet_b,
      var.subnet_c
    ]
    assign_public_ip = true

    security_groups = [
      var.service_security_group_id
    ]
  }

  load_balancer {
    target_group_arn = var.target_group_arn
    container_name = aws_ecs_task_definition.sqs_load_test_task.family
    container_port = 8080
  }
}

resource "aws_ecs_task_definition" "sqs_load_test_task" {
  family                   = "sqs-load-test-task"
  container_definitions    = data.template_file.task_definition_template.rendered
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = var.task_cpu
  memory                   = var.task_memory
  execution_role_arn       = aws_iam_role.ecsTaskExecutionRole.arn
  task_role_arn            = aws_iam_role.ecsTaskRole.arn
}

data "template_file" "task_definition_template" {
  template = file("${path.module}/task-definitions/sqs-load-test-task-definition.json")
  vars = {
    ecr_image = var.ecr_image
    task_cpu = var.task_cpu
    task_memory = var.task_memory
  }
}

resource "aws_iam_role" "ecsTaskExecutionRole" {
  name               = "ecsTaskExecutionRole"
  assume_role_policy = data.aws_iam_policy_document.assume_role_policy.json
}

resource "aws_iam_role" "ecsTaskRole" {
  name               = "ecsTaskRole"
  assume_role_policy = data.aws_iam_policy_document.assume_role_policy.json
}

data "aws_iam_policy_document" "assume_role_policy" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role_policy_attachment" "ecsTaskExecutionRole_policy" {
  role       = aws_iam_role.ecsTaskExecutionRole.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_iam_role_policy_attachment" "ecsTaskExecutionRole_sqs_policy" {
  role       = aws_iam_role.ecsTaskRole.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSQSFullAccess"
}

resource "aws_cloudwatch_log_group" "sqs-load-test-log-group" {
  name = "sqs-load-test-log-group"
}