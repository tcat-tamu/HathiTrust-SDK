package edu.tamu.tcat.hathitrust.model;

/**
 * Indicates an error creating or accessing a right code registration.
 *
 */
public class RightsCodeRegistrationException extends Exception
{

   public RightsCodeRegistrationException()
   {
   }

   public RightsCodeRegistrationException(String message)
   {
      super(message);
   }

   public RightsCodeRegistrationException(Throwable cause)
   {
      super(cause);
   }

   public RightsCodeRegistrationException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public RightsCodeRegistrationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }

}
