package edu.tamu.tcat.hathitrust;

import java.net.URI;
import java.time.temporal.TemporalAccessor;
import java.util.List;

public interface Record
{
   public enum IdType {
      ISBN, ISSN, OCLC, LCCN, HTID, RECORDNUMBER
   }

   /**
    * A identifier for a record, according to some bibliographic identification scheme.
    */
   interface RecordIdentifier
   {
      /**
       * @return The identification scheme for this id (for example, ISBN, ISSN, OCLC).
       */
      IdType getScheme();

      /**
       * @return The value for this identifier.
       */
      String getId();

      /**
       * @return The particular item ID (if any) for this record
       */
      String getItemId();
   }

   /**
    * @return The 9-digit HathiTrust identifier for this record.
    */
   String getId();

   /**
    * @return The URL for this record at HathiTrust.
    */
   URI getRecordURL();

   /**
    * @return The list of titles associated with this record, for sanity checking. This list
    *       includes the standard (MARC field 245) title with and without leading articles,
    *       and any vernacular (foreign language) titles provided in the record (MARC field 880).
    */
   List<String> getTitles();

   /**
    * @param type The type of identifier to return.
    * @return the identifiers associated with this record for the supplied identifier type.
    */
   List<RecordIdentifier> getIdentifiers(IdType type);

   /**
    * @return Publication dates associated with this entry.
    */
   List<TemporalAccessor> getPublishDates();

   /**
    * @return The MARC catalog record for this resource.
    */
   MarcRecord getMarcRecord();

   /**
    * @return
    */
   List<Item> getItems();


// "records": {
//     "000578050": {
//          "recordURL":"http:\/\/catalog.hathitrust.org\/Record\/000578050",
//          "titles":["Infinite series."],
//          "isbns":["9780030110405","9780030110405"],
//          "issns":[],
//          "oclcs":["424023"],
//          "lccns":["62009520"],
//          "publishDates":["1962"]
//     }
// },
}
