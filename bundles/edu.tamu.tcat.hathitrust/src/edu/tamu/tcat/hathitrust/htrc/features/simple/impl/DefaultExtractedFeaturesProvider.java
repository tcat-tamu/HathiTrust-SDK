package edu.tamu.tcat.hathitrust.htrc.features.simple.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import edu.tamu.tcat.hathitrust.HathiTrustClientException;
import edu.tamu.tcat.hathitrust.htrc.features.simple.ExtractedFeatures;
import edu.tamu.tcat.hathitrust.htrc.features.simple.ExtractedFeaturesProvider;

public class DefaultExtractedFeaturesProvider implements ExtractedFeaturesProvider, AutoCloseable
{
   private static final Logger debug = Logger.getLogger(DefaultExtractedFeaturesProvider.class.getName());
   private static final String TYPE_BASIC = "basic";
   private static final String TYPE_ADVANCED = "advanced";

   private final AtomicBoolean isDisposed = new AtomicBoolean(false);
   private final ConcurrentHashMap<String, DefaultExtractedFeatures> cache;
   private final Path root;

   private final ExecutorService exec;

   public DefaultExtractedFeaturesProvider(Path root)
   {
      this.root = root;
      cache = new ConcurrentHashMap<>();
      exec = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("def ext feat %1$d").build());
   }

   @Override
   public String toString()
   {
      return "provider[" + root + "]";
   }

   @Override
   public void close() throws Exception
   {
      debug.fine("Closing provider");
      // prevent any new cache entries from being created
      isDisposed.set(true);

      exec.shutdownNow();

      cache.forEachValue(1, ef ->
      {
         try
         {
            ef.close();
         }
         catch (Exception e)
         {
            debug.log(Level.SEVERE, "Error disposing [" + ef + "]", e);
         }
      });
      if (!cache.isEmpty())
      {
         debug.log(Level.SEVERE, "Provider had " + cache.size() + " dangling cache entries");
         cache.clear();
      }
   }

   public void closed(DefaultExtractedFeatures ch)
   {
      if (isDisposed.get())
         return;

      String vid = ch.getVolumeId();
      DefaultExtractedFeatures ef = cache.remove(vid);
      if (ef == null)
      {
         debug.log(Level.WARNING, "Extracted features closing but not in cache [" + vid + "]");
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
      for (int i = 0; i < tail.length(); i += 2)
      {
         pairs.add(tail.substring(i, i + 2));
      }

      Path p = root.resolve(type).resolve(src).resolve("pairtree_root");
      for (String part : pairs)
         p = p.resolve(part);

      p = p.resolve(tail);
      Path file = p.resolve(htrcVolumeId + "." + type + ".json.bz2");
      if (!Files.exists(file))
         return null;

      return file;
   }

   @Override
   public ExtractedFeatures getExtractedFeatures(String htrcVolumeId) throws HathiTrustClientException
   {
      if (isDisposed.get())
         throw new IllegalStateException("Provider is disposed");

      Path basic = getArchivePath(htrcVolumeId, TYPE_BASIC);
      Path advanced = getArchivePath(htrcVolumeId, TYPE_ADVANCED);

      // These are cheap enough to create and destroy if already in the cache
      DefaultExtractedFeatures ef = new DefaultExtractedFeatures(this, htrcVolumeId, basic, advanced);
      DefaultExtractedFeatures old = cache.putIfAbsent(htrcVolumeId, ef);
      if (old != null)
         // return 'old', don't bother calling "ef.close()"
         return old;

      ef.load(exec);
      return ef;
   }
}