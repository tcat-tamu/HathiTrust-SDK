/*
 * Copyright 2015 Texas A&M Engineering Experiment Station
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.tamu.tcat.hathitrust.bibliography;

import java.net.URI;
import java.time.Year;
import java.util.List;
import java.util.Set;

/**
 *  A description of a bibliographic entity (a book, serial, etc) as provided by HathiTrust.
 *
 *  @see http://www.hathitrust.org/bib_api for documentation of the HathiTrust Bibliographic API.
 */
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
    * @return the identifiers associated with this record
    */
   List<RecordIdentifier> getIdentifiers();

   /**
    * @param type The type of identifier to return.
    * @return the identifiers associated with this record for the supplied identifier type.
    */
   List<RecordIdentifier> getIdentifiers(IdType type);

   /**
    * @return Publication dates associated with this entry.
    */
   List<Year> getPublishDates();

   /**
    * @return The MARC catalog record for this resource. Note that this may involve a call
    *    to the HathiTrust API and consequently may be a long-running request and fail unexpectedly.
    * @throws IllegalStateException If the marc record cannot be retrieved.
    */
   String getMarcRecordXML() throws IllegalStateException;

   /**
    * @param id The id of the item to check.
    * @return {@code true} if the identified item belongs to this record.
    */
   boolean hasItem(String id);

   /**
    * @param id The id of the item to retrieve
    * @return The requested item.
    * @throws IllegalArgumentException If the requested item is not a member of this
    */
   Item getItem(String id) throws IllegalArgumentException;

   /**
    * @return The ids of all {@link Item}s associated with this record.
    */
   Set<String> getItemIds();

   /**
    * @return A list of all {@link Item}s associated with this record.
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
// }
}
