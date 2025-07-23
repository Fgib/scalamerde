package dao

import menu.DBConnection
import models.Vehicle

import java.sql.{PreparedStatement, ResultSet, Statement}
import scala.util.Try

class VehicleDAO {
  
  def createVehicle(vehicle: Vehicle): Try[Vehicle] = Try {
    val connection = DBConnection.getConnection
    try {
      val sql = """
        INSERT INTO vehicles (user_id, make, model, year, color, license_plate, seats) 
        VALUES (?, ?, ?, ?, ?, ?, ?)
      """
      val statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
      statement.setInt(1, vehicle.userId)
      statement.setString(2, vehicle.make)
      statement.setString(3, vehicle.model)
      statement.setInt(4, vehicle.year)
      statement.setString(5, vehicle.color)
      statement.setString(6, vehicle.licensePlate)
      statement.setInt(7, vehicle.seats)
      
      statement.executeUpdate()
      val keys = statement.getGeneratedKeys
      keys.next()
      val id = keys.getInt(1)
      
      vehicle.copy(id = Some(id))
    } finally {
      connection.close()
    }
  }
  
  def findByUserId(userId: Int): Try[Option[Vehicle]] = Try {
    val connection = DBConnection.getConnection
    try {
      val sql = "SELECT * FROM vehicles WHERE user_id = ?"
      val statement = connection.prepareStatement(sql)
      statement.setInt(1, userId)
      
      val resultSet = statement.executeQuery()
      if (resultSet.next()) {
        Some(extractVehicleFromResultSet(resultSet))
      } else {
        None
      }
    } finally {
      connection.close()
    }
  }
  
  def updateVehicle(vehicle: Vehicle): Try[Boolean] = Try {
    val connection = DBConnection.getConnection
    try {
      val sql = """
        UPDATE vehicles 
        SET make = ?, model = ?, year = ?, color = ?, license_plate = ?, seats = ?
        WHERE id = ?
      """
      val statement = connection.prepareStatement(sql)
      statement.setString(1, vehicle.make)
      statement.setString(2, vehicle.model)
      statement.setInt(3, vehicle.year)
      statement.setString(4, vehicle.color)
      statement.setString(5, vehicle.licensePlate)
      statement.setInt(6, vehicle.seats)
      statement.setInt(7, vehicle.id.get)
      
      statement.executeUpdate() > 0
    } finally {
      connection.close()
    }
  }
  
  def deleteVehicle(id: Int): Try[Boolean] = Try {
    val connection = DBConnection.getConnection
    try {
      val sql = "DELETE FROM vehicles WHERE id = ?"
      val statement = connection.prepareStatement(sql)
      statement.setInt(1, id)
      
      statement.executeUpdate() > 0
    } finally {
      connection.close()
    }
  }
  
  def deleteByUserId(userId: Int): Try[Boolean] = Try {
    val connection = DBConnection.getConnection
    try {
      val sql = "DELETE FROM vehicles WHERE user_id = ?"
      val statement = connection.prepareStatement(sql)
      statement.setInt(1, userId)
      
      statement.executeUpdate() > 0
    } finally {
      connection.close()
    }
  }
  
  private def extractVehicleFromResultSet(rs: ResultSet): Vehicle = {
    Vehicle(
      id = Some(rs.getInt("id")),
      userId = rs.getInt("user_id"),
      make = rs.getString("make"),
      model = rs.getString("model"),
      year = rs.getInt("year"),
      color = rs.getString("color"),
      licensePlate = rs.getString("license_plate"),
      seats = rs.getInt("seats")
    )
  }
}
