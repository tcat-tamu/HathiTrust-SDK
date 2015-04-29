package edu.tamu.tcat.hathitrust.basic.tests;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import edu.tamu.tcat.hathitrust.features.ExtractedFeatures;
import edu.tamu.tcat.hathitrust.features.ExtractedFeaturesProvider;

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
      public ExtractedFeatures getExtractedFeatures(String htrcVolumeId)
      {
         return null;
      }
   }
}
