package edu.tamu.tcat.hathitrust.basic.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import edu.tamu.tcat.hathitrust.client.HathiTrustClientException;
import edu.tamu.tcat.hathitrust.client.v1.basic.BibAPIClientImpl;
import edu.tamu.tcat.hathitrust.model.Item;
import edu.tamu.tcat.hathitrust.model.Record;
import edu.tamu.tcat.hathitrust.model.Record.IdType;
import edu.tamu.tcat.hathitrust.model.Record.RecordIdentifier;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.osgi.config.file.SimpleFileConfigurationProperties;
import edu.tamu.tcat.oss.json.jackson.JacksonJsonMapper;

public class TestBibAPIClient
{

   public TestBibAPIClient()
   {
      // TODO Auto-generated constructor stub
   }

   private SimpleFileConfigurationProperties getConfigProperties()
   {
      SimpleFileConfigurationProperties config = new SimpleFileConfigurationProperties();
      Map<String, Object> params = new HashMap<>();
      Path propsPath = Paths.get("config.properties");
      propsPath = propsPath.toAbsolutePath();
      params.put(SimpleFileConfigurationProperties.PROP_FILE, propsPath.toString());
      config.activate(params);
      return config;
   }

   @Test
   public void testClientConnection()
   {

      BibAPIClientImpl client = new BibAPIClientImpl();
      assertTrue("Unable to connect to HathiTrust", client.canConnect());

      client.setConfig(new ConfigurationPropertiesImpl());
      JacksonJsonMapper mapper = new JacksonJsonMapper();
      mapper.activate();

      BibligraphicRecordResultData result = new BibligraphicRecordResultData();
      Record bibRecord = result.getBibRecord();
      List<RecordIdentifier> recordIdents = bibRecord.getIdentifiers(IdType.OCLC);
      RecordIdentifier recordIdent = recordIdents.get(0);
      try
      {
         Collection<Record> lookup = client.lookup(recordIdent);
         for (Record recordResult : lookup)
         {
            assertEquals(bibRecord.getId(), recordResult.getId());

            Item bibRecordItems = bibRecord.getItems().get(0);
            Item recordResultItems = recordResult.getItems().get(0);
            assertEquals(bibRecordItems.getRecordId(), recordResultItems.getRecordId());
            assertEquals(bibRecordItems.getItemURL(), recordResultItems.getItemURL());
            assertEquals(bibRecordItems.getOriginatingInstitution(), recordResultItems.getOriginatingInstitution());
            assertEquals(bibRecordItems.getRightsCode(), recordResultItems.getRightsCode());

            assertEquals(bibRecord.getMarcRecord(), recordResult.getMarcRecord());
            assertEquals(bibRecord.getPublishDates(), recordResult.getPublishDates());
            assertEquals(bibRecord.getRecordURL(), recordResult.getRecordURL());
            assertEquals(bibRecord.getTitles(), recordResult.getTitles());

            assertTrue("ISBN Results did not match.", checkIdentType(bibRecord.getIdentifiers(IdType.ISBN), recordResult.getIdentifiers(IdType.ISBN)));
            assertTrue("ISSN Results did not match.", checkIdentType(bibRecord.getIdentifiers(IdType.ISSN), recordResult.getIdentifiers(IdType.ISSN)));
            assertTrue("LCCN Results did not match.", checkIdentType(bibRecord.getIdentifiers(IdType.LCCN), recordResult.getIdentifiers(IdType.LCCN)));
            assertTrue("OCLC Results did not match.", checkIdentType(bibRecord.getIdentifiers(IdType.OCLC), recordResult.getIdentifiers(IdType.OCLC)));
         }
      }
      catch (HathiTrustClientException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      // client.setJsonMapper(mapper);
   }

   private boolean checkIdentType(List<RecordIdentifier> orig, List<RecordIdentifier> result)
   {
      boolean pass = false;

      for(RecordIdentifier originalIdent : orig)
      {
         for(RecordIdentifier resultIdent : result)
         {
            if(!originalIdent.getId().equals(resultIdent.getId()))
               break;

            assertEquals(originalIdent.getId(), resultIdent.getId());
            assertEquals(originalIdent.getScheme(), resultIdent.getScheme());
            pass = true;
         }
      }

      if (orig.isEmpty() && result.isEmpty())
         pass = true;
      return pass;
   }

   private class ConfigurationPropertiesImpl implements ConfigurationProperties
   {

      @Override
      public <T> T getPropertyValue(String name, Class<T> type) throws IllegalStateException
      {
         if (!name.equalsIgnoreCase(BibAPIClientImpl.HATHI_TRUST))
            throw new IllegalStateException("No value configured for property '" + name + "'");

         if (type != String.class)
            throw new IllegalStateException("Expected String type");

         return (T)"http://catalog.hathitrust.org/api/";
      }

      @Override
      public <T> T getPropertyValue(String name, Class<T> type, T defaultValue) throws IllegalStateException
      {
         if (!name.equalsIgnoreCase(BibAPIClientImpl.HATHI_TRUST))
            throw new IllegalStateException("No value configured for property '" + name + "'");

         if (type != String.class)
            throw new IllegalStateException("Expected String type");

         return (T)"http://catalog.hathitrust.org/api/";
      }

   }
}
