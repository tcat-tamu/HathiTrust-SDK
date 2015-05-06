package edu.tamu.tcat.hathitrust.basic.tests;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import edu.tamu.tcat.hathitrust.HathiTrustClientException;
import edu.tamu.tcat.hathitrust.htrc.features.simple.ExtractedFeatures;
import edu.tamu.tcat.hathitrust.htrc.features.simple.ExtractedFeaturesProvider;

public class ExtractedFeaturesTests
{
   @Test
   public void testFactory() throws Exception
   {
      try (MockExtractedFeaturesProvider mp = createProvider())
      {
         ExtractedFeaturesProvider p = mp;
         try (ExtractedFeatures feat = p.getExtractedFeatures("hvd.ah3d1a"))
         {
            String vid = feat.getVolumeId();
            System.out.println("Loaded: " + vid);
            
            System.out.println("Title: " + feat.getMetadata().title());
            
            ExtractedFeatures.ExtractedPageFeatures page = feat.getPage(48);
            ExtractedFeatures.ExtractedPagePartOfSpeechData bodyData = page.getBodyData();
            Set<String> toks = bodyData.tokens();
            toks.stream().sorted().forEach(tok ->
            {
               try {
                  System.out.println(tok + ": " + bodyData.getCount(tok));
               } catch (Exception e) {
                  e.printStackTrace();
               }
            });
         }
      }
   }
   
   private static MockExtractedFeaturesProvider createProvider()
   {
      return new MockExtractedFeaturesProvider(Paths.get("\\\\citd.tamu.edu\\citdfs\\archive\\HTRC_Dataset\\"));
   }
   
   static class MockExtractedFeaturesProvider implements ExtractedFeaturesProvider, AutoCloseable
   {
      private static final Logger debug = Logger.getLogger(MockExtractedFeaturesProvider.class.getName());
      private static final String TYPE_BASIC = "basic";
      private static final String TYPE_ADVANCED = "advanced";
      
      private final AtomicBoolean isDisposed = new AtomicBoolean(false);
      private final ConcurrentHashMap<String, MockExtractedFeatures> cache;
      private final Path root;
      
      private final ExecutorService exec;
      
      public MockExtractedFeaturesProvider(Path root)
      {
         this.root = root;
         cache = new ConcurrentHashMap<>();
         exec = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("mock ext feat %1$d").build());
      }
      
      @Override
      public String toString()
      {
         return "provider["+root+"]";
      }
      
      @Override
      public void close() throws Exception
      {
         // prevent any new cache entries from being created
         isDisposed.set(true);
         
         exec.shutdownNow();
         
         cache.forEachValue(1, ef ->
         {
            try {
               ef.close();
            } catch (Exception e) {
               debug.log(Level.SEVERE, "Error disposing ["+ef+"]", e);
            }
         });
         if (!cache.isEmpty())
         {
            debug.log(Level.SEVERE, "Provider had "+cache.size()+" dangling cache entries");
            cache.clear();
         }
      }
      
      public void closed(MockExtractedFeatures ch)
      {
         if (isDisposed.get())
            return;
         
         String vid = ch.getVolumeId();
         MockExtractedFeatures ef = cache.remove(vid);
         if (ef == null)
         {
            debug.log(Level.WARNING, "Extracted features closing but not in cache ["+vid+"]");
            return;
         }
      }
      
      /**
       * Given the volume-id, type (currently "basic" or "advanced"), and internal root path,
       * provide a {@link Path} to the requested .json.bz2 file if it exists.
       * 
       * @param htrcVolumeId
       * @param type
       * @return The requested path, or {@code null} if the file does not exist.
       * @throws HathiTrustClientException
       */
      private Path getArchivePath(String htrcVolumeId, String type) throws HathiTrustClientException
      {
         // Volume-id looks like "xxx.123456" and needs to be split into
         // "xxx", "12", "34", "56", "xxx.123456.basic.json.bz2" parts
         
         String[] strs = htrcVolumeId.split(Pattern.quote("."));
         String src = strs[0];
         String tail = strs[1];
         
         List<String> pairs = new ArrayList<>();
         for (int i=0; i<tail.length(); i+=2)
         {
            pairs.add(tail.substring(i, i+2));
         }
         
         Path p = root.resolve(type).resolve(src).resolve("pairtree_root");
         for (String part : pairs)
            p = p.resolve(part);
         
         p = p.resolve(tail);
         Path file = p.resolve(htrcVolumeId+"."+type+".json.bz2");
         if (!Files.exists(file))
            return null;
         
         return file;
      }
      
      @Override
      public ExtractedFeatures getExtractedFeatures(String htrcVolumeId) throws HathiTrustClientException
      {
         if (isDisposed.get())
            throw new IllegalStateException("Provider is disposed");
         
         Path basic = getArchivePath(htrcVolumeId, "basic");
         Path advanced = getArchivePath(htrcVolumeId, "advanced");
         
         // These are cheap enough to create and destroy if already in the cache
         MockExtractedFeatures ef = new MockExtractedFeatures(this, htrcVolumeId, basic, advanced);
         MockExtractedFeatures old = cache.putIfAbsent(htrcVolumeId, ef);
         if (old != null)
            // return 'old', don't bother calling "ef.close()"
            return old;
         
         ef.load(exec);
         return ef;
      }
   }
   
   static class MockExtractedFeatures implements ExtractedFeatures, ExtractedFeatures.Metadata
   {
      private static final Logger debug = Logger.getLogger(MockExtractedFeatures.class.getName());
      
      private final MockExtractedFeaturesProvider parent;
      private final String vid;
      private final Path basic;
      private final Path advanced;
      
      private Future<Map<String, ?>> basicData;
      private Future<Map<String, ?>> advancedData;
      
      public MockExtractedFeatures(MockExtractedFeaturesProvider parent,
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
         catch (Exception e)
         {
            debug.log(Level.SEVERE, "Failed loading ["+vid+"]", e);
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
         Map<String, ?> data = basicData.get(10, TimeUnit.SECONDS);
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
         Map<String, ?> data = advancedData.get(10, TimeUnit.SECONDS);
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
            throw new HathiTrustClientException("Failed accessing metadata [title] on ["+vid+"]", e);
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

      @Override
      public int pageCount() throws HathiTrustClientException
      {
         try
         {
            Number v = getMetaValue("pageCount", Number.class);
            if (v == null)
               throw new IllegalStateException("Missing value 'pageCount'");
            
            return v.intValue();
         }
         catch (Exception e)
         {
            throw new HathiTrustClientException("Failed accessing metadata [pageCount] on ["+vid+"]", e);
         }
      }

      @Override
      public ExtractedPageFeatures getPage(int page)
      {
         return new MockPage(this, page);
      }
   }
   
   private static class MockPage implements ExtractedFeatures.ExtractedPageFeatures
   {
      private final MockExtractedFeatures parent;
      private final int index;
      
      // cache basic/advanced data to improve performance of repeated access, such as for token POS counts
      //@GuardedBy("this")
      private Map<String, ?> pageDataBasic;
      //@GuardedBy("this")
      private Map<String, ?> pageDataAdvanced;
      
      public MockPage(MockExtractedFeatures parent, int index)
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
         return new MockPOS(this, "body");
      }
   }
   
   private static class MockPOS implements ExtractedFeatures.ExtractedPagePartOfSpeechData
   {
      private final MockPage parent;
      private final String section;

      public MockPOS(MockPage parent, String section)
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
