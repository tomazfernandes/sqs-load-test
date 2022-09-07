resource "aws_security_group" "service_security_group" {

  vpc_id      = var.default_vpc_id

  ingress {
    from_port = 8080
    to_port = 8080
    protocol = "tcp"
    security_groups = [
      aws_security_group.load_balancer_security_group.id
    ]
    cidr_blocks = ["0.0.0.0/0"]

  }

  egress {
    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "load_balancer_security_group" {

  vpc_id      = var.default_vpc_id

  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}