package menu

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.io.StdIn
import scala.util.{Failure, Success, Try}
import services._
import models.{UserInfo, VehicleInfo}

class MenuPrincipal {
  private val userService = new UserService()
  private val vehicleService = new VehicleService()
  private val rideService = new RideService()
  private val reservationService = new ReservationService()
  
  private var currentUser: Option[UserInfo] = None
  private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
  
  def start(): Unit = {
    println("=== Bienvenue sur la plateforme de co-voiturage ===")
    
    // Initialiser la base de données
    try {
      DBConnection.initializeDatabase()
    } catch {
      case e: Exception =>
        println(s"Erreur lors de l'initialisation: ${e.getMessage}")
        return
    }
    
    var running = true
    while (running) {
      currentUser match {
        case None => running = showLoginMenu()
        case Some(user) => running = showMainMenu(user)
      }
    }
    
    println("Au revoir !")
  }
  
  private def showLoginMenu(): Boolean = {
    println("\n=== Menu de connexion ===")
    println("1. Se connecter")
    println("2. S'inscrire")
    println("3. Quitter")
    print("Votre choix: ")
    
    StdIn.readLine() match {
      case "1" => login()
      case "2" => register()
      case "3" => false
      case _ => 
        println("Choix invalide!")
        true
    }
  }
  
  private def login(): Boolean = {
    println("\n=== Connexion ===")
    print("Email: ")
    val email = StdIn.readLine()
    print("Mot de passe: ")
    val password = StdIn.readLine()
    
    userService.findByEmailAndPassword(email, password) match {
      case Success(Some(user)) =>
        currentUser = Some(user)
        println(s"Bienvenue ${user.fullName}!")
        true
      case Success(None) =>
        println("Email ou mot de passe incorrect!")
        true
      case Failure(e) =>
        println(s"Erreur de connexion: ${e.getMessage}")
        true
    }
  }
  
  private def register(): Boolean = {
    println("\n=== Inscription ===")
    print("Email: ")
    val email = StdIn.readLine()
    
    // Vérifier si l'email existe déjà
    userService.emailExists(email) match {
      case Success(true) =>
        println("Cet email est déjà utilisé!")
        return true
      case Failure(e) =>
        println(s"Erreur: ${e.getMessage}")
        return true
      case _ =>
    }
    
    print("Mot de passe: ")
    val password = StdIn.readLine()
    print("Prénom: ")
    val firstName = StdIn.readLine()
    print("Nom: ")
    val lastName = StdIn.readLine()
    print("Téléphone: ")
    val phone = StdIn.readLine()
    
    userService.createUser(email, password, firstName, lastName, phone) match {
      case Success(userId) =>
        // Récupérer l'utilisateur créé
        userService.findById(userId) match {
          case Success(Some(user)) =>
            currentUser = Some(user)
            println(s"Inscription réussie! Bienvenue ${user.fullName}!")
            true
          case _ =>
            println("Erreur lors de la récupération de l'utilisateur créé")
            true
        }
      case Failure(e) =>
        println(s"Erreur lors de l'inscription: ${e.getMessage}")
        true
    }
  }
  
  private def showMainMenu(user: UserInfo): Boolean = {
    println(s"\n=== Menu principal - ${user.fullName} ===")
    println("1. Gestion du profil")
    println("2. Gestion des trajets")
    println("3. Gestion des réservations")
    println("4. Se déconnecter")
    println("5. Quitter")
    print("Votre choix: ")
    
    StdIn.readLine() match {
      case "1" => showProfileMenu(user)
      case "2" => showRideMenu(user)
      case "3" => showReservationMenu(user)
      case "4" => 
        currentUser = None
        println("Déconnexion réussie!")
        true
      case "5" => false
      case _ => 
        println("Choix invalide!")
        true
    }
  }
  
  private def showProfileMenu(user: UserInfo): Boolean = {
    println(s"\n=== Gestion du profil ===")
    println("1. Voir mes informations")
    println("2. Modifier mes informations")
    println("3. Gérer mon véhicule")
    println("4. Supprimer mon compte")
    println("5. Retour")
    print("Votre choix: ")
    
    StdIn.readLine() match {
      case "1" => showUserInfo(user)
      case "2" => updateUserInfo(user)
      case "3" => manageVehicle(user)
      case "4" => deleteAccount(user)
      case "5" => true
      case _ => 
        println("Choix invalide!")
        true
    }
  }
  
  private def showUserInfo(user: UserInfo): Boolean = {
    println(s"\n=== Mes informations ===")
    println(s"Email: ${user.email}")
    println(s"Nom: ${user.fullName}")
    println(s"Téléphone: ${user.phone}")
    println(s"Membre depuis: ${user.createdAt.format(dateFormatter)}")
    
    // Afficher le véhicule s'il existe
    vehicleService.findByUserId(user.id) match {
      case Success(Some(vehicle)) =>
        println(s"\nVéhicule: ${vehicle.make} ${vehicle.model} (${vehicle.year})")
        println(s"Couleur: ${vehicle.color}")
        println(s"Plaque: ${vehicle.licensePlate}")
        println(s"Nombre de places: ${vehicle.seats}")
      case Success(None) =>
        println("\nAucun véhicule enregistré")
      case Failure(e) =>
        println(s"Erreur lors de la récupération du véhicule: ${e.getMessage}")
    }
    
    true
  }
  
  private def updateUserInfo(user: UserInfo): Boolean = {
    println(s"\n=== Modifier mes informations ===")
    println("Laissez vide pour conserver la valeur actuelle")
    
    print(s"Email (${user.email}): ")
    val newEmail = StdIn.readLine() match {
      case "" => user.email
      case email => email
    }
    
    print("Nouveau mot de passe (laisser vide pour ne pas changer): ")
    val newPassword = StdIn.readLine() match {
      case "" => user.password
      case password => password
    }
    
    print(s"Prénom (${user.firstName}): ")
    val newFirstName = StdIn.readLine() match {
      case "" => user.firstName
      case name => name
    }
    
    print(s"Nom (${user.lastName}): ")
    val newLastName = StdIn.readLine() match {
      case "" => user.lastName
      case name => name
    }
    
    print(s"Téléphone (${user.phone}): ")
    val newPhone = StdIn.readLine() match {
      case "" => user.phone
      case phone => phone
    }
    
    userService.updateUser(user.id, newEmail, newPassword, newFirstName, newLastName, newPhone) match {
      case Success(true) =>
        val updatedUser = user.copy(
          email = newEmail,
          password = newPassword,
          firstName = newFirstName,
          lastName = newLastName,
          phone = newPhone
        )
        currentUser = Some(updatedUser)
        println("Informations mises à jour avec succès!")
      case Success(false) =>
        println("Aucune modification effectuée")
      case Failure(e) =>
        println(s"Erreur lors de la mise à jour: ${e.getMessage}")
    }
    
    true
  }
  
  private def manageVehicle(user: UserInfo): Boolean = {
    vehicleService.findByUserId(user.id) match {
      case Success(Some(vehicle)) =>
        println(s"\n=== Mon véhicule: ${vehicle.make} ${vehicle.model} ===")
        println("1. Voir les détails du véhicule")
        println("2. Modifier le véhicule")
        println("3. Supprimer le véhicule")
        println("4. Retour")
        print("Votre choix: ")
        
        StdIn.readLine() match {
          case "1" => showVehicleDetails(vehicle)
          case "2" => updateVehicle(vehicle)
          case "3" => deleteVehicle(vehicle)
          case "4" => true
          case _ =>
            println("Choix invalide!")
            true
        }
      case Success(None) =>
        println("\n=== Ajouter un véhicule ===")
        addVehicle(user)
      case Failure(e) =>
        println(s"Erreur: ${e.getMessage}")
        true
    }
  }
  
  private def addVehicle(user: UserInfo): Boolean = {
    print("Marque: ")
    val make = StdIn.readLine()
    print("Modèle: ")
    val model = StdIn.readLine()
    print("Année: ")
    val year = try { StdIn.readLine().toInt } catch { case _: NumberFormatException => 
      println("Année invalide!")
      return true
    }
    print("Couleur: ")
    val color = StdIn.readLine()
    print("Plaque d'immatriculation: ")
    val licensePlate = StdIn.readLine()
    print("Nombre de places: ")
    val seats = try { StdIn.readLine().toInt } catch { case _: NumberFormatException => 
      println("Nombre de places invalide!")
      return true
    }
    
    vehicleService.createVehicle(user.id, make, model, year, color, licensePlate, seats) match {
      case Success(_) =>
        println("Véhicule ajouté avec succès!")
      case Failure(e) =>
        println(s"Erreur lors de l'ajout du véhicule: ${e.getMessage}")
    }
    
    true
  }
  
  private def updateVehicle(vehicle: VehicleInfo): Boolean = {
    println(s"\n=== Modifier le véhicule ===")
    println("Laissez vide pour conserver la valeur actuelle")
    
    print(s"Marque (${vehicle.make}): ")
    val newMake = StdIn.readLine() match {
      case "" => vehicle.make
      case make => make
    }
    
    print(s"Modèle (${vehicle.model}): ")
    val newModel = StdIn.readLine() match {
      case "" => vehicle.model
      case model => model
    }
    
    print(s"Année (${vehicle.year}): ")
    val newYear = StdIn.readLine() match {
      case "" => vehicle.year
      case year => try { year.toInt } catch { case _: NumberFormatException => 
        println("Année invalide!")
        return true
      }
    }
    
    print(s"Couleur (${vehicle.color}): ")
    val newColor = StdIn.readLine() match {
      case "" => vehicle.color
      case color => color
    }
    
    print(s"Plaque (${vehicle.licensePlate}): ")
    val newLicensePlate = StdIn.readLine() match {
      case "" => vehicle.licensePlate
      case plate => plate
    }
    
    print(s"Nombre de places (${vehicle.seats}): ")
    val newSeats = StdIn.readLine() match {
      case "" => vehicle.seats
      case seats => try { seats.toInt } catch { case _: NumberFormatException => 
        println("Nombre de places invalide!")
        return true
      }
    }
    
    vehicleService.updateVehicle(vehicle.id, newMake, newModel, newYear, newColor, newLicensePlate, newSeats) match {
      case Success(true) =>
        println("Véhicule mis à jour avec succès!")
      case Success(false) =>
        println("Aucune modification effectuée")
      case Failure(e) =>
        println(s"Erreur lors de la mise à jour: ${e.getMessage}")
    }
    
    true
  }
  
  private def deleteVehicle(vehicle: VehicleInfo): Boolean = {
    print("Êtes-vous sûr de vouloir supprimer ce véhicule? (oui/non): ")
    if (StdIn.readLine().toLowerCase != "oui") {
      println("Suppression annulée")
      return true
    }
    
    vehicleService.deleteVehicle(vehicle.id) match {
      case Success(true) =>
        println("Véhicule supprimé avec succès!")
      case Success(false) =>
        println("Véhicule non trouvé")
      case Failure(e) =>
        println(s"Erreur lors de la suppression: ${e.getMessage}")
    }
    
    true
  }
  
  private def deleteAccount(user: UserInfo): Boolean = {
    print("Êtes-vous sûr de vouloir supprimer votre compte? Cette action est irréversible! (oui/non): ")
    if (StdIn.readLine().toLowerCase != "oui") {
      println("Suppression annulée")
      return true
    }
    
    userService.deleteUser(user.id) match {
      case Success(true) =>
        println("Compte supprimé avec succès!")
        currentUser = None
      case Success(false) =>
        println("Compte non trouvé")
      case Failure(e) =>
        println(s"Erreur lors de la suppression: ${e.getMessage}")
    }
    
    true
  }
  
  private def showRideMenu(user: UserInfo): Boolean = {
    println(s"\n=== Gestion des trajets ===")
    println("1. Proposer un trajet")
    println("2. Mes trajets à venir")
    println("3. Mes trajets passés")
    println("4. Supprimer un trajet")
    println("5. Retour")
    print("Votre choix: ")
    
    StdIn.readLine() match {
      case "1" => createRide(user)
      case "2" => showUpcomingRides(user)
      case "3" => showPastRides(user)
      case "4" => deleteRide(user)
      case "5" => true
      case _ => 
        println("Choix invalide!")
        true
    }
  }
  
  private def createRide(user: UserInfo): Boolean = {
    // Vérifier si l'utilisateur a un véhicule
    vehicleService.findByUserId(user.id) match {
      case Success(None) =>
        println("Vous devez d'abord enregistrer un véhicule pour proposer un trajet!")
        return true
      case Failure(e) =>
        println(s"Erreur: ${e.getMessage}")
        return true
      case Success(Some(vehicle)) =>
        println(s"\n=== Proposer un trajet avec ${vehicle.make} ${vehicle.model} ===")
    }
    
    print("Ville de départ: ")
    val departureCity = StdIn.readLine()
    print("Ville d'arrivée: ")
    val arrivalCity = StdIn.readLine()
    
    print("Date et heure de départ (format: dd/MM/yyyy HH:mm): ")
    val departureTime = try {
      LocalDateTime.parse(StdIn.readLine(), dateFormatter)
    } catch {
      case _: Exception =>
        println("Format de date invalide!")
        return true
    }
    
    if (departureTime.isBefore(LocalDateTime.now())) {
      println("La date de départ ne peut pas être dans le passé!")
      return true
    }
    
    print("Date et heure d'arrivée (format: dd/MM/yyyy HH:mm): ")
    val arrivalTime = try {
      LocalDateTime.parse(StdIn.readLine(), dateFormatter)
    } catch {
      case _: Exception =>
        println("Format de date invalide!")
        return true
    }
    
    if (arrivalTime.isBefore(departureTime)) {
      println("L'heure d'arrivée ne peut pas être avant l'heure de départ!")
      return true
    }
    
    print("Nombre de places disponibles: ")
    val availableSeats = try { 
      val seats = StdIn.readLine().toInt
      if (seats <= 0) {
        println("Le nombre de places doit être positif!")
        return true
      }
      seats
    } catch { 
      case _: NumberFormatException => 
        println("Nombre de places invalide!")
        return true
    }
    
    print("Prix par place (€): ")
    val pricePerSeat = try { 
      val price = StdIn.readLine().toDouble
      if (price < 0) {
        println("Le prix ne peut pas être négatif!")
        return true
      }
      price
    } catch { 
      case _: NumberFormatException => 
        println("Prix invalide!")
        return true
    }
    
    print("Description (optionnel): ")
    val description = StdIn.readLine() match {
      case "" => None
      case desc => Some(desc)
    }
    
    rideService.createRide(user.id, departureCity, arrivalCity, departureTime, arrivalTime, availableSeats, pricePerSeat, description) match {
      case Success(_) =>
        println("Trajet créé avec succès!")
      case Failure(e) =>
        println(s"Erreur lors de la création du trajet: ${e.getMessage}")
    }
    
    true
  }
  
  private def showUpcomingRides(user: UserInfo): Boolean = {
    println(s"\n=== Mes trajets à venir ===")
    
    rideService.findUpcomingByDriverId(user.id) match {
      case Success(rides) =>
        if (rides.isEmpty) {
          println("Aucun trajet à venir")
        } else {
          rides.zipWithIndex.foreach { case (ride, index) =>
            println(s"\n${index + 1}. ${ride.departureCity} → ${ride.arrivalCity}")
            println(s"   Départ: ${ride.departureTime.format(dateFormatter)}")
            println(s"   Arrivée: ${ride.arrivalTime.format(dateFormatter)}")
            println(s"   Places disponibles: ${ride.availableSeats}")
            println(s"   Prix: ${ride.pricePerSeat}€")
            ride.description.foreach(desc => println(s"   Description: $desc"))
          }
        }
      case Failure(e) =>
        println(s"Erreur: ${e.getMessage}")
    }
    
    true
  }
  
  private def showPastRides(user: UserInfo): Boolean = {
    println(s"\n=== Mes trajets passés ===")
    
    rideService.findPastByDriverId(user.id) match {
      case Success(rides) =>
        if (rides.isEmpty) {
          println("Aucun trajet passé")
        } else {
          rides.zipWithIndex.foreach { case (ride, index) =>
            println(s"\n${index + 1}. ${ride.departureCity} → ${ride.arrivalCity}")
            println(s"   Départ: ${ride.departureTime.format(dateFormatter)}")
            println(s"   Arrivée: ${ride.arrivalTime.format(dateFormatter)}")
            println(s"   Places disponibles: ${ride.availableSeats}")
            println(s"   Prix: ${ride.pricePerSeat}€")
            ride.description.foreach(desc => println(s"   Description: $desc"))
          }
        }
      case Failure(e) =>
        println(s"Erreur: ${e.getMessage}")
    }
    
    true
  }
  
  private def deleteRide(user: UserInfo): Boolean = {
    println(s"\n=== Supprimer un trajet ===")
    
    rideService.findUpcomingByDriverId(user.id) match {
      case Success(rides) =>
        if (rides.isEmpty) {
          println("Aucun trajet à venir à supprimer")
          return true
        }
        
        println("Vos trajets à venir:")
        rides.zipWithIndex.foreach { case (ride, index) =>
          println(s"${index + 1}. ${ride.departureCity} → ${ride.arrivalCity} (${ride.departureTime.format(dateFormatter)})")
        }
        
        print("Numéro du trajet à supprimer (0 pour annuler): ")
        val choice = try { StdIn.readLine().toInt } catch { case _: NumberFormatException => 0 }
        
        if (choice == 0 || choice > rides.length) {
          println("Suppression annulée")
          return true
        }
        
        val rideToDelete = rides(choice - 1)
        
        // Vérifier s'il y a des réservations actives
        rideService.hasActiveReservations(rideToDelete.id.get) match {
          case Success(true) =>
            println("Impossible de supprimer ce trajet car il y a des réservations actives!")
            return true
          case Success(false) =>
            rideService.deleteRide(rideToDelete.id.get) match {
              case Success(true) =>
                println("Trajet supprimé avec succès!")
              case Success(false) =>
                println("Trajet non trouvé")
              case Failure(e) =>
                println(s"Erreur lors de la suppression: ${e.getMessage}")
            }
          case Failure(e) =>
            println(s"Erreur: ${e.getMessage}")
        }
        
      case Failure(e) =>
        println(s"Erreur: ${e.getMessage}")
    }
    
    true
  }
  
  private def showReservationMenu(user: UserInfo): Boolean = {
    println(s"\n=== Gestion des réservations ===")
    println("1. Rechercher un trajet")
    println("2. Mes réservations actives")
    println("3. Historique des réservations")
    println("4. Annuler une réservation")
    println("5. Retour")
    print("Votre choix: ")
    
    StdIn.readLine() match {
      case "1" => searchAndBookRide(user)
      case "2" => showActiveReservations(user)
      case "3" => showReservationHistory(user)
      case "4" => cancelReservation(user)
      case "5" => true
      case _ => 
        println("Choix invalide!")
        true
    }
  }
  
  private def searchAndBookRide(user: UserInfo): Boolean = {
    println(s"\n=== Rechercher un trajet ===")
    
    print("Ville de départ: ")
    val departureCity = StdIn.readLine()
    print("Ville d'arrivée: ")
    val arrivalCity = StdIn.readLine()
    print("Date de départ (format: dd/MM/yyyy): ")
    
    val departureDate = try {
      LocalDateTime.parse(StdIn.readLine() + " 00:00", dateFormatter)
    } catch {
      case _: Exception =>
        println("Format de date invalide!")
        return true
    }
    
    rideService.searchRides(departureCity, arrivalCity, departureDate) match {
      case Success(results) =>
        if (results.isEmpty) {
          println("Aucun trajet trouvé pour ces critères")
          return true
        }
        
        println(s"\n${results.length} trajet(s) trouvé(s):")
        results.zipWithIndex.foreach { case ((ride, driver), index) =>
          println(s"\n${index + 1}. ${ride.departureCity} → ${ride.arrivalCity}")
          println(s"   Conducteur: ${driver.fullName} (${driver.phone})")
          println(s"   Départ: ${ride.departureTime.format(dateFormatter)}")
          println(s"   Arrivée: ${ride.arrivalTime.format(dateFormatter)}")
          println(s"   Places disponibles: ${ride.availableSeats}")
          println(s"   Prix: ${ride.pricePerSeat}€")
          ride.description.foreach(desc => println(s"   Description: $desc"))
        }
        
        print("\nNuméro du trajet à réserver (0 pour annuler): ")
        val choice = try { StdIn.readLine().toInt } catch { case _: NumberFormatException => 0 }
        
        if (choice == 0 || choice > results.length) {
          println("Réservation annulée")
          return true
        }
        
        val (selectedRide, driver) = results(choice - 1)
        
        // Vérifier que l'utilisateur n'est pas le conducteur
        if (selectedRide.driverId == user.id) {
          println("Vous ne pouvez pas réserver votre propre trajet!")
          return true
        }
        
        print(s"Nombre de places à réserver (max ${selectedRide.availableSeats}): ")
        val seatsToReserve = try { 
          val seats = StdIn.readLine().toInt
          if (seats <= 0 || seats > selectedRide.availableSeats) {
            println("Nombre de places invalide!")
            return true
          }
          seats
        } catch { 
          case _: NumberFormatException => 
            println("Nombre invalide!")
            return true
        }
        
        val totalPrice = seatsToReserve * selectedRide.pricePerSeat
        println(s"Prix total: ${totalPrice}€")
        print("Confirmer la réservation? (oui/non): ")
        
        if (StdIn.readLine().toLowerCase != "oui") {
          println("Réservation annulée")
          return true
        }
        
        reservationService.createReservation(selectedRide.id.get, user.id, seatsToReserve, "ACTIVE") match {
          case Success(_) =>
            // Mettre à jour le nombre de places disponibles
            val newAvailableSeats = selectedRide.availableSeats - seatsToReserve
            rideService.updateAvailableSeats(selectedRide.id.get, newAvailableSeats) match {
              case Success(_) =>
                println("Réservation effectuée avec succès!")
                println(s"Contactez le conducteur: ${driver.fullName} - ${driver.phone}")
              case Failure(e) =>
                println(s"Réservation créée mais erreur lors de la mise à jour des places: ${e.getMessage}")
            }
          case Failure(e) =>
            println(s"Erreur lors de la réservation: ${e.getMessage}")
        }
        
      case Failure(e) =>
        println(s"Erreur lors de la recherche: ${e.getMessage}")
    }
    
    true
  }
  
  private def showActiveReservations(user: UserInfo): Boolean = {
    println(s"\n=== Mes réservations actives ===")
    
    reservationService.findActiveByPassengerId(user.id) match {
      case Success(reservations) =>
        if (reservations.isEmpty) {
          println("Aucune réservation active")
        } else {
          reservations.zipWithIndex.foreach { case ((reservation, ride, driver), index) =>
            println(s"\n${index + 1}. ${ride.departureCity} → ${ride.arrivalCity}")
            println(s"   Conducteur: ${driver.fullName} (${driver.phone})")
            println(s"   Départ: ${ride.departureTime.format(dateFormatter)}")
            println(s"   Places réservées: ${reservation.seatsReserved}")
            println(s"   Prix total: ${reservation.seatsReserved * ride.pricePerSeat}€")
            println(s"   Réservé le: ${reservation.createdAt.format(dateFormatter)}")
          }
        }
      case Failure(e) =>
        println(s"Erreur: ${e.getMessage}")
    }
    
    true
  }
  
  private def showReservationHistory(user: UserInfo): Boolean = {
    println(s"\n=== Historique des réservations ===")
    
    reservationService.findByPassengerIdWithRideDetails(user.id) match {
      case Success(reservations) =>
        if (reservations.isEmpty) {
          println("Aucune réservation")
        } else {
          reservations.zipWithIndex.foreach { case ((reservation, ride, driver), index) =>
            println(s"\n${index + 1}. ${ride.departureCity} → ${ride.arrivalCity}")
            println(s"   Conducteur: ${driver.fullName}")
            println(s"   Départ: ${ride.departureTime.format(dateFormatter)}")
            println(s"   Places réservées: ${reservation.seatsReserved}")
            println(s"   Prix total: ${reservation.seatsReserved * ride.pricePerSeat}€")
            println(s"   Statut: ${reservation.status}")
            println(s"   Réservé le: ${reservation.createdAt.format(dateFormatter)}")
          }
        }
      case Failure(e) =>
        println(s"Erreur: ${e.getMessage}")
    }
    
    true
  }
  
  private def cancelReservation(user: UserInfo): Boolean = {
    println(s"\n=== Annuler une réservation ===")
    
    reservationService.findActiveByPassengerId(user.id) match {
      case Success(reservations) =>
        if (reservations.isEmpty) {
          println("Aucune réservation active à annuler")
          return true
        }
        
        println("Vos réservations actives:")
        reservations.zipWithIndex.foreach { case ((reservation, ride, driver), index) =>
          println(s"${index + 1}. ${ride.departureCity} → ${ride.arrivalCity} (${ride.departureTime.format(dateFormatter)})")
        }
        
        print("Numéro de la réservation à annuler (0 pour annuler): ")
        val choice = try { StdIn.readLine().toInt } catch { case _: NumberFormatException => 0 }
        
        if (choice == 0 || choice > reservations.length) {
          println("Annulation annulée")
          return true
        }
        
        val (reservationToCancel, ride, _) = reservations(choice - 1)
        
        print("Êtes-vous sûr de vouloir annuler cette réservation? (oui/non): ")
        if (StdIn.readLine().toLowerCase != "oui") {
          println("Annulation annulée")
          return true
        }
        
        reservationService.cancelReservation(reservationToCancel.id.get) match {
          case Success(true) =>
            // Remettre les places disponibles
            val newAvailableSeats = ride.availableSeats + reservationToCancel.seatsReserved
            rideService.updateAvailableSeats(ride.id.get, newAvailableSeats) match {
              case Success(_) =>
                println("Réservation annulée avec succès!")
              case Failure(e) =>
                println(s"Réservation annulée mais erreur lors de la mise à jour des places: ${e.getMessage}")
            }
          case Success(false) =>
            println("Réservation non trouvée")
          case Failure(e) =>
            println(s"Erreur lors de l'annulation: ${e.getMessage}")
        }
        
      case Failure(e) =>
        println(s"Erreur: ${e.getMessage}")
    }
    
    true
  }

  private def showVehicleDetails(vehicle: VehicleInfo): Boolean = {
    println(s"\n=== Détails du véhicule ===")
    println(s"Marque: ${vehicle.make}")
    println(s"Modèle: ${vehicle.model}")
    println(s"Année: ${vehicle.year}")
    println(s"Couleur: ${vehicle.color}")
    println(s"Plaque d'immatriculation: ${vehicle.licensePlate}")
    println(s"Nombre de places: ${vehicle.seats}")

    true
  }
}
