package dao

import menu.DBConnection
import models.{Reservation, ReservationStatus, Ride, User, VehicleInfo}

import java.sql.{PreparedStatement, ResultSet, Statement, Timestamp}
import java.time.LocalDateTime
import scala.collection.mutable.ListBuffer
import scala.util.Try

class ReservationDAO {
  
  def createReservation(reservation: Reservation): Try[Reservation] = Try {
    val connection = DBConnection.getConnection
    try {
      val sql = """
        INSERT INTO reservations (ride_id, passenger_id, seats_reserved, status, created_at) 
        VALUES (?, ?, ?, ?, ?)
      """
      val statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
      statement.setInt(1, reservation.rideId)
      statement.setInt(2, reservation.passengerId)
      statement.setInt(3, reservation.seatsReserved)
      statement.setString(4, reservation.status.toString)
      statement.setTimestamp(5, Timestamp.valueOf(reservation.createdAt))
      
      statement.executeUpdate()
      val keys = statement.getGeneratedKeys
      keys.next()
      val id = keys.getInt(1)
      
      reservation.copy(id = Some(id))
    } finally {
      connection.close()
    }
  }
  
  def findByPassengerIdWithRideDetails(passengerId: Int): Try[List[(Reservation, Ride, User)]] = Try {
    val connection = DBConnection.getConnection
    try {
      val sql = """
        SELECT res.id as res_id, res.ride_id as res_ride_id, res.passenger_id as res_passenger_id,
               res.seats_reserved as res_seats_reserved, res.status as res_status, res.created_at as res_created_at,
               r.id as r_id, r.driver_id as r_driver_id, r.departure_city as r_departure_city,
               r.arrival_city as r_arrival_city, r.departure_time as r_departure_time,
               r.arrival_time as r_arrival_time, r.available_seats as r_available_seats,
               r.price_per_seat as r_price_per_seat, r.description as r_description, r.created_at as r_created_at,
               r.vehicle_id as r_vehicle_id,
               u.first_name, u.last_name, u.phone,
               v.make as vehicle_make, v.model as vehicle_model, v.year as vehicle_year,
               v.color as vehicle_color, v.license_plate as vehicle_license_plate
        FROM reservations res
        JOIN rides r ON res.ride_id = r.id
        JOIN users u ON r.driver_id = u.id
        LEFT JOIN vehicles v ON r.vehicle_id = v.id
        WHERE res.passenger_id = ?
        ORDER BY r.departure_time DESC
      """
      val statement = connection.prepareStatement(sql)
      statement.setInt(1, passengerId)
      
      val resultSet = statement.executeQuery()
      val results = ListBuffer[(Reservation, Ride, User)]()
      
      while (resultSet.next()) {
        val reservation = extractReservationFromResultSet(resultSet, "res_")
        val ride = extractRideFromResultSet(resultSet, "r_")
        val driver = User(
          id = Some(ride.driverId),
          email = "",
          password = "",
          firstName = resultSet.getString("first_name"),
          lastName = resultSet.getString("last_name"),
          phone = resultSet.getString("phone"),
          createdAt = LocalDateTime.now()
        )
        results += ((reservation, ride, driver))
      }
      
      results.toList
    } finally {
      connection.close()
    }
  }
  
  def findActiveByPassengerId(passengerId: Int): Try[List[(Reservation, Ride, User)]] = Try {
    val connection = DBConnection.getConnection
    try {
      val sql = """
        SELECT res.id as res_id, res.ride_id as res_ride_id, res.passenger_id as res_passenger_id,
               res.seats_reserved as res_seats_reserved, res.status as res_status, res.created_at as res_created_at,
               r.id as r_id, r.driver_id as r_driver_id, r.departure_city as r_departure_city,
               r.arrival_city as r_arrival_city, r.departure_time as r_departure_time,
               r.arrival_time as r_arrival_time, r.available_seats as r_available_seats,
               r.price_per_seat as r_price_per_seat, r.description as r_description, r.created_at as r_created_at,
               u.first_name, u.last_name, u.phone
        FROM reservations res
        JOIN rides r ON res.ride_id = r.id
        JOIN users u ON r.driver_id = u.id
        WHERE res.passenger_id = ? AND res.status = 'ACTIVE'
        AND r.departure_time > CURRENT_TIMESTAMP
        ORDER BY r.departure_time
      """
      val statement = connection.prepareStatement(sql)
      statement.setInt(1, passengerId)
      
      val resultSet = statement.executeQuery()
      val results = ListBuffer[(Reservation, Ride, User)]()
      
      while (resultSet.next()) {
        val reservation = extractReservationFromResultSet(resultSet, "res_")
        val ride = extractRideFromResultSet(resultSet, "r_")
        val driver = User(
          id = Some(ride.driverId),
          email = "",
          password = "",
          firstName = resultSet.getString("first_name"),
          lastName = resultSet.getString("last_name"),
          phone = resultSet.getString("phone"),
          createdAt = LocalDateTime.now()
        )
        results += ((reservation, ride, driver))
      }
      
      results.toList
    } finally {
      connection.close()
    }
  }
  
  def findById(id: Int): Try[Option[Reservation]] = Try {
    val connection = DBConnection.getConnection
    try {
      val sql = "SELECT * FROM reservations WHERE id = ?"
      val statement = connection.prepareStatement(sql)
      statement.setInt(1, id)
      
      val resultSet = statement.executeQuery()
      if (resultSet.next()) {
        Some(extractReservationFromResultSet(resultSet))
      } else {
        None
      }
    } finally {
      connection.close()
    }
  }
  
  def cancelReservation(id: Int): Try[Boolean] = Try {
    val connection = DBConnection.getConnection
    try {
      val sql = "UPDATE reservations SET status = 'CANCELLED' WHERE id = ?"
      val statement = connection.prepareStatement(sql)
      statement.setInt(1, id)
      
      statement.executeUpdate() > 0
    } finally {
      connection.close()
    }
  }
  
  def findByRideId(rideId: Int): Try[List[Reservation]] = Try {
    val connection = DBConnection.getConnection
    try {
      val sql = "SELECT * FROM reservations WHERE ride_id = ?"
      val statement = connection.prepareStatement(sql)
      statement.setInt(1, rideId)
      
      val resultSet = statement.executeQuery()
      val reservations = ListBuffer[Reservation]()
      
      while (resultSet.next()) {
        reservations += extractReservationFromResultSet(resultSet)
      }
      
      reservations.toList
    } finally {
      connection.close()
    }
  }
  
  def getTotalReservedSeats(rideId: Int): Try[Int] = Try {
    val connection = DBConnection.getConnection
    try {
      val sql = """
        SELECT COALESCE(SUM(seats_reserved), 0) 
        FROM reservations 
        WHERE ride_id = ? AND status = 'ACTIVE'
      """
      val statement = connection.prepareStatement(sql)
      statement.setInt(1, rideId)
      
      val resultSet = statement.executeQuery()
      resultSet.next()
      resultSet.getInt(1)
    } finally {
      connection.close()
    }
  }
  
  def findByPassengerIdWithRideAndVehicleDetails(passengerId: Int): Try[List[(Reservation, Ride, User, Option[VehicleInfo])]] = Try {
    val connection = DBConnection.getConnection
    try {
      val sql = """
        SELECT res.id as res_id, res.ride_id as res_ride_id, res.passenger_id as res_passenger_id,
               res.seats_reserved as res_seats_reserved, res.status as res_status, res.created_at as res_created_at,
               r.id as r_id, r.driver_id as r_driver_id, r.departure_city as r_departure_city,
               r.arrival_city as r_arrival_city, r.departure_time as r_departure_time,
               r.arrival_time as r_arrival_time, r.available_seats as r_available_seats,
               r.price_per_seat as r_price_per_seat, r.description as r_description, r.created_at as r_created_at,
               r.vehicle_id as r_vehicle_id,
               u.first_name, u.last_name, u.phone,
               v.id as vehicle_id, v.make as vehicle_make, v.model as vehicle_model, v.year as vehicle_year,
               v.color as vehicle_color, v.license_plate as vehicle_license_plate, v.seats as vehicle_seats
        FROM reservations res
        JOIN rides r ON res.ride_id = r.id
        JOIN users u ON r.driver_id = u.id
        LEFT JOIN vehicles v ON r.vehicle_id = v.id
        WHERE res.passenger_id = ?
        ORDER BY r.departure_time DESC
      """
      val statement = connection.prepareStatement(sql)
      statement.setInt(1, passengerId)

      val resultSet = statement.executeQuery()
      val results = ListBuffer[(Reservation, Ride, User, Option[VehicleInfo])]()

      while (resultSet.next()) {
        val reservation = extractReservationFromResultSet(resultSet, "res_")
        val ride = extractRideFromResultSet(resultSet, "r_")
        val driver = User(
          id = Some(ride.driverId),
          email = "",
          password = "",
          firstName = resultSet.getString("first_name"),
          lastName = resultSet.getString("last_name"),
          phone = resultSet.getString("phone"),
          createdAt = LocalDateTime.now()
        )

        val vehicle = Option(resultSet.getString("vehicle_license_plate")).map { _ =>
          VehicleInfo(
            id = resultSet.getInt("vehicle_id"),
            userId = ride.driverId,
            make = resultSet.getString("vehicle_make"),
            model = resultSet.getString("vehicle_model"),
            year = resultSet.getInt("vehicle_year"),
            color = resultSet.getString("vehicle_color"),
            licensePlate = resultSet.getString("vehicle_license_plate"),
            seats = resultSet.getInt("vehicle_seats")
          )
        }

        results += ((reservation, ride, driver, vehicle))
      }

      results.toList
    } finally {
      connection.close()
    }
  }

  def findByRideIdWithPassengerDetails(rideId: Int): Try[List[(Reservation, User)]] = Try {
    val connection = DBConnection.getConnection
    try {
      val sql = """
        SELECT res.id as res_id, res.ride_id as res_ride_id, res.passenger_id as res_passenger_id, 
               res.seats_reserved as res_seats_reserved, res.status as res_status, res.created_at as res_created_at,
               u.id as user_id, u.email, u.password, u.first_name, u.last_name, u.phone, u.created_at as user_created_at
        FROM reservations res
        JOIN users u ON res.passenger_id = u.id
        WHERE res.ride_id = ? AND res.status = 'ACTIVE'
        ORDER BY res.created_at
      """
      val statement = connection.prepareStatement(sql)
      statement.setInt(1, rideId)
      
      val resultSet = statement.executeQuery()
      val results = ListBuffer[(Reservation, User)]()
      
      while (resultSet.next()) {
        val reservation = extractReservationFromResultSet(resultSet, "res_")
        val user = User(
          id = Some(resultSet.getInt("user_id")),
          email = resultSet.getString("email"),
          password = resultSet.getString("password"),
          firstName = resultSet.getString("first_name"),
          lastName = resultSet.getString("last_name"),
          phone = resultSet.getString("phone"),
          createdAt = resultSet.getTimestamp("user_created_at").toLocalDateTime
        )
        results += ((reservation, user))
      }
      
      results.toList
    } finally {
      connection.close()
    }
  }
  
  private def extractReservationFromResultSet(rs: ResultSet, prefix: String = ""): Reservation = {
    val idCol = if (prefix.nonEmpty) s"${prefix}id" else "id"
    val rideIdCol = if (prefix.nonEmpty) s"${prefix}ride_id" else "ride_id"
    val passengerIdCol = if (prefix.nonEmpty) s"${prefix}passenger_id" else "passenger_id"
    val seatsCol = if (prefix.nonEmpty) s"${prefix}seats_reserved" else "seats_reserved"
    val statusCol = if (prefix.nonEmpty) s"${prefix}status" else "status"
    val createdCol = if (prefix.nonEmpty) s"${prefix}created_at" else "created_at"

    Reservation(
      id = Some(rs.getInt(idCol)),
      rideId = rs.getInt(rideIdCol),
      passengerId = rs.getInt(passengerIdCol),
      seatsReserved = rs.getInt(seatsCol),
      status = ReservationStatus.valueOf(rs.getString(statusCol)),
      createdAt = rs.getTimestamp(createdCol).toLocalDateTime
    )
  }
  
  private def extractRideFromResultSet(rs: ResultSet, prefix: String = ""): Ride = {
    val idCol = if (prefix.nonEmpty) s"${prefix}id" else "id"
    val driverIdCol = if (prefix.nonEmpty) s"${prefix}driver_id" else "driver_id"
    val depCityCol = if (prefix.nonEmpty) s"${prefix}departure_city" else "departure_city"
    val arrCityCol = if (prefix.nonEmpty) s"${prefix}arrival_city" else "arrival_city"
    val depTimeCol = if (prefix.nonEmpty) s"${prefix}departure_time" else "departure_time"
    val arrTimeCol = if (prefix.nonEmpty) s"${prefix}arrival_time" else "arrival_time"
    val seatsCol = if (prefix.nonEmpty) s"${prefix}available_seats" else "available_seats"
    val priceCol = if (prefix.nonEmpty) s"${prefix}price_per_seat" else "price_per_seat"
    val descCol = if (prefix.nonEmpty) s"${prefix}description" else "description"
    val createdCol = if (prefix.nonEmpty) s"${prefix}created_at" else "created_at"

    Ride(
      id = Some(rs.getInt(idCol)),
      driverId = rs.getInt(driverIdCol),
      departureCity = rs.getString(depCityCol),
      arrivalCity = rs.getString(arrCityCol),
      departureTime = rs.getTimestamp(depTimeCol).toLocalDateTime,
      arrivalTime = rs.getTimestamp(arrTimeCol).toLocalDateTime,
      availableSeats = rs.getInt(seatsCol),
      pricePerSeat = rs.getDouble(priceCol),
      description = Option(rs.getString(descCol)),
      createdAt = rs.getTimestamp(createdCol).toLocalDateTime
    )
  }
}
