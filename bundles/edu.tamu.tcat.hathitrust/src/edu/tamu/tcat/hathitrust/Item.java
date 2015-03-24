package edu.tamu.tcat.hathitrust;

import java.net.URI;

/**
 * A physical volume that was scanned for inclusion in HathiTrust. Each item belongs to
 * a single record, but a single record (e.g., the record for the journal <i>Nature</i>) may
 * have many items associated with it.
 */
public interface Item
{
   // IMPLEMENTATION NOTE: This will typically not have access to all data. It will need to
   // execute possibly time intensive queries to the underlying REST API layer.

   String getItemId();

   URI getItemURL();

   String getOriginatingInstitution();

   String getRecordId();

   Record getRecord();

   RightsCode getRightsCode();



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
