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
import java.net.URISyntaxException;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.tamu.tcat.hathitrust.bibliography.BasicRecord;
import edu.tamu.tcat.hathitrust.bibliography.BasicRecordIdentifier;
import edu.tamu.tcat.hathitrust.bibliography.Item;
import edu.tamu.tcat.hathitrust.bibliography.Record;
import edu.tamu.tcat.hathitrust.bibliography.Record.IdType;
import edu.tamu.tcat.hathitrust.bibliography.Record.RecordIdentifier;

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
