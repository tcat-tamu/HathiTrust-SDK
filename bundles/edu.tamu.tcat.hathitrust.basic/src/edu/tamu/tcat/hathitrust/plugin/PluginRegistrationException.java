package edu.tamu.tcat.hathitrust.plugin;

public class PluginRegistrationException extends Exception
{

   public PluginRegistrationException()
   {
   }

   public PluginRegistrationException(String message)
   {
      super(message);
   }

   public PluginRegistrationException(Throwable cause)
   {
      super(cause);
   }

   public PluginRegistrationException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public PluginRegistrationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }

}
