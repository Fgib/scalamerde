import menu.MenuPrincipal

@main def main(): Unit = {

  println(" ____  _       ____  _    __          ___     _")
  println("|  _ \\| |     |  _ \\| |   \\ \\        / (_)   | |")
  println("| |_) | | __ _| |_) | | __ \\ \\  /\\  / / _ ___| |__")
  println("|  _ <| |/ _` |  _ <| |/ _` \\ \\/  \\/ / | / __| '_ \\")
  println("| |_) | | (_| | |_) | | (_| |\\  /\\  /  | \\__ \\ | | |")
  println("|____/|_|\\__,_|____/|_|\\__,_| \\/  \\/   |_|___/_| |_|")

  val menu = new MenuPrincipal()
  menu.start()
}
