package edu.tamu.tcat.hathitrust.basic.tests;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.junit.Test;

import edu.tamu.tcat.hathitrust.basic.oauth.OAuthCredentials;
import edu.tamu.tcat.hathitrust.basic.oauth.OAuthException;
import edu.tamu.tcat.hathitrust.basic.oauth.OAuthRequest;
import edu.tamu.tcat.hathitrust.basic.oauth.SimpleParameter;


public class TestHathiTrustConnection
{

   public TestHathiTrustConnection()
   {
      // TODO Auto-generated constructor stub
   }

   @Test
   public void executeHelloWorldTest() throws OAuthException
   {
      URI base = URI.create("http://babel.hathitrust.org/cgi/htdc/dapiserver");
      OAuthRequest req = new OAuthRequest(new OAuthCredentials()
      {
         @Override
         public String getToken() { return ""; }
         @Override
         public String getSecret() { return "PUBLIC_OAUTH_CONSUMER_SECRET"; }
         @Override
         public String getKey() { return "PUBLIC_OAUTH_CONSUMER_KEY"; }
      });

      req.setUri(base);
      req.setMethod("GET");
      req.addParameter(SimpleParameter.create("hello", "world"));

      HttpResponse response = req.execute();
      StatusLine statusLine = response.getStatusLine();
      System.out.println(statusLine);
      assertEquals("Request did not return 200", 200, statusLine.getStatusCode());
   }

}
