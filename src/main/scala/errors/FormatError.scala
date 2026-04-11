package errors

case class FormatError(message: String) extends Exception(message)
