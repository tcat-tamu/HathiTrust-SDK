package edu.tamu.tcat.hathitrust;

/**
 *  Indicates an error either accessing or retrieving data from HathiTrust.
 *
 */
public class HathiTrustClientException extends Exception
{

   public HathiTrustClientException()
   {
   }

   public HathiTrustClientException(String message)
   {
      super(message);
   }

   public HathiTrustClientException(Throwable cause)
   {
      super(cause);
   }

   public HathiTrustClientException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public HathiTrustClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }

}
