package edu.tamu.tcat.hathitrust.basic.tests;

import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import edu.tamu.tcat.hathitrust.client.basic.BibAPIClientImpl;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.osgi.config.file.SimpleFileConfigurationProperties;

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
      SimpleFileConfigurationProperties config = getConfigProperties();

      client.setConfig(new ConfigurationPropertiesImpl());
      // client.setJsonMapper(mapper);
      assertTrue("", client.canConnect());
   }

   private class ConfigurationPropertiesImpl implements ConfigurationProperties
   {

      @Override
      public <T> T getPropertyValue(String name, Class<T> type) throws IllegalStateException
      {
         if (!name.equalsIgnoreCase("edu.tamu.tcat.hathitrust.api_endpoint"))
            throw new IllegalStateException("No value configured for property '" + name + "'");

         if (type != String.class)
            throw new IllegalStateException("Expected String type");

         return (T)"http://catalog.hathitrust.org/api/";
      }

      @Override
      public <T> T getPropertyValue(String name, Class<T> type, T defaultValue) throws IllegalStateException
      {
         if (!name.equalsIgnoreCase("edu.tamu.tcat.hathitrust.api_endpoint"))
            throw new IllegalStateException("No value configured for property '" + name + "'");

         if (type != String.class)
            throw new IllegalStateException("Expected String type");

         return (T)"http://catalog.hathitrust.org/api/";
      }

   }
}
