package edu.tamu.tcat.hathitrust.client.v1.basic;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.tamu.tcat.hathitrust.model.BasicRecord;
import edu.tamu.tcat.hathitrust.model.BasicRecordIdentifier;
import edu.tamu.tcat.hathitrust.model.Item;
import edu.tamu.tcat.hathitrust.model.Record;
import edu.tamu.tcat.hathitrust.model.Record.IdType;
import edu.tamu.tcat.hathitrust.model.Record.RecordIdentifier;

public class BibligraphicRecordResult
{
   public Map<String, RecordDTO> records = new HashMap<>();
   public List<ItemDTO> items;

   /**
    * @param id
    * @return A Record instance for the record from this result having the supplied id.
    * @throws URISyntaxException
    */
   public Record createRecord(String id)
   {
      if (!records.containsKey(id))
         throw new IllegalArgumentException("The requested record [" + id + "] was not contained within this result set.");

      RecordDTO dto = records.get(id);

      URI recordUri = URI.create(dto.recordURL);   // silently throws if the server supplies bad URIs

      List<RecordIdentifier> identifiers = new ArrayList<>();
      identifiers.addAll(makeRecordIdentifiers(IdType.ISBN, dto.isbns));
      identifiers.addAll(makeRecordIdentifiers(IdType.ISSN, dto.issns));
      identifiers.addAll(makeRecordIdentifiers(IdType.LCCN, dto.lccns));
      identifiers.addAll(makeRecordIdentifiers(IdType.OCLC, dto.oclcs));

      List<Year> pubDates = dto.publishDates.stream()
         .filter(year -> year != null && year.matches("^\\d{4}$"))      // HACK: silently fail on non four digit years
         .map(Year::parse)
         .collect(Collectors.toList());

      List<Item> recordItems = items.stream()
            .filter(item -> item.fromRecord.equals(id))
             .map(ItemDTO::instantiate)
            .collect(Collectors.toList());

      return new BasicRecord(id, recordUri, dto.titles, identifiers, pubDates, () -> dto.marcXml, () -> recordItems);
   }

   private static List<RecordIdentifier> makeRecordIdentifiers(IdType identType, List<String> ids)
   {
      return ids.parallelStream()
                .map(id -> new BasicRecordIdentifier(identType, id))
                .collect(Collectors.toList());
   }
}
