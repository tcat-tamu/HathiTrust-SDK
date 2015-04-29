package edu.tamu.tcat.hathitrust.basic.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.junit.Test;

import edu.tamu.tcat.hathitrust.HathiTrustClientException;
import edu.tamu.tcat.hathitrust.bibliography.Item;
import edu.tamu.tcat.hathitrust.bibliography.Record;
import edu.tamu.tcat.hathitrust.bibliography.Record.IdType;
import edu.tamu.tcat.hathitrust.bibliography.Record.RecordIdentifier;
import edu.tamu.tcat.hathitrust.client.v1.basic.BibAPIClientImpl;

public class TestBibAPIClient
{
//   private SimpleFileConfigurationProperties getConfigProperties()
//   {
//      SimpleFileConfigurationProperties config = new SimpleFileConfigurationProperties();
//      Map<String, Object> params = new HashMap<>();
//      Path propsPath = Paths.get("config.properties");
//      propsPath = propsPath.toAbsolutePath();
//      params.put(SimpleFileConfigurationProperties.PROP_FILE, propsPath.toString());
//      config.activate(params);
//      return config;
//   }

   @Test
   public void testClientConnection()
   {
      try (BibAPIClientImpl client = BibAPIClientImpl.create("http://catalog.hathitrust.org/api/"))
      {
         assertTrue("Unable to connect to HathiTrust", client.canConnect());
   
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
   
               assertEquals(bibRecord.getMarcRecordXML(), recordResult.getMarcRecordXML());
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

//   private class ConfigurationPropertiesImpl implements ConfigurationProperties
//   {
//
//      @Override
//      public <T> T getPropertyValue(String name, Class<T> type) throws IllegalStateException
//      {
//         if (!name.equalsIgnoreCase(BibAPIClientImpl.HATHI_TRUST))
//            throw new IllegalStateException("No value configured for property '" + name + "'");
//
//         if (type != String.class)
//            throw new IllegalStateException("Expected String type");
//
//         return (T)"http://catalog.hathitrust.org/api/";
//      }
//
//      @Override
//      public <T> T getPropertyValue(String name, Class<T> type, T defaultValue) throws IllegalStateException
//      {
//         if (!name.equalsIgnoreCase(BibAPIClientImpl.HATHI_TRUST))
//            throw new IllegalStateException("No value configured for property '" + name + "'");
//
//         if (type != String.class)
//            throw new IllegalStateException("Expected String type");
//
//         return (T)"http://catalog.hathitrust.org/api/";
//      }
//   }
}
