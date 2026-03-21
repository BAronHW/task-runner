package error_handling

sealed trait ErrorType
case object UnknownDependancy extends ErrorType
case object CyclicalDependancy extends ErrorType
case object FormatError extends ErrorType
case object DefaultError extends ErrorType