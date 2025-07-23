package models

import java.time.LocalDateTime

case class Reservation(
  id: Option[Int] = None,
  rideId: Int,
  passengerId: Int,
  seatsReserved: Int,
  status: ReservationStatus = ReservationStatus.ACTIVE,
  createdAt: LocalDateTime = LocalDateTime.now()
)

enum ReservationStatus:
  case ACTIVE, CANCELLED
