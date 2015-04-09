package edu.tamu.tcat.hathitrust.model;

import java.net.URI;
import java.time.LocalDate;

/**
 * A physical volume that was scanned for inclusion in HathiTrust. Each item belongs to
 * a single record, but a single record (e.g., the record for the journal <i>Nature</i>) may
 * have many items associated with it.
 *
 * @see http://www.hathitrust.org/bib_api for documentation of the HathiTrust Bibliographic API.
 */
public interface Item
{
   // IMPLEMENTATION NOTE: This will typically not have access to all data. It will need to
   // execute possibly time intensive queries to the underlying REST API layer.

   String getItemId();

   /**
    * @return The URL to this item in the pageturner interface. This is trivially derived from
    *       the htid at the moment, but is included here in the event that the handle URLs get
    *       more complex in the future.
    */
   URI getItemURL();

   /**
    * @return The nine-digit record number with which this particular item is attached.
    */
   String getRecordId();

   /**
    * @return The originating institution where this particular volume was digitized.
    */
   String getOriginatingInstitution();

   Record getRecord();

   /**
    * @return The rights code as used in the downloadable files, describing the copyright
    *       status of the item and what users in various locales are able to do with it.
    */
   RightsCode getRightsCode();

   /**
    * @return The date this item was ingested or last changed (because, e.g., the rights
    *       determination changed).
    */
   LocalDate getLastUpdate();

   /**
    * A key used to for sorting this item with respect to other items from the same record.
    * This is described as the enumeration/chronology of the item within the HahtiTrust
    * documentation and describes describing the item's place in a series. These are
    * commonly of the form, "vol. 3, n. 2 1993" or something similar.
    *
    * @return A string based key used for sorting. May be used as a label to assist users in
    *       understanding the nature of this item.
    */
   String getSortKey();

   /**
    * A textual description of the rights for a US-based user. This is trivially derived from
    * {@link #getRightsCode()}, but useful enough to the majority of likely users that it is
    * included here. Will be either "Limited (search only)" or "Full View."
    *
    * @return A description of how this item may be used.
    */
   String getRightsDisplayLabel();



//   Page getPage(int ix) throws IndexOutOfBoundsException;

//   "items":
//    [
//     {  "orig":"University of California",
//        "fromRecord":"000578050",
//        "htid":"uc1.b4405602",
//        "itemURL":"http:\/\/hdl.handle.net\/2027\/uc1.b4405602",
//        "rightsCode":"ic",
//        "lastUpdate":"20130805",
//        "enumcron":false,
//        "usRightsString":"Limited (search-only)"
//     },
}
