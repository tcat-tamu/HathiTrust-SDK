package edu.tamu.tcat.hathitrust.basic.oauth;

import java.net.URI;

import org.apache.http.HttpResponse;

public interface OAuthRequest
{
   OAuthRequest setCredentials(OAuthCredentials cred);
   OAuthRequest setMethod(String m);
   OAuthRequest setUri(URI uri);
   OAuthRequest setHeader(String header, String value);
   OAuthRequest addParameter(Parameter param);

   // TODO: need to accept something here
   OAuthRequest setBody();

   HttpResponse execute() throws OAuthException;
}
