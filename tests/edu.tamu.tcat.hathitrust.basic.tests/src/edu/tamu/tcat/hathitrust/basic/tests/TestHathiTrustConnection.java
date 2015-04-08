package edu.tamu.tcat.hathitrust.basic.tests;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.junit.Test;

import edu.tamu.tcat.hathitrust.basic.oauth.HathiTrustAPICommandBuilder;
import edu.tamu.tcat.hathitrust.basic.oauth.SimpleParameter;
import edu.tamu.tcat.hathitrust.client.DataAPIClient;
import edu.tamu.tcat.hathitrust.client.DataAPIClient.DataFormat;
import edu.tamu.tcat.hathitrust.client.DataAPIClient.ImageFormat;
import edu.tamu.tcat.hathitrust.client.v1.basic.DataApiImpl;


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

   @Test
   public void executeDataAPITest()
   {
      String htid = "loc.ark:/13960/t3bz7b19z";
      DataAPIClient data = new DataApiImpl();

      String aggregateFileName = data.getAggregate(htid);
      String structureFileName = data.getStructure(htid, DataFormat.json);
      String volumeMetaFilename = data.getVolumeMeta(htid, DataFormat.json);
      String pageMetaFileName = data.getPageMeta(htid, DataFormat.json, 89);
      String pageImageFileName = data.getPageImage(htid, ImageFormat.jpeg, 89);
      String pageOCRFileName = data.getPageOCR(htid, 89);
      String pageCoordOCRFileName = data.getPageCoordOCR(htid, 89);
   }

}
