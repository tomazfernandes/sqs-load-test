output "default_subnet_a_id" {
  value = aws_default_subnet.default_subnet_a.id
}

output "default_subnet_b_id" {
  value = aws_default_subnet.default_subnet_b.id
}

output "default_subnet_c_id" {
  value = aws_default_subnet.default_subnet_c.id
}

output "default_vpc_id" {
  value = aws_default_vpc.default_vpc.id
}

output "default_vpc_cidr_block" {
  value = aws_default_vpc.default_vpc.cidr_block
}

output "default_vpc_ipv6_cidr_block" {
  value = aws_default_vpc.default_vpc.ipv6_cidr_block
}
