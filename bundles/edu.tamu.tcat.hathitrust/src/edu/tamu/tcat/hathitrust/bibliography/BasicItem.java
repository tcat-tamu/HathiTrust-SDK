package edu.tamu.tcat.hathitrust.bibliography;

import java.net.URI;
import java.time.LocalDate;

import edu.tamu.tcat.hathitrust.rights.RightsCode;

public class BasicItem implements Item
{
   private final String itemId;
   private final String recordId;
   private final URI itemUri;
   private final String orig;
   private final RightsCode rightsCode;
   private final LocalDate lastUpdate;
   private final String sortKey;
   private final String rightsLabel;

   public BasicItem(String itemId,
                    String recordId,
                    URI itemURL,
                    String orig,
                    RightsCode rightsCode,
                    LocalDate lastUpdate,
                    String enumcrom,
                    String usRightsString)
   {
      this.orig = orig;
      this.recordId = recordId;
      this.itemId = itemId;
      this.itemUri = itemURL;
      this.rightsCode = rightsCode;
      this.lastUpdate = lastUpdate;
      this.sortKey = enumcrom;
      this.rightsLabel = usRightsString;
   }

   @Override
   public String getItemId()
   {
      return itemId;
   }

   @Override
   public String getRecordId()
   {
      return recordId;
   }

   @Override
   public URI getItemURL()
   {
      return itemUri;
   }

   @Override
   public String getOriginatingInstitution()
   {
      return orig;
   }

   @Override
   public Record getRecord()
   {
      // TODO implement me
      throw new UnsupportedOperationException();
   }

   @Override
   public RightsCode getRightsCode()
   {
      return rightsCode;
   }

   @Override
   public LocalDate getLastUpdate()
   {
      return lastUpdate;
   }

   @Override
   public String getRightsDisplayLabel()
   {
      return rightsLabel;
   }

   @Override
   public String getSortKey()
   {
      return sortKey;
   }
}
