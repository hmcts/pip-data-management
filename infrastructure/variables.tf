## Defaults
variable "product" {
  default = "pip"
}
variable "component" {
  default = "sds"
}
variable "location" {
  default = "UK South"
}
variable "env" {}
variable "subscription" {
  default = ""
}
variable "deployment_namespace" {
  default = ""
}
variable "common_tags" {
  type = map(string)
}
variable "team_name" {
  default = "PIP DevOps"
}
variable "team_contact" {
  default = "#vh-devops"
}
