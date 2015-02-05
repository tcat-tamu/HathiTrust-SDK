package edu.tamu.tcat.hathitrust.basic.tests;

import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import edu.tamu.tcat.hathitrust.Record;
import edu.tamu.tcat.hathitrust.Record.IdType;
import edu.tamu.tcat.hathitrust.client.HathiTrustClientException;
import edu.tamu.tcat.hathitrust.client.basic.BasicRecord;
import edu.tamu.tcat.hathitrust.client.basic.BibAPIClientImpl;
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
         Record lookup = client.lookup(recordIdent);
      }
      catch (HathiTrustClientException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      // client.setJsonMapper(mapper);
      assertTrue("", client.canConnect());
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
