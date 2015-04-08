package edu.tamu.tcat.hathitrust.client.v1.basic;

import java.net.URI;
import java.time.temporal.TemporalAccessor;
import java.util.List;

import edu.tamu.tcat.hathitrust.Item;
import edu.tamu.tcat.hathitrust.MarcRecord;
import edu.tamu.tcat.hathitrust.Record;

public class BasicRecord implements Record
{
   private final String id;
   private final URI recordUri;
   private final List<String> titles;
   private final List<RecordIdentifier> recordIdents;
   private final List<TemporalAccessor> publishedDates;
   private final MarcRecord marc;
   private final List<Item> items;

   public BasicRecord(String id, URI recordUri,
                      List<String> titles,
                      List<RecordIdentifier> recordIdents,
                      List<TemporalAccessor> publishedDates,
                      MarcRecord marc,
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
      this.marc = null;
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
   public MarcRecord getMarcRecord()
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
      public String getItemId()
      {
         return itemId;
      }

   }

}
