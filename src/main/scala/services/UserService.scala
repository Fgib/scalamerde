package services

import dao.UserDAO
import models.{User, UserInfo}
import java.time.LocalDateTime
import scala.util.{Failure, Success, Try}

class UserService {
  private val userDAO = new UserDAO()
  
  def createUser(email: String, password: String, firstName: String, lastName: String, phone: String): Try[Int] = {
    val user = User(
      email = email,
      password = password,
      firstName = firstName,
      lastName = lastName,
      phone = phone,
      createdAt = LocalDateTime.now()
    )
    userDAO.createUser(user).map(_.id.get)
  }
  
  def findByEmailAndPassword(email: String, password: String): Try[Option[UserInfo]] = {
    userDAO.findByEmailAndPassword(email, password).map(_.map(user => 
      UserInfo(
        id = user.id.get,
        email = user.email,
        password = user.password,
        firstName = user.firstName,
        lastName = user.lastName,
        phone = user.phone,
        createdAt = user.createdAt
      )
    ))
  }
  
  def findById(id: Int): Try[Option[UserInfo]] = {
    userDAO.findById(id).map(_.map(user => 
      UserInfo(
        id = user.id.get,
        email = user.email,
        password = user.password,
        firstName = user.firstName,
        lastName = user.lastName,
        phone = user.phone,
        createdAt = user.createdAt
      )
    ))
  }
  
  def updateUser(id: Int, email: String, password: String, firstName: String, lastName: String, phone: String): Try[Boolean] = {
    val user = User(
      id = Some(id),
      email = email,
      password = password,
      firstName = firstName,
      lastName = lastName,
      phone = phone,
      createdAt = LocalDateTime.now() // This will be ignored in update
    )
    userDAO.updateUser(user)
  }
  
  def deleteUser(id: Int): Try[Boolean] = {
    userDAO.deleteUser(id)
  }
  
  def emailExists(email: String): Try[Boolean] = {
    userDAO.emailExists(email)
  }
}
