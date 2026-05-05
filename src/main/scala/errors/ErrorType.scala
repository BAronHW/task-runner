package errors

case class UnknownDependancy(message: String) extends Exception(message)
case class CyclicalDependancy(message: String) extends Exception(message)
case class FormatError(message: String) extends Exception(message)
case class DefaultError(message: String) extends Exception(message)
