package models

case class VehicleInfo(
  id: Int,
  userId: Int,
  make: String,
  model: String,
  year: Int,
  color: String,
  licensePlate: String,
  seats: Int
)
