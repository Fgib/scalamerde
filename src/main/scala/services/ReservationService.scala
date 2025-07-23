package services

import dao.ReservationDAO
import models.{Reservation, ReservationStatus, Ride, User, UserInfo}
import java.time.LocalDateTime
import scala.util.Try

class ReservationService {
  private val reservationDAO = new ReservationDAO()
  
  def createReservation(rideId: Int, passengerId: Int, seatsReserved: Int, status: String): Try[Int] = {
    val reservation = Reservation(
      rideId = rideId,
      passengerId = passengerId,
      seatsReserved = seatsReserved,
      status = ReservationStatus.valueOf(status),
      createdAt = LocalDateTime.now()
    )
    reservationDAO.createReservation(reservation).map(_.id.get)
  }
  
  def findByPassengerIdWithRideDetails(passengerId: Int): Try[List[(Reservation, Ride, UserInfo)]] = {
    reservationDAO.findByPassengerIdWithRideDetails(passengerId).map(_.map { case (reservation, ride, user) =>
      val userInfo = UserInfo(
        id = user.id.get,
        email = user.email,
        password = user.password,
        firstName = user.firstName,
        lastName = user.lastName,
        phone = user.phone,
        createdAt = user.createdAt
      )
      (reservation, ride, userInfo)
    })
  }
  
  def findActiveByPassengerId(passengerId: Int): Try[List[(Reservation, Ride, UserInfo)]] = {
    reservationDAO.findActiveByPassengerId(passengerId).map(_.map { case (reservation, ride, user) =>
      val userInfo = UserInfo(
        id = user.id.get,
        email = user.email,
        password = user.password,
        firstName = user.firstName,
        lastName = user.lastName,
        phone = user.phone,
        createdAt = user.createdAt
      )
      (reservation, ride, userInfo)
    })
  }
  
  def findById(id: Int): Try[Option[Reservation]] = {
    reservationDAO.findById(id)
  }
  
  def cancelReservation(id: Int): Try[Boolean] = {
    reservationDAO.cancelReservation(id)
  }
  
  def findByRideId(rideId: Int): Try[List[Reservation]] = {
    reservationDAO.findByRideId(rideId)
  }
  
  def getTotalReservedSeats(rideId: Int): Try[Int] = {
    reservationDAO.getTotalReservedSeats(rideId)
  }
}
