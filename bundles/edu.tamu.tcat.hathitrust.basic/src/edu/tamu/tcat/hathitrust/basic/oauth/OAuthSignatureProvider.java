package edu.tamu.tcat.hathitrust.basic.oauth;

import java.security.SignatureException;

public interface OAuthSignatureProvider
{
   String getName();

   void setKey(byte[] key);

   byte[] getSignature(byte[] data) throws SignatureException;
}
