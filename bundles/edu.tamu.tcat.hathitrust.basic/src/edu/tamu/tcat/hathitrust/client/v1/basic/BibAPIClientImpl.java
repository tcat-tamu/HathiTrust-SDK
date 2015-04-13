package edu.tamu.tcat.hathitrust.client.v1.basic;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.hathitrust.client.BibliographicAPIClient;
import edu.tamu.tcat.hathitrust.client.HathiTrustClientException;
import edu.tamu.tcat.hathitrust.model.Record;
import edu.tamu.tcat.hathitrust.model.Record.RecordIdentifier;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;

public class BibAPIClientImpl implements BibliographicAPIClient
{
   private static final Logger logger = Logger.getLogger(BibAPIClientImpl.class.getName());

   public static final String HATHI_TRUST = "edu.tamu.tcat.hathitrust.api_endpoint";

   private DefaultHttpClient client;
   private ObjectMapper mapper;
   private ConfigurationProperties config;
   private URI apiEndpoint;

   public BibAPIClientImpl()
   {
   }

   public void setConfig(ConfigurationProperties config)
   {
      this.config = config;
   }

   public void activate()
   {
      this.mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

      if (config == null)
         throw new IllegalStateException("Activation failed. Configuration properties not available.");

      String url = config.getPropertyValue(HATHI_TRUST, String.class);
      if (url == null || url.trim().isEmpty())
         throw new IllegalStateException("Activation failed. No API endpoint supplied. Expected configuration property for  [" + HATHI_TRUST + "].");

      try
      {
         apiEndpoint = new URI(url.endsWith("/") ? url : url + "/");
      }
      catch (URISyntaxException ex)
      {
         throw new IllegalStateException("Activation failed. Invalid API endpoint supplied [" + url + "].");
      }

      // FIXME this is using the old HttpClient API. Need to use closable variant
      client = new DefaultHttpClient();
   }

   public void dispose()
   {
      try
      {
         client.getConnectionManager().shutdown();
      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, "Failed to shutdown HTTPClient", ex);
      }

      client = null;
      mapper = null;
      apiEndpoint = null;
   }

   @Override
   public Collection<Record> lookup(RecordIdentifier id) throws HathiTrustClientException
   {
      Objects.requireNonNull(apiEndpoint, "No API endpoint supplied.");
      Objects.requireNonNull(client, "Http client not initialized.");
      Objects.requireNonNull(mapper, "JSON mapper not initialized.");

      // HACK: Need better approach for generating URI's
      URI recordUri = createUri(id);

      HttpGet request = new HttpGet(recordUri);
      try
      {
         HttpResponse resp = client.execute(request);
         checkResponseCode(resp, id, recordUri);
         return constructRecords(resp);
      }
      catch (IOException e)
      {
         throw new HathiTrustClientException("Failed to connect to HathiTrust.", e);
      }
      finally
      {
         request.releaseConnection();
      }
   }

   private URI createUri(RecordIdentifier id)
   {
      String scheme = id.getScheme().toString();
      String ident = id.getId();
      String resourceUri = MessageFormat.format("volumes/full/{0}/{1}.json", scheme, ident);
      URI recordUri = URI.create(apiEndpoint.toString() + resourceUri);
      return recordUri;
   }

   private Collection<Record> constructRecords(HttpResponse resp) throws IOException, JsonMappingException
   {
      BibligraphicRecordResult recordResult;
      List<Record> records = new ArrayList<>();
      try (InputStream is = resp.getEntity().getContent())
      {
         recordResult = mapper.readValue(is, BibligraphicRecordResult.class);
         for (Map.Entry<String, RecordDTO> record : recordResult.records.entrySet())
         {
            records.add(RecordDTO.instantiate(record, recordResult.items));
         }

         return records;
      }
      catch (JsonParseException e)
      {
         throw new IllegalStateException("Failed to parse response from server.", e);
      }
   }

   /**
    * Checks the response from a lookup against the HathiTrust API and throws an exception
    * for error resposnse.
    *
    * @param resp the response.
    * @param id the id of the record being requested, for messaging purposes
    * @param recordUri The URI of the requested resource.
    *
    * @throws HathiTrustClientException If the response is not
    * @throws IllegalStateException If the response indicates a bad request or other error
    *       related to formulation of the request.
    */
   private void checkResponseCode(HttpResponse resp, RecordIdentifier id, URI recordUri) throws HathiTrustClientException
   {
      int statusCode = resp.getStatusLine().getStatusCode();
      switch (statusCode)
      {
         case 404:
            throw new HathiTrustClientException("Failed to retrieve [" + id + "]. Record not found");
         case 400:
            throw new IllegalStateException("Failed to retrieve [" + id + "]. HathiTrust responded with bad request for [" + recordUri + "]");

         default:
            // TODO do better job of passing through remote error messaging or logging for internal use.
            if (statusCode >= 500)
               throw new HathiTrustClientException("Failed to retrieve [" + id + "]. Connection with HathiTrust failed due to server error.");

            if (statusCode > 400)
               throw new HathiTrustClientException("Failed to retrieve [" + id + "]. Unexpected error code [" + statusCode + "]");
            break;
      }
   }

   @Override
   public Collection<Record> lookup(Set<RecordIdentifier> id) throws HathiTrustClientException
   {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean canConnect()
   {
      // TODO Rather than pinging a random book, lets ping the base API and see what comes
      //      back. If we get something (even an error), we're probably connected.
      URI checkUri = URI.create(apiEndpoint.toString() + "volumes/brief/oclc/424023.json");
      HttpGet request = new HttpGet(checkUri);
      try
      {
         HttpResponse resp = client.execute(request);
         return resp.getStatusLine().getStatusCode() < 300;
      }
      catch (IOException e)
      {
         logger.log(Level.WARNING, "Failed to connect to HathiTrust endpoing [" + checkUri + "].", e);
         return false;
      }
      finally
      {
         request.releaseConnection();
      }
   }
}
