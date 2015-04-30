package edu.tamu.tcat.hathitrust.basic.tests;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

import edu.tamu.tcat.hathitrust.HathiTrustClientException;
import edu.tamu.tcat.hathitrust.htrc.features.simple.ExtractedFeatures;
import edu.tamu.tcat.hathitrust.htrc.features.simple.ExtractedFeaturesProvider;

public class ExtractedFeaturesTests
{
   @Test
   public void testFactoryCreate() throws Exception
   {
      ExtractedFeaturesProvider p = createProvider();
      try (ExtractedFeatures feat = p.getExtractedFeatures("hvd.ah3d1a"))
      {
         String vid = feat.getVolumeId();
         System.out.println("Loaded: " + vid);
      }
   }
   
   private static ExtractedFeaturesProvider createProvider()
   {
      return new MockExtractedFeaturesProvider(Paths.get("\\\\citd.tamu.edu\\citdfs\\archive\\HTRC_Dataset\\"));
   }
   
   static class MockExtractedFeaturesProvider implements ExtractedFeaturesProvider
   {
      private static final String TYPE_BASIC = "basic";
      private static final String TYPE_ADVANCED = "advanced";
      
      private final Path root;
      
      public MockExtractedFeaturesProvider(Path root)
      {
         this.root = root;
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
         Path basic = getArchivePath(htrcVolumeId, "basic");
         Path advanced = getArchivePath(htrcVolumeId, "advanced");
         
         return new MockExtractedFeatures(this, htrcVolumeId, basic, advanced);
      }
   }
   
   static class MockExtractedFeatures implements ExtractedFeatures
   {
      private final String vid;
      private Path basic;
      private Path advanced;
      
      public MockExtractedFeatures(MockExtractedFeaturesProvider parent,
                                   String vid,
                                   Path basic,
                                   Path advanced)
      {
         this.vid = vid;
         this.basic = basic;
         this.advanced = advanced;
      }
      
      @Override
      public void close() throws Exception
      {
         // no-op
      }

      @Override
      public String getVolumeId()
      {
         return vid;
      }
   }
}
