package edu.tamu.tcat.hathitrust.client.v1.basic;

import java.net.URI;
import java.time.temporal.TemporalAccessor;
import java.util.List;

import edu.tamu.tcat.hathitrust.Item;
import edu.tamu.tcat.hathitrust.Record;

public class BasicRecord implements Record
{
   private final String id;
   private final URI recordUri;
   private final List<String> titles;
   private final List<RecordIdentifier> recordIdents;
   private final List<TemporalAccessor> publishedDates;
   private final String marc;
   private final List<Item> items;

   public BasicRecord(String id, URI recordUri,
                      List<String> titles,
                      List<RecordIdentifier> recordIdents,
                      List<TemporalAccessor> publishedDates,
                      String marc,
                      List<Item> items)
   {
      this.id = id;
      this.recordUri = recordUri;
      this.titles = titles;
      this.recordIdents = recordIdents;
      this.publishedDates = publishedDates;
      this.marc = marc;
      this.items = items;
   }

   // FIXME must supply a mechanism to retrieve MARC record
   public BasicRecord(String id, URI recordUri,
                      List<String> titles,
                      List<RecordIdentifier> recordIdents,
                      List<TemporalAccessor> publishedDates,
                      List<Item> items)
   {
      this.id = id;
      this.recordUri = recordUri;
      this.titles = titles;
      this.recordIdents = recordIdents;
      this.publishedDates = publishedDates;
      this.marc = null;    // FIXME
      this.items = items;
   }

   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public URI getRecordURL()
   {
      return recordUri;
   }

   @Override
   public List<String> getTitles()
   {
      return titles;
   }

   @Override
   public List<RecordIdentifier> getIdentifiers(IdType type)
   {
      return recordIdents;
   }

   @Override
   public List<TemporalAccessor> getPublishDates()
   {
      return publishedDates;
   }

   @Override
   public String getMarcRecordXML()
   {
      return marc;
   }

   @Override
   public List<Item> getItems()
   {
      return items;
   }

   public static class BasicRecordIdentifier implements RecordIdentifier
   {
      private final IdType idType;
      private final String id;
      private final String itemId;

      public BasicRecordIdentifier(IdType idType, String id) {
         this(idType, id, null);
      }

      public BasicRecordIdentifier(IdType idType, String id, String itemId)
      {
         this.idType = idType;
         this.id = id;
         this.itemId = itemId;
      }

      @Override
      public IdType getScheme()
      {
         return idType;
      }

      @Override
      public String getId()
      {
         return id;
      }

      @Override
      public String getRecordId()
      {
         return itemId;
      }

   }

}
