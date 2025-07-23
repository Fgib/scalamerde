package models

import java.time.LocalDateTime

case class Ride(
  id: Option[Int] = None,
  driverId: Int,
  departureCity: String,
  arrivalCity: String,
  departureTime: LocalDateTime,
  arrivalTime: LocalDateTime,
  availableSeats: Int,
  pricePerSeat: Double,
  description: Option[String] = None,
  createdAt: LocalDateTime = LocalDateTime.now()
)
