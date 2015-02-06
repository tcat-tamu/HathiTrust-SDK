package edu.tamu.tcat.hathitrust.client.v1.basic;

import java.net.URI;

import edu.tamu.tcat.hathitrust.Item;
import edu.tamu.tcat.hathitrust.Record;
import edu.tamu.tcat.hathitrust.RightsCode;

public class BasicItem implements Item
{
   private final String orig;
   private final String fromRecord;
   private final String htid;
   private final String itemURL;
   private final String rightsCode;
   private final String lastUpdate;
   private final String enumcron;
   private final String usRightsString;

   public BasicItem()
   {
      this.orig = "";
      this.fromRecord = "";
      this.htid = "";
      this.itemURL = "";
      this.rightsCode = "";
      this.lastUpdate = "";
      this.enumcron = "";
      this.usRightsString = "";
   }

   public BasicItem(String orig, String fromRecord, String htid,
                    String itemURL, String rightsCode, String lastUpdate,
                    String enumcrom, String usRightsString)
   {
      this.orig = orig;
      this.fromRecord = fromRecord;
      this.htid = htid;
      this.itemURL = itemURL;
      this.rightsCode = rightsCode;
      this.lastUpdate = lastUpdate;
      this.enumcron = enumcrom;
      this.usRightsString = usRightsString;
   }

   @Override
   public URI getItemURL()
   {
      return URI.create(itemURL);
   }

   @Override
   public String getOriginatingInstitution()
   {
      return fromRecord;
   }

   @Override
   public String getRecordId()
   {
      return htid;
   }

   @Override
   public Record getRecord()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public RightsCode getRightsCode()
   {
      // TODO Auto-generated method stub
      return null;
   }

}
