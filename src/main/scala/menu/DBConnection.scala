package menu

import org.postgresql.ds.PGSimpleDataSource
import java.sql.Connection

object DBConnection {
  private val dataSource = {
    val ds = new PGSimpleDataSource()
    ds.setURL("jdbc:postgresql://84.46.246.209:5433/postgres")
    ds.setUser("postgres")
    ds.setPassword("k8SnF5swPiGP3gJxPsmqWh3Qrd4w2a10frFfPVcPYmH3mwKU5yzXImazWBYJoDGa")
    ds
  }
  
  def getConnection: Connection = dataSource.getConnection()
  
  def initializeDatabase(): Unit = {
    val connection = getConnection
    try {
      val statement = connection.createStatement()
      
      // Création de la table users
      statement.executeUpdate("""
        CREATE TABLE IF NOT EXISTS users (
          id SERIAL PRIMARY KEY,
          email VARCHAR(255) UNIQUE NOT NULL,
          password VARCHAR(255) NOT NULL,
          first_name VARCHAR(100) NOT NULL,
          last_name VARCHAR(100) NOT NULL,
          phone VARCHAR(20) NOT NULL,
          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
      """)
      
      // Création de la table vehicles
      statement.executeUpdate("""
        CREATE TABLE IF NOT EXISTS vehicles (
          id SERIAL PRIMARY KEY,
          user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
          make VARCHAR(50) NOT NULL,
          model VARCHAR(50) NOT NULL,
          year INTEGER NOT NULL,
          color VARCHAR(30) NOT NULL,
          license_plate VARCHAR(20) UNIQUE NOT NULL,
          seats INTEGER NOT NULL
        )
      """)
      
      // Création de la table rides
      statement.executeUpdate("""
        CREATE TABLE IF NOT EXISTS rides (
          id SERIAL PRIMARY KEY,
          driver_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
          departure_city VARCHAR(100) NOT NULL,
          arrival_city VARCHAR(100) NOT NULL,
          departure_time TIMESTAMP NOT NULL,
          arrival_time TIMESTAMP NOT NULL,
          available_seats INTEGER NOT NULL,
          price_per_seat DECIMAL(10,2) NOT NULL,
          description TEXT,
          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
      """)
      
      // Création de la table reservations
      statement.executeUpdate("""
        CREATE TABLE IF NOT EXISTS reservations (
          id SERIAL PRIMARY KEY,
          ride_id INTEGER REFERENCES rides(id) ON DELETE CASCADE,
          passenger_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
          seats_reserved INTEGER NOT NULL,
          status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
      """)
      
      statement.close()
      println("Base de données initialisée avec succès!")
      
    } catch {
      case e: Exception =>
        println(s"Erreur lors de l'initialisation de la base de données: ${e.getMessage}")
        throw e
    } finally {
      connection.close()
    }
  }
}
