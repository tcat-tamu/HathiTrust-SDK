package edu.tamu.tcat.hathitrust.basic.tests;

import static org.junit.Assert.*;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import edu.tamu.tcat.hathitrust.Record;
import edu.tamu.tcat.hathitrust.Record.IdType;
import edu.tamu.tcat.hathitrust.Record.RecordIdentifier;
import edu.tamu.tcat.hathitrust.client.HathiTrustClientException;
import edu.tamu.tcat.hathitrust.client.v1.basic.BasicRecord;
import edu.tamu.tcat.hathitrust.client.v1.basic.BibAPIClientImpl;
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
      String oclcNum = "379388464";
      IdType oclc = IdType.OCLC;
      BasicRecord record = new BasicRecord();
      BasicRecord.BasicRecordIdentifier recordIdent = record.new BasicRecordIdentifier(oclc, oclcNum);
//      SimpleFileConfigurationProperties config = getConfigProperties();

      BibAPIClientImpl client = new BibAPIClientImpl();
      client.setConfig(new ConfigurationPropertiesImpl());
      JacksonJsonMapper mapper = new JacksonJsonMapper();
      mapper.activate();
      client.setJsonMapper(mapper);
      try
      {
         Collection<Record> lookup = client.lookup(recordIdent);
         for (Record recordResult : lookup)
         {
            boolean identNumberListed = false;
            List<RecordIdentifier> identifiers = recordResult.getIdentifiers(oclc);
            for(RecordIdentifier resultRecordIdent : identifiers)
            {
               if(oclcNum.equals(resultRecordIdent.getId()))
               {
                  identNumberListed = true;
                  break;
               }
            }
            assertTrue("OCLC number do not match.", identNumberListed);
         }
      }
      catch (HathiTrustClientException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      // client.setJsonMapper(mapper);
      assertTrue("Unable to connect to HathiTrust", client.canConnect());
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
