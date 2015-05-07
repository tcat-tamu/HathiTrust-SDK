package edu.tamu.tcat.hathitrust.basic.tests;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import edu.tamu.tcat.hathitrust.htrc.features.simple.ExtractedFeatures;
import edu.tamu.tcat.hathitrust.htrc.features.simple.ExtractedFeaturesProvider;
import edu.tamu.tcat.hathitrust.htrc.features.simple.impl.DefaultExtractedFeaturesProvider;

public class ExtractedFeaturesTests
{
   private static final Logger debug = Logger.getLogger(ExtractedFeaturesTests.class.getName());
   
   private static final String FILES_PATH_ROOT = "\\\\citd.tamu.edu\\citdfs\\archive\\HTRC_Dataset\\";
   private static final String FILE_VOL_IDS = "res/volume_ids.txt";
   
   private static ConsoleHandler ch = new ConsoleHandler();
   
   @BeforeClass
   public static void log()
   {
      ch.setFormatter(new SimpleFormatter());
      ch.setLevel(Level.ALL);
      // Use empty string to properly get the "global" logger
      Logger.getLogger("").addHandler(ch);
      
      // Enable loggers for this test case
      Logger.getLogger(ExtractedFeaturesTests.class.getName()).setLevel(Level.ALL);
      // Enable all loggers in the impl package for these test cases
      Logger.getLogger(DefaultExtractedFeaturesProvider.class.getPackage().getName()).setLevel(Level.ALL);
   }
   
   @AfterClass
   public static void endLog()
   {
      Logger.getLogger("").removeHandler(ch);
      Logger.getLogger(ExtractedFeaturesTests.class.getName()).setLevel(Level.OFF);
      Logger.getLogger(DefaultExtractedFeaturesProvider.class.getPackage().getName()).setLevel(Level.OFF);
   }
   
   @Test
   @Ignore
   public void testGrab() throws Exception
   {
      List<String> ids = new ArrayList<>();
      try (BufferedReader reader = Files.newBufferedReader(Paths.get(FILE_VOL_IDS)))
      {
         String s;
         while ((s = reader.readLine()) != null)
         {
            s = s.trim();
            if (s.isEmpty())
               continue;
            ids.add(s);
         }
      }
      
      try (DefaultExtractedFeaturesProvider mp = createProvider())
      {
         ExtractedFeaturesProvider p = mp;
         for (String id : ids)
         {
            doTokens(id, p, (pkg) -> {
               try {
                  debug.info(pkg.token + ": " + pkg.bodyData.getCount(pkg.token));
               } catch (Exception e) {
                  debug.log(Level.SEVERE, "Error", e);
               }
            });
         }
      }
   }
   
   /**
    * An encapsulation to use in test cases
    */
   static class Pkg
   {
      String token;
      ExtractedFeatures.ExtractedPagePartOfSpeechData bodyData;
      
      Pkg(String t,
          ExtractedFeatures.ExtractedPagePartOfSpeechData p)
      {
         token = t;
         bodyData = p;
      }
   }
   
   private void doTokens(String id, Integer pageNumber, ExtractedFeaturesProvider p, Consumer<Pkg> f) throws Exception
   {
      try (ExtractedFeatures feat = p.getExtractedFeatures(id))
      {
         String vid = feat.getVolumeId();
         
         debug.info("Processing: " + vid);
         
         debug.info("Title: " + feat.getMetadata().title());
         
         ExtractedFeatures.ExtractedPageFeatures page = feat.getPage(48);
         ExtractedFeatures.ExtractedPagePartOfSpeechData bodyData = page.getBodyData();
         Set<String> toks = bodyData.tokens();
         toks.stream().sorted().forEach(tok ->
         {
            f.accept(new Pkg(tok, bodyData));
         });
      }
   }
   
   @Test
   @Ignore
   public void testFactory() throws Exception
   {
      String TEST_FILE = "hvd.ah3d1a";
      try (DefaultExtractedFeaturesProvider mp = createProvider())
      {
         doTokens(TEST_FILE, mp, pkg -> {
            try {
               debug.info(pkg.token + ": " + pkg.bodyData.getCount(pkg.token));
            } catch (Exception e) {
               debug.log(Level.SEVERE, "Error", e);
            }
         });
      }
   }
   
   // Return the actual type because the interface type is not AutoCloseable
   private static DefaultExtractedFeaturesProvider createProvider()
   {
      return new DefaultExtractedFeaturesProvider(Paths.get(FILES_PATH_ROOT));
   }
}
