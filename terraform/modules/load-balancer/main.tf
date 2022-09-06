resource "aws_alb" "application_load_balancer" {
  name               = "sqs-load-test-lb"
  load_balancer_type = "application"
  subnets = [
    var.subnet_a,
    var.subnet_b,
    var.subnet_c
  ]
  internal = false

  security_groups = [
    var.lb_security_group_id
  ]
}

resource "aws_lb_target_group" "target_group" {
  name        = "target-group"
  port        = 8080
  protocol    = "HTTP"
  target_type = "ip"
  vpc_id      = var.default_vpc_id
  deregistration_delay = 30
  health_check {
    matcher = "200"
    path = "/sqs-load-test/actuator/health"
    interval = 40
    timeout = 30
    healthy_threshold = 2
    unhealthy_threshold = 3
  }
  depends_on = [
    aws_alb.application_load_balancer
  ]
}

resource "aws_lb_listener" "listener" {
  load_balancer_arn = aws_alb.application_load_balancer.arn
  port              = "8080"
  protocol          = "HTTP"
  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.target_group.arn
  }
  depends_on = [
    aws_lb_target_group.target_group
  ]
}