package services

import dao.VehicleDAO
import models.{Vehicle, VehicleInfo}
import scala.util.Try

class VehicleService {
  private val vehicleDAO = new VehicleDAO()
  
  def createVehicle(userId: Int, make: String, model: String, year: Int, color: String, licensePlate: String, seats: Int): Try[Int] = {
    val vehicle = Vehicle(
      userId = userId,
      make = make,
      model = model,
      year = year,
      color = color,
      licensePlate = licensePlate,
      seats = seats
    )
    vehicleDAO.createVehicle(vehicle).map(_.id.get)
  }
  
  def findByUserId(userId: Int): Try[Option[VehicleInfo]] = {
    vehicleDAO.findByUserId(userId).map(_.map(vehicle => 
      VehicleInfo(
        id = vehicle.id.get,
        userId = vehicle.userId,
        make = vehicle.make,
        model = vehicle.model,
        year = vehicle.year,
        color = vehicle.color,
        licensePlate = vehicle.licensePlate,
        seats = vehicle.seats
      )
    ))
  }
  
  def updateVehicle(id: Int, make: String, model: String, year: Int, color: String, licensePlate: String, seats: Int): Try[Boolean] = {
    val vehicle = Vehicle(
      id = Some(id),
      userId = 0, // This will be ignored in update
      make = make,
      model = model,
      year = year,
      color = color,
      licensePlate = licensePlate,
      seats = seats
    )
    vehicleDAO.updateVehicle(vehicle)
  }
  
  def deleteVehicle(id: Int): Try[Boolean] = {
    vehicleDAO.deleteVehicle(id)
  }
  
  def deleteByUserId(userId: Int): Try[Boolean] = {
    vehicleDAO.deleteByUserId(userId)
  }
}
