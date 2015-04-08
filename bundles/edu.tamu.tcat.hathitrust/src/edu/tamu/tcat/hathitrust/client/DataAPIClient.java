package edu.tamu.tcat.hathitrust.client;

public interface DataAPIClient
{
   public enum DataFormat
   {
      ebm,
      pdf,
      epub,
      xml,
      json
   }

   public enum ImageFormat
   {
      raw,
      png,
      jpeg
   }

   /**
    * HathiTrust DataAPIClient /aggregate provides a compressed file that contains 3 separate files for each page of a work.
    *    jp2 image file
    *    txt file containing the text of the image file.
    *    xml file containing coords of each word contained in the image file.
    * @param String representation of the HathiTrust unique item identifier
    * @return String name of the file retreived
    */
   String getAggregate(String htid);


   String getStructure(String htid, DataFormat format);

   /**
    * HathTrust DataAPIClient /volume is currently restricted to a project outside of our scope.
    * If the future it could support pdf | epub documents
    * @param htid
    * @param format
    * @return String identifying the name of the file.
    */
   String getVolume(String htid, DataFormat format);


   String getVolumeMeta(String htid, DataFormat format);


   String getPageMeta(String htid, DataFormat format, int seqNum);


   String getPageImage(String htid, ImageFormat format, int seqNum);


   String getPageOCR(String htid, int seqNum);


   String getPageCoordOCR(String htid, int seqNum);
}
