package edu.tamu.tcat.hathitrust.basic.tests;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import edu.tamu.tcat.hathitrust.htrc.features.simple.impl.DefaultExtractedFeaturesProvider;
import edu.tamu.tcat.pairtree.Pairtree;

public class PairtreeTests
{
   private static final Logger debug = Logger.getLogger(PairtreeTests.class.getName());
   
   private static final String FILES_PATH_ROOT = "\\\\citd.tamu.edu\\citdfs\\archive\\HTRC_Dataset\\";
   
   // Use .properties file to gain semantics of comments and literals; potentially k=v mappings
   private static final String FILE_VOL_IDS = "res/volume_ids.properties";
   
   @Test
   @Ignore
   public void testRead() throws Exception
   {
      for (String str : loadIds())
      {
         String unc = Pairtree.uncleanId(str);
         debug.info("unclean " + str + " -> " + unc);
      }
   }
   
   private List<String> loadIds() throws Exception
   {
      List<String> ids = new ArrayList<>();
      try (BufferedReader reader = Files.newBufferedReader(Paths.get(FILE_VOL_IDS)))
      {
         String s;
         // ignore reading as a properties file and just process simple elements for now
         while ((s = reader.readLine()) != null)
         {
            s = s.trim();
            if (s.isEmpty())
               continue;
            // Skip commented lines
            if (s.charAt(0) == '#')
               continue;
            ids.add(s);
         }
      }
      return ids;
   }

   private static final ConsoleHandler ch = new ConsoleHandler();
   
   @BeforeClass
   public static void log()
   {
      // Use empty string to properly get the "global" logger
      Logger root = Logger.getLogger("");
      // flush all existing handlers
      Arrays.asList(root.getHandlers()).forEach(h -> root.removeHandler(h));
      
      // Add our custom console handler
      ch.setFormatter(new SimpleFormatter());
      ch.setLevel(Level.ALL);
      root.addHandler(ch);
      
      // Enable loggers for this test case
      Logger.getLogger(PairtreeTests.class.getName()).setLevel(Level.ALL);
      Logger.getLogger(Pairtree.class.getPackage().getName()).setLevel(Level.ALL);
      // Enable all loggers in the impl package for these test cases
      Logger.getLogger(DefaultExtractedFeaturesProvider.class.getPackage().getName()).setLevel(Level.ALL);
   }
   
   @AfterClass
   public static void endLog()
   {
      Logger.getLogger("").removeHandler(ch);
      Logger.getLogger(PairtreeTests.class.getName()).setLevel(Level.OFF);
      Logger.getLogger(Pairtree.class.getPackage().getName()).setLevel(Level.OFF);
      Logger.getLogger(DefaultExtractedFeaturesProvider.class.getPackage().getName()).setLevel(Level.OFF);
   }
}
