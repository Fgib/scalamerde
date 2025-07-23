package dao

import menu.DBConnection
import models.{Ride, User}

import java.sql.{PreparedStatement, ResultSet, Statement, Timestamp}
import java.time.LocalDateTime
import scala.collection.mutable.ListBuffer
import scala.util.Try

class RideDAO {
  
  def createRide(ride: Ride): Try[Ride] = Try {
    val connection = DBConnection.getConnection
    try {
      val sql = """
        INSERT INTO rides (driver_id, departure_city, arrival_city, departure_time, 
                          arrival_time, available_seats, price_per_seat, description, created_at) 
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
      """
      val statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
      statement.setInt(1, ride.driverId)
      statement.setString(2, ride.departureCity)
      statement.setString(3, ride.arrivalCity)
      statement.setTimestamp(4, Timestamp.valueOf(ride.departureTime))
      statement.setTimestamp(5, Timestamp.valueOf(ride.arrivalTime))
      statement.setInt(6, ride.availableSeats)
      statement.setDouble(7, ride.pricePerSeat)
      statement.setString(8, ride.description.orNull)
      statement.setTimestamp(9, Timestamp.valueOf(ride.createdAt))
      
      statement.executeUpdate()
      val keys = statement.getGeneratedKeys
      keys.next()
      val id = keys.getInt(1)
      
      ride.copy(id = Some(id))
    } finally {
      connection.close()
    }
  }
  
  def findByDriverId(driverId: Int): Try[List[Ride]] = Try {
    val connection = DBConnection.getConnection
    try {
      val sql = "SELECT * FROM rides WHERE driver_id = ? ORDER BY departure_time"
      val statement = connection.prepareStatement(sql)
      statement.setInt(1, driverId)
      
      val resultSet = statement.executeQuery()
      val rides = ListBuffer[Ride]()
      
      while (resultSet.next()) {
        rides += extractRideFromResultSet(resultSet)
      }
      
      rides.toList
    } finally {
      connection.close()
    }
  }
  
  def findUpcomingByDriverId(driverId: Int): Try[List[Ride]] = Try {
    val connection = DBConnection.getConnection
    try {
      val sql = """
        SELECT * FROM rides 
        WHERE driver_id = ? AND departure_time > CURRENT_TIMESTAMP 
        ORDER BY departure_time
      """
      val statement = connection.prepareStatement(sql)
      statement.setInt(1, driverId)
      
      val resultSet = statement.executeQuery()
      val rides = ListBuffer[Ride]()
      
      while (resultSet.next()) {
        rides += extractRideFromResultSet(resultSet)
      }
      
      rides.toList
    } finally {
      connection.close()
    }
  }
  
  def findPastByDriverId(driverId: Int): Try[List[Ride]] = Try {
    val connection = DBConnection.getConnection
    try {
      val sql = """
        SELECT * FROM rides 
        WHERE driver_id = ? AND departure_time <= CURRENT_TIMESTAMP 
        ORDER BY departure_time DESC
      """
      val statement = connection.prepareStatement(sql)
      statement.setInt(1, driverId)
      
      val resultSet = statement.executeQuery()
      val rides = ListBuffer[Ride]()
      
      while (resultSet.next()) {
        rides += extractRideFromResultSet(resultSet)
      }
      
      rides.toList
    } finally {
      connection.close()
    }
  }
  
  def searchRides(departureCity: String, arrivalCity: String, departureDate: LocalDateTime): Try[List[(Ride, User)]] = Try {
    val connection = DBConnection.getConnection
    try {
      val sql = """
        SELECT r.*, u.first_name, u.last_name, u.phone 
        FROM rides r 
        JOIN users u ON r.driver_id = u.id
        WHERE LOWER(r.departure_city) LIKE LOWER(?) 
        AND LOWER(r.arrival_city) LIKE LOWER(?)
        AND DATE(r.departure_time) = DATE(?)
        AND r.available_seats > 0
        AND r.departure_time > CURRENT_TIMESTAMP
        ORDER BY r.departure_time
      """
      val statement = connection.prepareStatement(sql)
      statement.setString(1, s"%$departureCity%")
      statement.setString(2, s"%$arrivalCity%")
      statement.setTimestamp(3, Timestamp.valueOf(departureDate))
      
      val resultSet = statement.executeQuery()
      val results = ListBuffer[(Ride, User)]()
      
      while (resultSet.next()) {
        val ride = extractRideFromResultSet(resultSet)
        val driver = User(
          id = Some(ride.driverId),
          email = "", // On ne récupère pas l'email pour la recherche
          password = "",
          firstName = resultSet.getString("first_name"),
          lastName = resultSet.getString("last_name"),
          phone = resultSet.getString("phone"),
          createdAt = LocalDateTime.now()
        )
        results += ((ride, driver))
      }
      
      results.toList
    } finally {
      connection.close()
    }
  }
  
  def findById(id: Int): Try[Option[Ride]] = Try {
    val connection = DBConnection.getConnection
    try {
      val sql = "SELECT * FROM rides WHERE id = ?"
      val statement = connection.prepareStatement(sql)
      statement.setInt(1, id)
      
      val resultSet = statement.executeQuery()
      if (resultSet.next()) {
        Some(extractRideFromResultSet(resultSet))
      } else {
        None
      }
    } finally {
      connection.close()
    }
  }
  
  def updateAvailableSeats(rideId: Int, newAvailableSeats: Int): Try[Boolean] = Try {
    val connection = DBConnection.getConnection
    try {
      val sql = "UPDATE rides SET available_seats = ? WHERE id = ?"
      val statement = connection.prepareStatement(sql)
      statement.setInt(1, newAvailableSeats)
      statement.setInt(2, rideId)
      
      statement.executeUpdate() > 0
    } finally {
      connection.close()
    }
  }
  
  def deleteRide(id: Int): Try[Boolean] = Try {
    val connection = DBConnection.getConnection
    try {
      val sql = "DELETE FROM rides WHERE id = ?"
      val statement = connection.prepareStatement(sql)
      statement.setInt(1, id)
      
      statement.executeUpdate() > 0
    } finally {
      connection.close()
    }
  }
  
  def hasActiveReservations(rideId: Int): Try[Boolean] = Try {
    val connection = DBConnection.getConnection
    try {
      val sql = "SELECT COUNT(*) FROM reservations WHERE ride_id = ? AND status = 'ACTIVE'"
      val statement = connection.prepareStatement(sql)
      statement.setInt(1, rideId)
      
      val resultSet = statement.executeQuery()
      resultSet.next()
      resultSet.getInt(1) > 0
    } finally {
      connection.close()
    }
  }
  
  private def extractRideFromResultSet(rs: ResultSet): Ride = {
    Ride(
      id = Some(rs.getInt("id")),
      driverId = rs.getInt("driver_id"),
      departureCity = rs.getString("departure_city"),
      arrivalCity = rs.getString("arrival_city"),
      departureTime = rs.getTimestamp("departure_time").toLocalDateTime,
      arrivalTime = rs.getTimestamp("arrival_time").toLocalDateTime,
      availableSeats = rs.getInt("available_seats"),
      pricePerSeat = rs.getDouble("price_per_seat"),
      description = Option(rs.getString("description")),
      createdAt = rs.getTimestamp("created_at").toLocalDateTime
    )
  }
}
