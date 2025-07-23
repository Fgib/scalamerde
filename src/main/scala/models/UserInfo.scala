package models

import java.time.LocalDateTime

case class UserInfo(
  id: Int,
  email: String,
  password: String,
  firstName: String,
  lastName: String,
  phone: String,
  createdAt: LocalDateTime
) {
  def fullName: String = s"$firstName $lastName"
}
