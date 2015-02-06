package edu.tamu.tcat.hathitrust.basic.tests;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.junit.Test;

import edu.tamu.tcat.hathitrust.basic.oauth.HathiTrustAPICommandBuilder;
import edu.tamu.tcat.hathitrust.basic.oauth.SimpleParameter;


public class TestHathiTrustConnection
{

   public TestHathiTrustConnection()
   {
      // TODO Auto-generated constructor stub
   }

   @Test
   public void executeHelloWorldTest() throws Exception
   {
      URI base = URI.create("http://babel.hathitrust.org/cgi/htdc/dapiserver");
      HathiTrustAPICommandBuilder req = new HathiTrustAPICommandBuilder();
      req.setCredentials("PUBLIC_OAUTH_CONSUMER_KEY", "PUBLIC_OAUTH_CONSUMER_SECRET");

      req.setUri(base);
      req.setMethod("GET");
      req.addParameter(SimpleParameter.create("hello", "world"));

      HttpResponse response = req.build().call();
      StatusLine statusLine = response.getStatusLine();
      System.out.println(statusLine);
      assertEquals("Request did not return 200", 200, statusLine.getStatusCode());
   }

}
