package edu.tamu.tcat.hathitrust.htrc.features.simple.impl;

import java.io.InputStream;
import java.nio.channels.ClosedByInterruptException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.hathitrust.HathiTrustClientException;
import edu.tamu.tcat.hathitrust.htrc.features.simple.ExtractedFeatures;

/**
 * A default implementation of {@link ExtractedFeatures} which is used with {@link DefaultExtractedFeaturesProvider}.
 */
public class DefaultExtractedFeatures implements ExtractedFeatures, ExtractedFeatures.Metadata
{
   private static final Logger debug = Logger.getLogger(DefaultExtractedFeatures.class.getName());
   
   private final DefaultExtractedFeaturesProvider parent;
   private final String vid;
   private final Path basic;
   private final Path advanced;
   
   private Future<Map<String, ?>> basicData;
   private Future<Map<String, ?>> advancedData;
   
   public DefaultExtractedFeatures(DefaultExtractedFeaturesProvider parent,
                                   String vid,
                                   Path basic,
                                   Path advanced)
   {
      this.parent = Objects.requireNonNull(parent);
      this.vid = Objects.requireNonNull(vid);
      this.basic = basic;
      this.advanced = advanced;
   }
   
   @Override
   public String toString()
   {
      return "ex feat["+vid+" of "+parent+"]";
   }
   
   /**
    * Load the basic and advanced data from disk archive in the given executor.
    * 
    * @param exec
    */
   //TODO: this could be done less agressively to not load basic/advanced if not needed
   public void load(ExecutorService exec)
   {
      if (basic != null)
         basicData = exec.submit(() -> doLoad(basic, ExtractedFeatures.schemaVersionBasic));
      if (advanced != null)
         advancedData = exec.submit(() -> doLoad(advanced, ExtractedFeatures.schemaVersionAdvanced));
      
      if (basicData == null && advancedData == null)
         debug.log(Level.WARNING, "No basic or advanced data provided for volume ["+vid+"]");
   }
   
   private Map<String, ?> doLoad(Path p, String ver) throws Exception
   {
      try
      {
         debug.fine("loading " + p);
         Map<?,?> value = null;
         // open basic path as a bz2 and use Jackson to parse into raw data vehicles
         try (InputStream str = Files.newInputStream(p);
              BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(str))
         {
            ObjectMapper mapper = new ObjectMapper();
            value = mapper.readValue(bzIn, Map.class);
         }
         
         Map<String, ?> data = (Map)value;
         
         // validate schema
         Map<String, ?> features = (Map)data.get("features");
         if (features == null)
            throw new HathiTrustClientException("Data missing element 'features'");
            
         Object sv = features.get("schemaVersion");
         if (!Objects.equals(sv, ver))
            throw new HathiTrustClientException("Unexpected schema version ["+sv+"] expecting ["+ver+"]");
         
         // Return entire validated data vehicle in raw format
         return data;
      }
      catch (ClosedByInterruptException ie)
      {
         debug.log(Level.FINE, "Failed loading (due to interrupt) "+p+" "+ver+" ["+vid+"]");
         throw ie;
      }
      catch (Exception e)
      {
         debug.log(Level.SEVERE, "Failed loading "+p+" "+ver+" ["+vid+"]", e);
         throw e;
      }
   }
   
   /**
    * Get the "basic" JSON data vehicle. Does not return {@code null}
    */
   private Map<String, ?> getBasic() throws Exception
   {
      if (basicData == null)
         throw new IllegalStateException("No basic data available");
      
      // Don't allow unbounded 'get'; could be configurable
      Map<String, ?> data = basicData.get(10, TimeUnit.MINUTES);
      return data;
   }

   /**
    * Get the "advanced" JSON data vehicle. Does not return {@code null}
    */
   private Map<String, ?> getAdvanced() throws Exception
   {
      if (advancedData == null)
         throw new IllegalStateException("No advanced data available");
      
      // Don't allow unbounded 'get'; could be configurable
      Map<String, ?> data = advancedData.get(10, TimeUnit.MINUTES);
      return data;
   }
   
   @Override
   public void close() throws Exception
   {
      parent.closed(this);
   }

   @Override
   public String getVolumeId()
   {
      return vid;
   }

   @Override
   public Metadata getMetadata()
   {
      //HACK: simpler impl has singleton the same as this instance
      return this;
   }
   
   @Override
   public ExtractedFeatures getVolume()
   {
      return this;
   }
   
   @Override
   public String title() throws HathiTrustClientException
   {
      try
      {
         return getMetaValue("title", String.class);
      }
      catch (Exception e)
      {
         throw new HathiTrustClientException("Failed accessing metadata [metadata.title] on ["+vid+"]", e);
      }
   }
   
   private <T> T getMetaValue(String key, Class<T> type) throws Exception
   {
      Map<String, ?> map = null;
      if (basicData != null)
         map = getBasic();
      else
         map = getAdvanced();
      
      Map<String, ?> meta = (Map)map.get("metadata");
      Object v = meta.get(key);
      return (T)v;
   }

   private <T> T getFeaturesValue(String key, Class<T> type) throws Exception
   {
      Map<String, ?> map = null;
      if (basicData != null)
         map = getBasic();
      else
         map = getAdvanced();
      
      Map<String, ?> meta = (Map)map.get("features");
      Object v = meta.get(key);
      return (T)v;
   }
   
   @Override
   public int pageCount() throws HathiTrustClientException
   {
      try
      {
         Number v = getFeaturesValue("pageCount", Number.class);
         if (v == null)
            throw new IllegalStateException("Missing value 'features.pageCount'");
         
         return v.intValue();
      }
      catch (Exception e)
      {
         throw new HathiTrustClientException("Failed accessing metadata [features.pageCount] on ["+vid+"]", e);
      }
   }

   @Override
   public ExtractedPageFeatures getPage(int page)
   {
      return new DefaultPage(this, page);
   }
   
   public static class DefaultPage implements ExtractedFeatures.ExtractedPageFeatures
   {
      private final DefaultExtractedFeatures parent;
      private final int index;
      
      // cache basic/advanced data to improve performance of repeated access, such as for token POS counts
      //@GuardedBy("this")
      private Map<String, ?> pageDataBasic;
      //@GuardedBy("this")
      private Map<String, ?> pageDataAdvanced;
      
      public DefaultPage(DefaultExtractedFeatures parent, int index)
      {
         this.index = index;
         this.parent = Objects.requireNonNull(parent);
      }
      
      @Override
      public String toString()
      {
         return "page["+index+" of "+parent+"]";
      }

      @Override
      public ExtractedFeatures getVolume()
      {
         return parent;
      }
      
      @Override
      public int getPageIndex()
      {
         return index;
      }
      
      private synchronized Map<String, ?> loadPageBasicData() throws Exception
      {
         if (pageDataBasic == null)
         {
            Map<String, ?> data = parent.getBasic();
            Map<String, ?> features = (Map)data.get("features");
            if (features == null)
               throw new IllegalStateException("Basic data missing 'features' element");
            List<?> pages = (List<?>)features.get("pages");
            if (pages == null)
               throw new IllegalStateException("Basic data missing 'features.pages' element");
            pageDataBasic = (Map)pages.get(index);
         }
         
         return pageDataBasic;
      }

      private synchronized Map<String, ?> loadPageAdvancedData() throws Exception
      {
         if (pageDataAdvanced == null)
         {
            Map<String, ?> data = parent.getAdvanced();
            Map<String, ?> features = (Map)data.get("features");
            if (features == null)
               throw new IllegalStateException("Advanced data missing 'features' element");
            List<?> pages = (List<?>)features.get("pages");
            if (pages == null)
               throw new IllegalStateException("Advanced data missing 'features.pages' element");
            pageDataAdvanced = (Map)pages.get(index);
         }
         return pageDataAdvanced;
      }
      
      @Override
      public String seq() throws HathiTrustClientException
      {
         try
         {
            return (String)loadPageBasicData().get("seq");
         }
         catch (Exception e)
         {
            throw new HathiTrustClientException("Failed accessing data [seq] on ["+this+"]", e);
         }
      }

      @Override
      public String dateCreated() throws HathiTrustClientException
      {
         try
         {
            return (String)loadPageBasicData().get("dateCreated");
         }
         catch (Exception e)
         {
            throw new HathiTrustClientException("Failed accessing data [dateCreated] on ["+this+"]", e);
         }
      }

      @Override
      public int tokenCount() throws HathiTrustClientException
      {
         try
         {
            Number v = (Number)loadPageBasicData().get("tokenCount");
            return v.intValue();
         }
         catch (Exception e)
         {
            throw new HathiTrustClientException("Failed accessing data [tokenCount] on ["+this+"]", e);
         }
      }

      @Override
      public int lineCount() throws HathiTrustClientException
      {
         try
         {
            Number v = (Number)loadPageBasicData().get("lineCount");
            return v.intValue();
         }
         catch (Exception e)
         {
            throw new HathiTrustClientException("Failed accessing data [lineCount] on ["+this+"]", e);
         }
      }

      @Override
      public ExtractedFeatures.ExtractedPagePartOfSpeechData getBodyData() throws HathiTrustClientException
      {
         return new DefaultPOS(this, "body");
      }
   }
   
   public static class DefaultPOS implements ExtractedFeatures.ExtractedPagePartOfSpeechData
   {
      private final DefaultPage parent;
      private final String section;

      public DefaultPOS(DefaultPage parent, String section)
      {
         this.parent = Objects.requireNonNull(parent);
         this.section = Objects.requireNonNull(section);
      }
      
      @Override
      public String toString()
      {
         return "part of speech ["+section+" on "+parent+"]";
      }

      @Override
      public ExtractedFeatures.ExtractedPageFeatures getPage()
      {
         return parent;
      }

      @Override
      public boolean isBody()
      {
         return section.equals("body");
      }

      @Override
      public boolean isHeader()
      {
         return section.equals("header");
      }

      @Override
      public boolean isFooter()
      {
         return section.equals("footer");
      }

      @Override
      public Set<String> tokens() throws HathiTrustClientException
      {
         try
         {
            Map<String, ?> secData = (Map)parent.loadPageBasicData().get(section);
            if (secData == null)
               throw new IllegalStateException("Section ["+section+"] has no basic data");
            
            Map<String, ?> tokensData = (Map)secData.get("tokenPosCount");
            if (tokensData == null)
               throw new IllegalStateException("Section ["+section+"] has no basic 'tokenPosCount' data");
               
            return Collections.unmodifiableSet(tokensData.keySet());
         }
         catch (Exception e)
         {
            throw new HathiTrustClientException("Failed accessing token data on ["+this+"]", e);
         }
      }

      @Override
      public Map<String, Integer> getPosCount(String token) throws HathiTrustClientException
      {
         try
         {
            Map<String, ?> secData = (Map)parent.loadPageBasicData().get(section);
            if (secData == null)
               throw new IllegalStateException("Section ["+section+"] has no basic data");
            
            Map<String, ?> tokensData = (Map)secData.get("tokenPosCount");
            if (tokensData == null)
               throw new IllegalStateException("Section ["+section+"] has no basic 'tokenPosCount' data");
               
            // This map is typically of size=1
            Map<String, ?> tokData = (Map)tokensData.get(token);
            
            // Asked for invalid token
            if (tokData == null)
               return Collections.emptyMap();
            
            Map<String, Integer> rv = new HashMap<>();
            
            for (Map.Entry<String, ?> entry : tokData.entrySet())
            {
               Number n = (Number)entry.getValue();
               Integer v = null;
               if (n instanceof Integer)
                  v = (Integer)n;
               else
                  v = Integer.valueOf(n.intValue());
               rv.put(entry.getKey(), v);
            }
            
            return rv;
         }
         catch (Exception e)
         {
            throw new HathiTrustClientException("Failed accessing token data on ["+this+"]", e);
         }
      }

      @Override
      public int getCount(String token) throws HathiTrustClientException
      {
         // sum counts of all parts of speech for the given token
         int count = getPosCount(token).values().stream().mapToInt(Integer::intValue).sum();
         return count;
      }
   }
}
