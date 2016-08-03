package org.datacommons.prepbuddy.exceptions

class ApplicationException(errorMessage: ErrorMessage) extends Throwable{
    override def getMessage: String = errorMessage.getMessage
}
