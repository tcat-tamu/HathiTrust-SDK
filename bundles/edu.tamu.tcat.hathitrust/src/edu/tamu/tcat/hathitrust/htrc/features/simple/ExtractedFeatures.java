package edu.tamu.tcat.hathitrust.htrc.features.simple;

/**
 * The main abstraction for dealing with HTRC Extracted Feature data. This is a part of
 * the "simple" API, which provides direct access to the various fields and directly represents
 * a single version-set of the HTRC Extracted Feature schema.
 * <p>
 * Since the HTRC Extracted Feature data has independent schema versions for metadata,
 * basic features, and advanced features, this API represents all segments of the data over
 * some range of those versions.
 * <p>
 * This API applies to: Metadata schema version 1.2; Basic Features schema version 2.0; Advanced
 * Features schema version 2.0.
 */
public interface ExtractedFeatures extends AutoCloseable
{
   String getVolumeId();
   
   interface Metadata
   {
      String schemaVersion();
      String title();
   }
}
