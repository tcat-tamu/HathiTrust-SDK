package edu.tamu.tcat.hathitrust.client.v1.basic.dto;

import java.net.URI;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.tamu.tcat.hathitrust.client.v1.basic.BasicRecord;
import edu.tamu.tcat.hathitrust.client.v1.basic.BasicRecord.BasicRecordIdentifier;
import edu.tamu.tcat.hathitrust.model.Item;
import edu.tamu.tcat.hathitrust.model.Record;

public class RecordDTO
{
   public String recordURL;
   public List<String> titles;
   public List<String> isbns;
   public List<String> issns;
   public List<String> lccns;
   public List<String> oclcs;
   public List<String> publishDates;
   // TODO add dependency on Jackson, serialize directly as needed
   @JsonProperty("marc-xml")
   public String marcXml;

   public static Record instantiate(Entry<String, RecordDTO> recordEntry, List<ItemDTO> items)
   {
      RecordImpl recordImpl = new RecordImpl(recordEntry, items);
      return recordImpl;
   }

   private static final class RecordImpl implements Record
   {
      private final String id;
      private final URI recordUri;
      private final List<String> titles;
      private final List<RecordIdentifier> recordIdents = new ArrayList<>();
      private final List<TemporalAccessor> publishedDates;
      private final String marc;
      private final List<Item> items;

      RecordImpl(Map.Entry<String, RecordDTO> recordEntry, List<ItemDTO> items)
      {
         RecordDTO record = recordEntry.getValue();
         this.id = recordEntry.getKey();
         this.recordUri = URI.create(record.recordURL);
         this.titles = record.titles;
         this.recordIdents.addAll(addRecordIdents(record.isbns, IdType.ISBN));
         this.recordIdents.addAll(addRecordIdents(record.issns, IdType.ISSN));
         this.recordIdents.addAll(addRecordIdents(record.lccns, IdType.LCCN));
         this.recordIdents.addAll(addRecordIdents(record.oclcs, IdType.OCLC));
         // TODO: Transform the string date into a date
         this.publishedDates = new ArrayList<>();
         // TODO: MarcRecord has not been implemented, currently marc is a string
         this.marc = null;
         // TODO: items are currently another class
         this.items = addItems(items);
      }

      List<Item> addItems(List<ItemDTO> itemDtos)
      {
         List<Item> items = new ArrayList<>();
         for(ItemDTO item : itemDtos)
         {
            items.add(ItemDTO.instantiate(item));
         }
         return items;
      }

      List<RecordIdentifier> addRecordIdents(List<String> identNums, IdType identType)
      {
         List<RecordIdentifier> recordIdents = new ArrayList<>();
         for(String identNum : identNums)
         {
            BasicRecord.BasicRecordIdentifier recordIdent = new BasicRecordIdentifier(identType, identNum);
            recordIdents.add(recordIdent);
         }

         return recordIdents;
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
         List<RecordIdentifier> idents = new ArrayList<>();
         for(RecordIdentifier recordIdent : recordIdents)
         {
            if(recordIdent.getScheme().equals(type))
               idents.add(recordIdent);
         }
         return idents;
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
         return this.items;
      }
   }
}
