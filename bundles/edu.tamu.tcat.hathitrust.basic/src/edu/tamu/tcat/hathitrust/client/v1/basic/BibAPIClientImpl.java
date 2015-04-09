package edu.tamu.tcat.hathitrust.client.v1.basic;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.hathitrust.client.BibliographicAPIClient;
import edu.tamu.tcat.hathitrust.client.HathiTrustClientException;
import edu.tamu.tcat.hathitrust.model.Record;
import edu.tamu.tcat.hathitrust.model.Record.RecordIdentifier;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;

public class BibAPIClientImpl implements BibliographicAPIClient
{
   public static final String HATHI_TRUST = "edu.tamu.tcat.hathitrust.api_endpoint";
   private static DefaultHttpClient client;
   private static HttpGet  get;
   private ObjectMapper mapper;
   private ConfigurationProperties config;

   public BibAPIClientImpl()
   {
      // TODO Auto-generated constructor stub
   }

   public void setConfig(ConfigurationProperties config)
   {
      this.config = config;
      this.mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
   }

   public void activate()
   {

   }

   public void dispose()
   {

   }

   @Override
   public Collection<Record> lookup(RecordIdentifier id) throws HathiTrustClientException
   {
      return Collections.unmodifiableList(retrieveHathiRecords(id));
   }

   @Override
   public Collection<Record> lookup(Set<RecordIdentifier> id) throws HathiTrustClientException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public boolean canConnect()
   {
      String hathiUri = config.getPropertyValue(HATHI_TRUST, String.class);
      URI checkUri = URI.create(hathiUri + "volumes/brief/oclc/424023.json");
      client = new DefaultHttpClient();
      get = new HttpGet(checkUri);
      try
      {
         HttpResponse resp = client.execute(get);
         if (resp.getStatusLine().getStatusCode() < 300)
            return true;
         else
            return false;
      }
      catch (IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return false;
   }

   List<Record> retrieveHathiRecords(RecordIdentifier id)
   {
      BibligraphicRecordResult recordResult = null;
      List<Record> records = new ArrayList<>();
      String hathiUri = config.getPropertyValue(HATHI_TRUST, String.class);
      URI baseHathi = URI.create(hathiUri);
      // HACK: Need better approach for generating URI's
      URI recordUri = baseHathi.resolve("volumes/full/" + id.getScheme().toString() + "/" + id.getId() + ".json");
      client = new DefaultHttpClient();
      get = new HttpGet(recordUri);

      try
      {
         HttpResponse resp = client.execute(get);
         try(InputStream is = resp.getEntity().getContent())
         {
            recordResult = mapper.readValue(is, BibligraphicRecordResult.class);
         }
         catch (JsonParseException e)
         {
            e.printStackTrace();
         }
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }

      for (Map.Entry<String, RecordDTO> record : recordResult.records.entrySet())
      {
         records.add(RecordDTO.instantiate(record, recordResult.items));
      }
      return records;

   }
}
