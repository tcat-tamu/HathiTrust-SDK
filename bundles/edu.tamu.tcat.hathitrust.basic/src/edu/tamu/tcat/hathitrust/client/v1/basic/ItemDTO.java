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
package edu.tamu.tcat.hathitrust.client.v1.basic;

import java.net.URI;
import java.time.LocalDate;

import edu.tamu.tcat.hathitrust.bibliography.Item;
import edu.tamu.tcat.hathitrust.bibliography.Record;
import edu.tamu.tcat.hathitrust.rights.RightsCode;

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
      public String getItemId()
      {
         return itemId;
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
         throw new UnsupportedOperationException();
      }
      @Override
      public LocalDate getLastUpdate()
      {
         // TODO Auto-generated method stub
         return null;
      }
      @Override
      public String getRightsDisplayLabel()
      {
         // TODO Auto-generated method stub
         return null;
      }
      @Override
      public String getSortKey()
      {
         // TODO Auto-generated method stub
         return null;
      }
   }
}
