package edu.tamu.tcat.hathitrust.model;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class ItemDTO
{
   public String itemId;
   public String recordId;
   public String itemUri;
   public String originatingInstitution;
   public String rightsCode;
   public String lastUpdate;
   public String sortKey;
   public String rightsLabel;

   public static ItemDTO create(Item item)
   {
      throw new UnsupportedOperationException();
   }

   public static Item instantiate(ItemDTO dto, RightsCodeRegistry registry)
   {
      // HACK: at the moment, this is pretty brittle
      //       need to think about how to handle mal-formed data -
      //          throw or get as good an approximation as possible?
      //          that choice should be based on real use cases with API
      try
      {
         URI uri = URI.create(dto.itemUri);
         RightsCode rights = registry.find(dto.rightsCode);
         LocalDate date = LocalDate.parse(dto.lastUpdate, DateTimeFormatter.BASIC_ISO_DATE);
         return new BasicItem(dto.itemId,
                              dto.recordId,
                              uri,
                              dto.originatingInstitution,
                              rights,
                              date,
                              dto.sortKey,
                              dto.rightsLabel);
      }
      catch (Exception ex)
      {
         throw new IllegalArgumentException("Failed to correctly parse dto [" + dto + "]", ex);
      }
   }
}
