package edu.tamu.tcat.hathitrust.features;

public interface ExtractedFeatures extends AutoCloseable
{
   // keep in mind
   //    use in streaming single properties out of volume/page
   //    look-ahead to pre-load upcoming pages or volumes
   
   String getVolumeId();
   
   interface Metadata
   {
      String schemaVersion();
      String title();
   }
}
