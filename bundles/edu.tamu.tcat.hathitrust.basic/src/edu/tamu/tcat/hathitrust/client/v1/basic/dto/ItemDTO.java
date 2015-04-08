package edu.tamu.tcat.hathitrust.client.v1.basic.dto;

import java.net.URI;

import edu.tamu.tcat.hathitrust.model.Item;
import edu.tamu.tcat.hathitrust.model.Record;
import edu.tamu.tcat.hathitrust.model.RightsCode;

public class ItemDTO
{
   /** The originating institution -- where this particular volume was digitized. */
   public String orig;

   /**
    * The nine-digit record number to which this particular item is attached. It
    * will always be one of the records listed in the records section.
    */
   public String fromRecord;

   /** The HathiTrust volume id. */
   public String htid;

   /**
    * The URL to this item in the pageturner interface. This is trivially derived from the htid
    * at the moment, but is included here in the event that the handle URLs get more complex in
    * the future.
    */
   public String itemURL;

   /**
    * The rights code as used in the downloadable files, describing the copyright status of the
    * item and what users in various locales are able to do with it.
    */
   public String rightsCode;

   /**
    *  The date (YYYYMMDD) this item was ingested or last changed (because, e.g., the rights
    *  determination changed).
    */
   public String lastUpdate;

   /**
    *  The enumeration/chronology of the item, describing its place in a series. These are
    *  commonly of the form, "vol. 3, n. 2 1993" or something similar. Used to sort the
    *  items when present.
    */
   public String enumcron;

   /**
    *  A textual description of the rights for a US-based user. This is, again, trivially
    *  derived from the rightsCode, but useful enough to the majority of likely users that
    *  it is included here. Will be either "Limited (search only)" or "Full View."
    */
   public String usRightsString;


   public static Item instantiate(ItemDTO item)
   {
      URI url = URI.create(item.itemURL);
      ItemImpl itemImpl = new ItemImpl(item);

      return itemImpl;
   }

   private static final class ItemImpl implements Item
   {
      private final String itemId;
      private final String recordId;
      private final URI itemUrl;
      private final String institution;
      private RightsCode rights;

      ItemImpl(ItemDTO item)
      {
         this.itemUrl = URI.create(item.itemURL);
         this.institution = item.orig;
         this.itemId = item.htid;
         this.recordId = item.fromRecord;
         // item.lastUpdate;  // TODO parse this into LocalDate?
         // this.rights = rights; lookup
      }

      @Override
      public URI getItemURL()
      {
         return itemUrl;
      }

      @Override
      public String getOriginatingInstitution()
      {
         return institution;
      }

      @Override
      public String getRecordId()
      {
         return recordId;
      }

      @Override
      public RightsCode getRightsCode()
      {
         return rights;
      }

      @Override
      public Record getRecord()
      {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public String getItemId()
      {
         // TODO Auto-generated method stub
         return itemId;
      }
   }
}
