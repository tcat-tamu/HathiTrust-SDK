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

   @Test
   public void testIdCleaning() {
      Assert.assertEquals("ark+=13030=xt12t3", Pairtree.toCleanEncodedId("ark:/13030/xt12t3"));
      Assert.assertEquals("http+==n2t,info=urn+nbn+se+kb+repos-1", Pairtree.toCleanEncodedId("http://n2t.info/urn:nbn:se:kb:repos-1"));
      Assert.assertEquals("what-the-^2a@^3f#!^5e!^3f", Pairtree.toCleanEncodedId("what-the-*@?#!^!?"));
   }
   
   @Test
   public void testIdUncleaning() {
      Assert.assertEquals("ark:/13030/xt12t3", Pairtree.toRawDecodedId("ark+=13030=xt12t3"));
      Assert.assertEquals("http://n2t.info/urn:nbn:se:kb:repos-1", Pairtree.toRawDecodedId("http+==n2t,info=urn+nbn+se+kb+repos-1"));
      Assert.assertEquals("what-the-*@?#!^!?", Pairtree.toRawDecodedId("what-the-^2a@^3f#!^5e!^3f"));
   }
   
   @Test
   public void testtoPPath() {
      Assert.assertEquals(Paths.get("ab/cd"), Pairtree.toPPath("abcd"));
      Assert.assertEquals(Paths.get("ab/cd/ef/g"), Pairtree.toPPath("abcdefg"));
      Assert.assertEquals(Paths.get("12/-9/86/xy/4"), Pairtree.toPPath("12-986xy4"));
      
      Path base = Paths.get("/data");
      Path objDir = Paths.get("793842495");

      Assert.assertEquals(Paths.get("13/03/0_/45/xq/v_/79/38/42/49/5"), Pairtree.toPPath("13030_45xqv_793842495"));
      Assert.assertEquals(Paths.get("13/03/0_/45/xq/v_/79/38/42/49/5/793842495"), Pairtree.toPPath("13030_45xqv_793842495").resolve(objDir));
      Assert.assertEquals(Paths.get("/data/13/03/0_/45/xq/v_/79/38/42/49/5"), base.resolve(Pairtree.toPPath("13030_45xqv_793842495")));
      Assert.assertEquals(Paths.get("/data/13/03/0_/45/xq/v_/79/38/42/49/5/793842495"), base.resolve(Pairtree.toPPath("13030_45xqv_793842495")).resolve(objDir));
   }

   @Test
   public void testtoPPathIrregular() {
      Assert.assertEquals(Paths.get("abc/d"),       Pairtree.toPPath("abcd",      3));
      Assert.assertEquals(Paths.get("abc/def/g"),   Pairtree.toPPath("abcdefg",   3));
      Assert.assertEquals(Paths.get("12-/986/xy4"), Pairtree.toPPath("12-986xy4", 3));
      Assert.assertEquals(Paths.get("abcd"),        Pairtree.toPPath("abcd",      5));
      Assert.assertEquals(Paths.get("abcde/fg"),    Pairtree.toPPath("abcdefg",   5));
      Assert.assertEquals(Paths.get("12-98/6xy4"),  Pairtree.toPPath("12-986xy4", 5));
   }
   
   @Test
   public void testtoPPathWithIdCleaning() {
      Assert.assertEquals(Paths.get("ar/k+/=1/30/30/=x/t1/2t/3"), Pairtree.toPPath("ark:/13030/xt12t3"));
      Assert.assertEquals(Paths.get("ht/tp/+=/=n/2t/,i/nf/o=/ur/n+/nb/n+/se/+k/b+/re/po/s-/1"), Pairtree.toPPath("http://n2t.info/urn:nbn:se:kb:repos-1"));
      Assert.assertEquals(Paths.get("wh/at/-t/he/-^/2a/@^/3f/#!/^5/e!/^3/f"), Pairtree.toPPath("what-the-*@?#!^!?"));
   }

   @Test
   public void testEncapsulatingDir()
   {
      // Ensure encapsulating dir path is pruned
      Assert.assertEquals(Paths.get("ab"), Pairtree.getPpathBase(Paths.get("ab")));
      Assert.assertEquals(Paths.get("ab/cd"), Pairtree.getPpathBase(Paths.get("ab/cd")));
      Assert.assertEquals(Paths.get("ab/cd"), Pairtree.getPpathBase(Paths.get("ab/cd/")));
      Assert.assertEquals(Paths.get("ab/cd/ef/g"), Pairtree.getPpathBase(Paths.get("ab/cd/ef/g")));
      Assert.assertEquals(Paths.get("ab/cd/ef/g"), Pairtree.getPpathBase(Paths.get("ab/cd/ef/g/")));
      Assert.assertEquals(Paths.get("ab/cd/ef/g"), Pairtree.getPpathBase(Paths.get("ab/cd/ef/g/h")));
      Assert.assertEquals(Paths.get("ab/cd/ef/g"), Pairtree.getPpathBase(Paths.get("ab/cd/ef/g/h/")));
      Assert.assertEquals(Paths.get("ab/cd"), Pairtree.getPpathBase(Paths.get("ab/cd/efg")));
      Assert.assertEquals(Paths.get("ab/cd"), Pairtree.getPpathBase(Paths.get("ab/cd/efg/")));
      
      // Extract encapsulating dir from relative path
      Path relToObjDir = Paths.get("ab/cd/ef/g/h/");
      Path justObjDir = Pairtree.getPpathBase(relToObjDir).relativize(relToObjDir);
      Assert.assertEquals(Paths.get("h"), justObjDir);

      // Prune encapsulating dir from an absolute path
      Path base = Paths.get("/data");
      Assert.assertEquals(Paths.get("ab"),         Pairtree.getPpathBase(base.relativize(Paths.get("/data/ab"))));
      Assert.assertEquals(Paths.get("ab/cd/ef/g"), Pairtree.getPpathBase(base.relativize(Paths.get("/data/ab/cd/ef/g/h"))));
      
      // Extract encapsulating dir from absolute path
      Path relToObj2 = base.relativize(Paths.get("/data/ab/cd/ef/g/h"));
      Path justObj2 = Pairtree.getPpathBase(relToObj2).relativize(relToObj2);
      Assert.assertEquals(Paths.get("h"), justObj2);
   }

   @Test
   public void testMapToId()
   {
      Assert.assertEquals("ab", Pairtree.toObjectId(Paths.get("ab")));
      Assert.assertEquals("abcd", Pairtree.toObjectId(Paths.get("ab/cd")));
      Assert.assertEquals("abcd", Pairtree.toObjectId(Paths.get("ab/cd/")));
      Assert.assertEquals("abcdefg", Pairtree.toObjectId(Paths.get("ab/cd/ef/g")));
      Assert.assertEquals("abcdefg", Pairtree.toObjectId(Paths.get("ab/cd/ef/g/")));
      Assert.assertEquals("abcdefg", Pairtree.toObjectId(Paths.get("ab/cd/ef/g/h")));
      Assert.assertEquals("abcdefg", Pairtree.toObjectId(Paths.get("ab/cd/ef/g/h/")));
      Assert.assertEquals("abcd", Pairtree.toObjectId(Paths.get("ab/cd/efg")));
      Assert.assertEquals("abcd", Pairtree.toObjectId(Paths.get("ab/cd/efg/")));
      Assert.assertEquals("abcdefg", Pairtree.toObjectId(Paths.get("ab/cd/ef/g/h")));
      Assert.assertEquals("abcdefg", Pairtree.toObjectId(Paths.get("ab/cd/ef/g/h/")));

      Assert.assertEquals("12-986xy4", Pairtree.toObjectId(Paths.get("12/-9/86/xy/4")));

      Assert.assertEquals("13030_45xqv_793842495", Pairtree.toObjectId(Paths.get("13/03/0_/45/xq/v_/79/38/42/49/5")));
      Assert.assertEquals("13030_45xqv_793842495", Pairtree.toObjectId(Paths.get("13/03/0_/45/xq/v_/79/38/42/49/5/793842495")));
      
      Path base = Paths.get("/data");
      Assert.assertEquals("13030_45xqv_793842495", Pairtree.toObjectId(base.relativize(Paths.get("/data/13/03/0_/45/xq/v_/79/38/42/49/5"))));
      Assert.assertEquals("13030_45xqv_793842495", Pairtree.toObjectId(base.relativize(Paths.get("/data/13/03/0_/45/xq/v_/79/38/42/49/5/793842495"))));
   }

   @Test
   public void testMapToIdWithIdCleaning()
   {
      Assert.assertEquals("ark:/13030/xt12t3", Pairtree.toObjectId(Paths.get(("ar/k+/=1/30/30/=x/t1/2t/3"))));
      Assert.assertEquals("http://n2t.info/urn:nbn:se:kb:repos-1", Pairtree.toObjectId(Paths.get(("ht/tp/+=/=n/2t/,i/nf/o=/ur/n+/nb/n+/se/+k/b+/re/po/s-/1"))));
      Assert.assertEquals("what-the-*@?#!^!?", Pairtree.toObjectId(Paths.get(("wh/at/-t/he/-^/2a/@^/3f/#!/^5/e!/^3/f"))));
   }
   
   @Ignore
   @Test
   public void testProperEncapsulation()
   {
      //TODO: somehow craft tests for these?
      // This object is not properly encapsulated: ppath followed by "h" which is too short
      //Paths.get("ab/cd/ef/g/h/");
      // As a pair, the first object is not properly encapsulated: no encapsulation
      //Paths.get("ab/cd/ef/a.txt");
      
      // Properly encapsulated under "hij"
      //Paths.get("ab/cd/ef/g/hij/");
      // Properly encapsulated under "abcdefg", full ppath as encapsulation directory
      //Paths.get("ab/cd/ef/g/abcdefg/");
      // Properly encapsulated under "cdefg", abbreviated (tail) ppath as encapsulation directory
      //Paths.get("ab/cd/ef/g/abcdefg/");
   }
   
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
