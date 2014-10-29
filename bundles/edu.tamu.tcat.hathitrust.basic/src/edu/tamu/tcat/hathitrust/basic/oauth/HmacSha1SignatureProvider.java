package edu.tamu.tcat.hathitrust.basic.oauth;

import java.security.SignatureException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HmacSha1SignatureProvider implements OAuthSignatureProvider
{
   private static final String HMAC_SHA1_SPEC = "HmacSHA1";

   private byte[] key;

   @Override
   public void setKey(byte[] key)
   {
      this.key = key;
   }

   @Override
   public String getName()
   {
      return "HMAC-SHA1";
   }

   @Override
   public byte[] getSignature(byte[] data) throws SignatureException
   {
      try {

         // get an hmac_sha1 key from the raw key bytes
         SecretKeySpec signingKey = new SecretKeySpec(key, HMAC_SHA1_SPEC);

         // get an hmac_sha1 Mac instance and initialize with the signing key
         Mac mac = Mac.getInstance(HMAC_SHA1_SPEC);
         mac.init(signingKey);
         return mac.doFinal(data);

      } catch (Exception e) {
         throw new SignatureException("Failed to generate HMAC", e);
      }
   }
}
