package edu.tamu.tcat.hathitrust.basic.tests;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import edu.tamu.tcat.hathitrust.htrc.features.simple.ExtractedFeatures;
import edu.tamu.tcat.hathitrust.htrc.features.simple.ExtractedFeaturesProvider;
import edu.tamu.tcat.hathitrust.htrc.features.simple.impl.DefaultExtractedFeaturesProvider;

public class ExtractedFeaturesTests
{
   private static final String FILES_PATH_ROOT = "\\\\citd.tamu.edu\\citdfs\\archive\\HTRC_Dataset\\";
   private static final String FILE_VOL_IDS = "res/volume_ids.txt";
   
   @BeforeClass
   public static void log()
   {
      ConsoleHandler ch = new ConsoleHandler();
      ch.setFormatter(new SimpleFormatter());
      ch.setLevel(Level.ALL);
      // Use empty string to properly get the "global" logger
      Logger.getLogger("").addHandler(ch);
      
      // Enable all loggers in the package for these test cases
      Logger.getLogger(DefaultExtractedFeaturesProvider.class.getPackage().getName()).setLevel(Level.ALL);
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
            doTokens(id, p, (tok, bodyData) -> {
               try {
                  System.out.println(tok + ": " + bodyData.getCount(tok));
               } catch (Exception e) {
                  e.printStackTrace();
               }
            });
         }
      }
   }
   
   private void doTokens(String id, ExtractedFeaturesProvider p, BiConsumer<String, ExtractedFeatures.ExtractedPagePartOfSpeechData> f) throws Exception
   {
      try (ExtractedFeatures feat = p.getExtractedFeatures(id))
      {
         String vid = feat.getVolumeId();
         System.out.println("Loaded: " + vid);
         
         System.out.println("Title: " + feat.getMetadata().title());
         
         ExtractedFeatures.ExtractedPageFeatures page = feat.getPage(48);
         ExtractedFeatures.ExtractedPagePartOfSpeechData bodyData = page.getBodyData();
         Set<String> toks = bodyData.tokens();
         toks.stream().sorted().forEach(tok ->
         {
            f.accept(tok, bodyData);
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
         doTokens(TEST_FILE, mp, (tok, bodyData) -> {
            try {
               System.out.println(tok + ": " + bodyData.getCount(tok));
            } catch (Exception e) {
               e.printStackTrace();
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
