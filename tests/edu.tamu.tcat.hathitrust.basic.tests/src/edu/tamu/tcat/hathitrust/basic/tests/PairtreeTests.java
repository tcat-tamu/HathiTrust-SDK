package edu.tamu.tcat.hathitrust.basic.tests;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.junit.AfterClass;
import org.junit.Assert;
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
   
//   gov.loc.repository.pairtree.Pairtree pt = new gov.loc.repository.pairtree.Pairtree();
//
//   @Before
//   public void setup() {
//      pt.setSeparator('/');
//   }

//   @Test
//   public void testtoPPath() {
//      Assert.assertEquals(Paths.get("ab/cd"), Pairtree.toPPath("abcd"));
//      Assert.assertEquals(Paths.get("ab/cd/ef/g"), Pairtree.toPPath("abcdefg"));
//      Assert.assertEquals(Paths.get("12/-9/86/xy/4"), Pairtree.toPPath("12-986xy4"));

//      assertEquals("13/03/0_/45/xq/v_/79/38/42/49/5", pt.mapToPPath(null, "13030_45xqv_793842495", null));
//      assertEquals("13/03/0_/45/xq/v_/79/38/42/49/5/793842495", pt.mapToPPath(null, "13030_45xqv_793842495", "793842495"));
//      assertEquals("/data/13/03/0_/45/xq/v_/79/38/42/49/5", pt.mapToPPath("/data", "13030_45xqv_793842495", null));
//      assertEquals("/data/13/03/0_/45/xq/v_/79/38/42/49/5", pt.mapToPPath("/data/", "13030_45xqv_793842495", null));
//      assertEquals("/data/13/03/0_/45/xq/v_/79/38/42/49/5/793842495", pt.mapToPPath("/data", "13030_45xqv_793842495", "793842495"));
//   }

   @Test
   public void testIdCleaning() {
      Assert.assertEquals("ark+=13030=xt12t3", Pairtree.toCleanEncodedId("ark:/13030/xt12t3"));
      Assert.assertEquals("http+==n2t,info=urn+nbn+se+kb+repos-1", Pairtree.toCleanEncodedId("http://n2t.info/urn:nbn:se:kb:repos-1"));
      Assert.assertEquals("what-the-^2a@^3f#!^5e!^3f", Pairtree.toCleanEncodedId("what-the-*@?#!^!?"));
   }

//   @Test
//   public void testIdUncleaning() {
//      assertEquals("ark:/13030/xt12t3", pt.uncleanId("ark+=13030=xt12t3"));
//      assertEquals("http://n2t.info/urn:nbn:se:kb:repos-1", pt.uncleanId("http+==n2t,info=urn+nbn+se+kb+repos-1"));
//      assertEquals("what-the-*@?#!^!?", pt.uncleanId("what-the-^2a@^3f#!^5e!^3f"));
//   }
//
//   @Test
//   public void testtoPPathWithIdCleaning() {
//      assertEquals("ar/k+/=1/30/30/=x/t1/2t/3", pt.mapToPPath("ark:/13030/xt12t3"));
//
//      assertEquals("ht/tp/+=/=n/2t/,i/nf/o=/ur/n+/nb/n+/se/+k/b+/re/po/s-/1", pt.mapToPPath("http://n2t.info/urn:nbn:se:kb:repos-1"));
//      assertEquals("wh/at/-t/he/-^/2a/@^/3f/#!/^5/e!/^3/f", pt.mapToPPath("what-the-*@?#!^!?"));
//   }
//
//   @Test
//   public void testExtractEncapsulatingDir() throws InvalidPpathException {
//      assertNull(pt.extractEncapsulatingDirFromPpath("ab"));
//      assertNull(pt.extractEncapsulatingDirFromPpath("ab/cd"));
//      assertNull(pt.extractEncapsulatingDirFromPpath("ab/cd/"));
//      assertNull(pt.extractEncapsulatingDirFromPpath("ab/cd/ef/g"));
//      assertNull(pt.extractEncapsulatingDirFromPpath("ab/cd/ef/g/"));
//      assertEquals("h", pt.extractEncapsulatingDirFromPpath("ab/cd/ef/g/h"));
//      assertEquals("h", pt.extractEncapsulatingDirFromPpath("ab/cd/ef/g/h/"));
//      assertEquals("efg", pt.extractEncapsulatingDirFromPpath("ab/cd/efg"));
//      assertEquals("efg", pt.extractEncapsulatingDirFromPpath("ab/cd/efg/"));
//      assertEquals("h", pt.extractEncapsulatingDirFromPpath("ab/cd/ef/g/h"));
//      assertEquals("h", pt.extractEncapsulatingDirFromPpath("ab/cd/ef/g/h/"));
//
//      assertNull(pt.extractEncapsulatingDirFromPpath("/data", "/data/ab"));
//      assertNull(pt.extractEncapsulatingDirFromPpath("/data/", "/data/ab"));
//      assertEquals("h", pt.extractEncapsulatingDirFromPpath("/data", "/data/ab/cd/ef/g/h"));
//      assertEquals("h", pt.extractEncapsulatingDirFromPpath("/data/", "/data/ab/cd/ef/g/h"));
//
//   }
//
//   @Test
//   public void testMapToId() throws InvalidPpathException {
//      assertEquals("ab", pt.mapToId("ab"));
//      assertEquals("abcd", pt.mapToId("ab/cd"));
//      assertEquals("abcd", pt.mapToId("ab/cd/"));
//      assertEquals("abcdefg", pt.mapToId("ab/cd/ef/g"));
//      assertEquals("abcdefg", pt.mapToId("ab/cd/ef/g/"));
//      assertEquals("abcdefg", pt.mapToId("ab/cd/ef/g/h"));
//      assertEquals("abcdefg", pt.mapToId("ab/cd/ef/g/h/"));
//      assertEquals("abcd", pt.mapToId("ab/cd/efg"));
//      assertEquals("abcd", pt.mapToId("ab/cd/efg/"));
//      assertEquals("abcdefg", pt.mapToId("ab/cd/ef/g/h"));
//      assertEquals("abcdefg", pt.mapToId("ab/cd/ef/g/h/"));
//
//      assertEquals("ab/cd/ef/g", pt.mapToPPath("abcdefg"));
//      assertEquals("12-986xy4", pt.mapToId("12/-9/86/xy/4"));
//
//      assertEquals("13030_45xqv_793842495", pt.mapToId("13/03/0_/45/xq/v_/79/38/42/49/5"));
//      assertEquals("13030_45xqv_793842495", pt.mapToId("13/03/0_/45/xq/v_/79/38/42/49/5/793842495"));
//      assertEquals("13030_45xqv_793842495", pt.mapToId("/data", "/data/13/03/0_/45/xq/v_/79/38/42/49/5"));
//      assertEquals("13030_45xqv_793842495", pt.mapToId("/data/", "/data/13/03/0_/45/xq/v_/79/38/42/49/5"));
//      assertEquals("13030_45xqv_793842495", pt.mapToId("/data", "/data/13/03/0_/45/xq/v_/79/38/42/49/5/793842495"));
//   }
//
//
//   @Test(expected=gov.loc.repository.pairtree.Pairtree.InvalidPpathException.class)
//   public void testInvalidExtractEncapsulatingDir1() throws InvalidPpathException {
//      pt.extractEncapsulatingDirFromPpath("abc");
//   }
//
//   @Test(expected=gov.loc.repository.pairtree.Pairtree.InvalidPpathException.class)
//   public void testInvalidExtractEncapsulatingDir2() throws InvalidPpathException {
//      pt.extractEncapsulatingDirFromPpath("ab/cdx/efg/");
//   }
//
//   @Test
//   public void testMapToIdWithIdCleaning() throws InvalidPpathException {
//      assertEquals("ark:/13030/xt12t3", pt.mapToId("ar/k+/=1/30/30/=x/t1/2t/3"));
//      assertEquals("http://n2t.info/urn:nbn:se:kb:repos-1", pt.mapToId("ht/tp/+=/=n/2t/,i/nf/o=/ur/n+/nb/n+/se/+k/b+/re/po/s-/1"));
//      assertEquals("what-the-*@?#!^!?", pt.mapToId("wh/at/-t/he/-^/2a/@^/3f/#!/^5/e!/^3/f"));
//   }
   
   @Test
   @Ignore
   public void testRead() throws Exception
   {
      for (String str : loadIds())
      {
         Path p = Pairtree.toPPath(str);
         String unc = Pairtree.toRawDecodedId(str);
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
