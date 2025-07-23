package models

import java.time.LocalDateTime

case class User(
  id: Option[Int] = None,
  email: String,
  password: String,
  firstName: String,
  lastName: String,
  phone: String,
  createdAt: LocalDateTime = LocalDateTime.now()
)
