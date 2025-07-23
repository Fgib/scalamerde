package models

case class Vehicle(
  id: Option[Int] = None,
  userId: Int,
  make: String,
  model: String,
  year: Int,
  color: String,
  licensePlate: String,
  seats: Int
)
