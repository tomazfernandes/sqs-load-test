output "load_balancer_security_group_id" {
  value = aws_security_group.load_balancer_security_group.id
}

output "service_security_group_id" {
  value = aws_security_group.service_security_group.id
}
