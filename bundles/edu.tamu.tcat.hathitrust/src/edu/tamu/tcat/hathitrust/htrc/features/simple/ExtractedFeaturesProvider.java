package edu.tamu.tcat.hathitrust.htrc.features.simple;

import edu.tamu.tcat.hathitrust.HathiTrustClientException;

/**
 * A provider API for "simple" {@link ExtractedFeatures}. This is intended to be implemented
 * by a client application and provided via service or some other means to allow access to
 * extracted feature data.
 */
public interface ExtractedFeaturesProvider
{
   /**
    * Get an {@link ExtractedFeatures} for the given HTRC Volume Identifier.
    * 
    * @param htrcVolumeId
    * @return An {@link ExtractedFeatures} to access data for the volume. Does not
    *         return {@code null}
    * @throws HathiTrustClientException If no volume feature metadata can be found.
    */
   ExtractedFeatures getExtractedFeatures(String htrcVolumeId) throws HathiTrustClientException;
}
