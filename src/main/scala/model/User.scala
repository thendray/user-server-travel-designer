package model

case class User(id: Int, email: String, username: String, password: String, profilePhoto: Array[Byte])