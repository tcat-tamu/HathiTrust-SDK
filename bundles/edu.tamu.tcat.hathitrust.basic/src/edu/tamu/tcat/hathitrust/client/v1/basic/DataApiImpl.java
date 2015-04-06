package edu.tamu.tcat.hathitrust.client.v1.basic;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;

import edu.tamu.tcat.hathitrust.basic.oauth.HathiTrustAPICommandBuilder;
import edu.tamu.tcat.hathitrust.basic.oauth.SimpleParameter;
import edu.tamu.tcat.hathitrust.client.DataAPI;

public class DataApiImpl implements DataAPI
{
   // TODO: Pull key, access and base URI from configuration properties.
   Logger logger = Logger.getLogger("edu.tamu.tcat.hathitrust.client.v1.basic");
   private final String key = "f5a66701ca";
   private final String access = "a6bd838c2643e8686881d8b77bd5";
   private final URI base = URI.create("https://babel.hathitrust.org/cgi/htd");
   private final Path tempPath = Paths.get("C:\\Users\\jesse.mitchell\\Documents\\SDA\\Documents\\");

   public DataApiImpl()
   {
   }

   private String sendRequest(URI htDataApi, String formatType, Path tempFile)
   {
      HathiTrustAPICommandBuilder req = new HathiTrustAPICommandBuilder();
      req.setCredentials(key, access);
      req.setUri(htDataApi);
      req.setMethod("GET");
      req.addParameter(SimpleParameter.create("v", "2"));
      if(!formatType.isEmpty())
         req.addParameter(SimpleParameter.create("format", formatType));

      try
      {
         HttpResponse response = req.build().call();
         if(response.getStatusLine().getStatusCode() != 200)
            throw new IllegalStateException("An error occured while retreiving data from HathiTrust URI: [" + htDataApi + "]");

         try(InputStream content = response.getEntity().getContent();
             FileOutputStream fileOut = new FileOutputStream(tempFile.toFile()))
         {
            int c;
            while ((c = content.read()) != -1)
            {
               fileOut.write(c);
            }
            return tempFile.getFileName().toString();
         }
      }
      catch (Exception e)
      {
         logger.log(Level.FINE, "An error occured while retreiving data from HathiTrust uri [" + htDataApi.toString() + "]");
         throw new IllegalStateException("An error occured while retreiving data from HathiTrust", e);
      }

   }

   @Override
   public String getAggregate(String htid)
   {
      URI aggregate = base.resolve("htd/aggregate/" + htid);
      Path temp = tempPath.resolve("aggregate.zip");
      return sendRequest(aggregate, "", temp);
   }

   @Override
   public String getStructure(String htid, DataFormat format)
   {
      URI aggregate = base.resolve("htd/structure/" + htid);
      Path temp = tempPath.resolve("structure.txt");
      return sendRequest(aggregate, format.toString(), temp);

   }

   @Override
   public String getVolume(String htid, DataFormat format)
   {
//      URI aggregate = base.resolve("htd/volume/" + htid);
//      Path temp = tempPath.resolve("volume.pdf");
//      return sendRequest(aggregate, format.toString(), temp);
      throw new UnsupportedOperationException();
   }

   @Override
   public String getVolumeMeta(String htid, DataFormat format)
   {
      URI aggregate = base.resolve("htd/volume/meta/" + htid);
      Path temp = tempPath.resolve("VolumeMeta.txt");
      return sendRequest(aggregate, format.toString(), temp);
   }

   @Override
   public String getPageMeta(String htid, DataFormat format, int pageSeqNum)
   {
      URI aggregate = base.resolve("htd/volume/pagemeta/" + htid + "/" + pageSeqNum);
      Path temp = tempPath.resolve("VolumePageMeta.txt");
      return sendRequest(aggregate, format.toString(), temp);
   }

   @Override
   public String getPageImage(String htid, ImageFormat format, int pageSeqNum)
   {
      URI aggregate = base.resolve("htd/volume/pageimage/" + htid + "/" + pageSeqNum);
      Path temp = tempPath.resolve("VolumePageImage." + format.toString());
      return sendRequest(aggregate, format.toString(), temp);
   }

   @Override
   public String getPageOCR(String htid, int pageSeqNum)
   {
      URI aggregate = base.resolve("htd/volume/pageocr/" + htid + "/" + pageSeqNum);
      Path temp = tempPath.resolve("VolumePageOCR.txt");
      return sendRequest(aggregate, "", temp);
   }

   @Override
   public String getPageCoordOCR(String htid, int pageSeqNum)
   {
      URI aggregate = base.resolve("htd/volume/pagecoordocr/" + htid + "/" + pageSeqNum);
      Path temp = tempPath.resolve("VolumePageCoordOCR.txt");
      return sendRequest(aggregate, "", temp);
   }
}
