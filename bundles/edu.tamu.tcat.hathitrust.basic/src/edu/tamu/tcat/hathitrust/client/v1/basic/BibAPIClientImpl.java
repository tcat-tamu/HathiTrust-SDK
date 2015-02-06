package edu.tamu.tcat.hathitrust.client.v1.basic;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import edu.tamu.tcat.hathitrust.Record;
import edu.tamu.tcat.hathitrust.Record.RecordIdentifier;
import edu.tamu.tcat.hathitrust.client.BibliographicAPIClient;
import edu.tamu.tcat.hathitrust.client.HathiTrustClientException;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.oss.json.JsonException;
import edu.tamu.tcat.oss.json.JsonMapper;

public class BibAPIClientImpl implements BibliographicAPIClient
{
   public static final String HATHI_TRUST = "edu.tamu.tcat.hathitrust.api_endpoint";
   private static DefaultHttpClient client;
   private static HttpGet  get;
   private JsonMapper mapper;
   private ConfigurationProperties config;

   public BibAPIClientImpl()
   {
      // TODO Auto-generated constructor stub
   }

   public void setConfig(ConfigurationProperties config)
   {
      this.config = config;
   }

   public void setJsonMapper(JsonMapper mapper)
   {
      this.mapper = mapper;
   }

   public void activate()
   {

   }

   public void dispose()
   {

   }

   @Override
   public Record lookup(RecordIdentifier id) throws HathiTrustClientException
   {
      hathiConnect(id);
      return null;
   }

   @Override
   public Record lookup(Set<RecordIdentifier> id) throws HathiTrustClientException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public boolean canConnect()
   {
      throw new UnsupportedOperationException();
   }

   Record hathiConnect(RecordIdentifier id)
   {
      String hathiUri = config.getPropertyValue(HATHI_TRUST, String.class);
      URI baseHathi = URI.create(hathiUri);

      // HACK: Need better approach for generating URI's
//      URI recordUri = baseHathi.resolve("volumes").resolve("full").resolve(id.getScheme().toString()).resolve(id.getId() + ".json");
      URI recordUri = baseHathi.resolve("volumes/full/" + id.getScheme().toString() + "/" + id.getId() + ".json");
      client = new DefaultHttpClient();
      get = new HttpGet(recordUri);

      try
      {
         HttpResponse resp = client.execute(get);
         try(InputStream is = resp.getEntity().getContent())
         {
            return mapper.parse(is, BasicRecord.class);
         }
         catch (JsonException e)
         {
            e.printStackTrace();
         }
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      return null;

   }

}
