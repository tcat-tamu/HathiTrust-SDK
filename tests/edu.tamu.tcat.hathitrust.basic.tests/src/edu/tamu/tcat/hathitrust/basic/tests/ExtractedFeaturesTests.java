package edu.tamu.tcat.hathitrust.basic.tests;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
   
   static class MockExtractedFeatures implements ExtractedFeatures
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
      
      public void load(ExecutorService exec)
      {
         basicData = exec.submit(() ->
         {
            try
            {
               Map<?,?> value = null;
               try (InputStream str = Files.newInputStream(basic);
                    BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(str))
               {
                  ObjectMapper mapper = new ObjectMapper();
                  value = mapper.readValue(bzIn, Map.class);
               }
               
               Map<String, ?> data = (Map)value;
               Map<String, ?> features = (Map)data.get("features");
               if (features == null)
                  throw new HathiTrustClientException("Basic data missing element 'features'");
                  
               Object sv = features.get("schemaVersion");
               if (!Objects.equals(sv, ExtractedFeatures.schemaVersionBasic))
                  throw new HathiTrustClientException("Unexpected schema version ["+sv+"] expecting ["+ExtractedFeatures.schemaVersionBasic+"]");
               
               return data;
               
               // open basic path as a bz2
               // use Jackson to parse into raw data vehicles
               // validate schema
            }
            catch (Exception e)
            {
               debug.log(Level.SEVERE, "Failed loading ["+vid+"]", e);
               throw e;
            }
         });
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
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public int pageCount()
      {
         // TODO Auto-generated method stub
         return 0;
      }

      @Override
      public ExtractedPageFeatures getPage(int page)
      {
         // TODO Auto-generated method stub
         return null;
      }
   }
}
