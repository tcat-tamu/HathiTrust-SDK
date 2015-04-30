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
      private Path root;
      
      public MockExtractedFeaturesProvider(Path root)
      {
         this.root = root;
      }
      
      @Override
      public ExtractedFeatures getExtractedFeatures(String htrcVolumeId) throws HathiTrustClientException
      {
         String[] strs = htrcVolumeId.split(Pattern.quote("."));
         String src = strs[0];
         String tail = strs[1];
         
         List<String> pairs = new ArrayList<>();
         for (int i=0; i<tail.length(); i+=2)
         {
            pairs.add(tail.substring(i, i+2));
         }
         
         Path p = root.resolve("basic").resolve(src).resolve("pairtree_root");
         for (String part : pairs)
            p = p.resolve(part);
         
         p = p.resolve(tail);
         Path file = p.resolve(htrcVolumeId+".basic.json.bz2");
         if (!Files.exists(file))
         {
            throw new IllegalStateException("not exist: " + file);
         }
         
         
         return new MockExtractedFeatures(htrcVolumeId);
      }
   }
   
   static class MockExtractedFeatures implements ExtractedFeatures
   {
      private final String vid;
      
      public MockExtractedFeatures(String vid)
      {
         this.vid = vid;
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
