package services

import dao.{RideDAO, UserDAO}
import models.{Ride, UserInfo}
import java.time.LocalDateTime
import scala.util.Try

class RideService {
  private val rideDAO = new RideDAO()
  private val userDAO = new UserDAO()
  
  def createRide(driverId: Int, departureCity: String, arrivalCity: String, departureTime: LocalDateTime, 
                arrivalTime: LocalDateTime, availableSeats: Int, pricePerSeat: Double, description: Option[String]): Try[Int] = {
    val ride = Ride(
      driverId = driverId,
      departureCity = departureCity,
      arrivalCity = arrivalCity,
      departureTime = departureTime,
      arrivalTime = arrivalTime,
      availableSeats = availableSeats,
      pricePerSeat = pricePerSeat,
      description = description,
      createdAt = LocalDateTime.now()
    )
    rideDAO.createRide(ride).map(_.id.get)
  }
  
  def findByDriverId(driverId: Int): Try[List[Ride]] = {
    rideDAO.findByDriverId(driverId)
  }
  
  def findUpcomingByDriverId(driverId: Int): Try[List[Ride]] = {
    rideDAO.findUpcomingByDriverId(driverId)
  }
  
  def findPastByDriverId(driverId: Int): Try[List[Ride]] = {
    rideDAO.findPastByDriverId(driverId)
  }
  
  def searchRides(departureCity: String, arrivalCity: String, departureDate: LocalDateTime): Try[List[(Ride, UserInfo)]] = {
    rideDAO.searchRides(departureCity, arrivalCity, departureDate).map(_.map { case (ride, user) =>
      val userInfo = UserInfo(
        id = user.id.get,
        email = user.email,
        password = user.password,
        firstName = user.firstName,
        lastName = user.lastName,
        phone = user.phone,
        createdAt = user.createdAt
      )
      (ride, userInfo)
    })
  }
  
  def findById(id: Int): Try[Option[Ride]] = {
    rideDAO.findById(id)
  }
  
  def updateAvailableSeats(rideId: Int, newAvailableSeats: Int): Try[Boolean] = {
    rideDAO.updateAvailableSeats(rideId, newAvailableSeats)
  }
  
  def deleteRide(id: Int): Try[Boolean] = {
    rideDAO.deleteRide(id)
  }
  
  def hasActiveReservations(rideId: Int): Try[Boolean] = {
    rideDAO.hasActiveReservations(rideId)
  }
}
