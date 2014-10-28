package edu.tamu.tcat.hathitrust.basic.oauth;

public class OAuthException extends Exception
{

   public OAuthException()
   {
   }

   public OAuthException(String message)
   {
      super(message);
   }

   public OAuthException(Throwable cause)
   {
      super(cause);
   }

   public OAuthException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public OAuthException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }

}
