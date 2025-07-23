package dao

import menu.DBConnection
import models.{User, Vehicle}

import java.sql.{PreparedStatement, ResultSet, Statement, Timestamp}
import java.time.LocalDateTime
import scala.util.{Failure, Success, Try}

class UserDAO {
  
  def createUser(user: User): Try[User] = Try {
    val connection = DBConnection.getConnection
    try {
      val sql = """
        INSERT INTO users (email, password, first_name, last_name, phone, created_at) 
        VALUES (?, ?, ?, ?, ?, ?)
      """
      val statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
      statement.setString(1, user.email)
      statement.setString(2, user.password)
      statement.setString(3, user.firstName)
      statement.setString(4, user.lastName)
      statement.setString(5, user.phone)
      statement.setTimestamp(6, Timestamp.valueOf(user.createdAt))
      
      statement.executeUpdate()
      val keys = statement.getGeneratedKeys
      keys.next()
      val id = keys.getInt(1)
      
      user.copy(id = Some(id))
    } finally {
      connection.close()
    }
  }
  
  def findByEmailAndPassword(email: String, password: String): Try[Option[User]] = Try {
    val connection = DBConnection.getConnection
    try {
      val sql = "SELECT * FROM users WHERE email = ? AND password = ?"
      val statement = connection.prepareStatement(sql)
      statement.setString(1, email)
      statement.setString(2, password)
      
      val resultSet = statement.executeQuery()
      if (resultSet.next()) {
        Some(extractUserFromResultSet(resultSet))
      } else {
        None
      }
    } finally {
      connection.close()
    }
  }
  
  def findById(id: Int): Try[Option[User]] = Try {
    val connection = DBConnection.getConnection
    try {
      val sql = "SELECT * FROM users WHERE id = ?"
      val statement = connection.prepareStatement(sql)
      statement.setInt(1, id)
      
      val resultSet = statement.executeQuery()
      if (resultSet.next()) {
        Some(extractUserFromResultSet(resultSet))
      } else {
        None
      }
    } finally {
      connection.close()
    }
  }
  
  def updateUser(user: User): Try[Boolean] = Try {
    val connection = DBConnection.getConnection
    try {
      val sql = """
        UPDATE users 
        SET email = ?, password = ?, first_name = ?, last_name = ?, phone = ?
        WHERE id = ?
      """
      val statement = connection.prepareStatement(sql)
      statement.setString(1, user.email)
      statement.setString(2, user.password)
      statement.setString(3, user.firstName)
      statement.setString(4, user.lastName)
      statement.setString(5, user.phone)
      statement.setInt(6, user.id.get)
      
      statement.executeUpdate() > 0
    } finally {
      connection.close()
    }
  }
  
  def deleteUser(id: Int): Try[Boolean] = Try {
    val connection = DBConnection.getConnection
    try {
      val sql = "DELETE FROM users WHERE id = ?"
      val statement = connection.prepareStatement(sql)
      statement.setInt(1, id)
      
      statement.executeUpdate() > 0
    } finally {
      connection.close()
    }
  }
  
  def emailExists(email: String): Try[Boolean] = Try {
    val connection = DBConnection.getConnection
    try {
      val sql = "SELECT COUNT(*) FROM users WHERE email = ?"
      val statement = connection.prepareStatement(sql)
      statement.setString(1, email)
      
      val resultSet = statement.executeQuery()
      resultSet.next()
      resultSet.getInt(1) > 0
    } finally {
      connection.close()
    }
  }
  
  private def extractUserFromResultSet(rs: ResultSet): User = {
    User(
      id = Some(rs.getInt("id")),
      email = rs.getString("email"),
      password = rs.getString("password"),
      firstName = rs.getString("first_name"),
      lastName = rs.getString("last_name"),
      phone = rs.getString("phone"),
      createdAt = rs.getTimestamp("created_at").toLocalDateTime
    )
  }
}
