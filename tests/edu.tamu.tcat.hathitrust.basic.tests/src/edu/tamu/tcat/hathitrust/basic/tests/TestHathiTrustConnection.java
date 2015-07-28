/*
 * Copyright 2015 Texas A&M Engineering Experiment Station
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.tamu.tcat.hathitrust.basic.tests;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.junit.Test;

import edu.tamu.tcat.hathitrust.basic.oauth.HathiTrustAPICommandBuilder;
import edu.tamu.tcat.hathitrust.basic.oauth.SimpleParameter;
import edu.tamu.tcat.hathitrust.client.v1.basic.DataApiImpl;
import edu.tamu.tcat.hathitrust.data.DataAPIClient;
import edu.tamu.tcat.hathitrust.data.DataAPIClient.DataFormat;
import edu.tamu.tcat.hathitrust.data.DataAPIClient.ImageFormat;


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
