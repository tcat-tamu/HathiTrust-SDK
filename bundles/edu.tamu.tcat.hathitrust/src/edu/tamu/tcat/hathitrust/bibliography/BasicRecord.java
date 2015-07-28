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
package edu.tamu.tcat.hathitrust.bibliography;

import java.net.URI;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import edu.tamu.tcat.hathitrust.bibliography.Record.IdType;
import edu.tamu.tcat.hathitrust.bibliography.Record.RecordIdentifier;

/**
 * Basic implementation of the {@link Record} interface.
 */
public final class BasicRecord implements Record
   {
      private final String id;
      private final URI recordUri;
      private final List<String> titles;
      private final List<RecordIdentifier> recordIdents;
      private final List<Year> publishedDates;

      // FIXME implement these
      private final Supplier<String> marc;
      private final Supplier<List<Item>> itemsSupplier;

      // FIXME must supply a mechanism to retrieve MARC record and items
      public BasicRecord(String id,
                         URI recordUri,
                         List<String> titles,
                         List<RecordIdentifier> recordIdents,
                         List<Year> publishedDates,
                         Supplier<String> marcXml,
                         Supplier<List<Item>> itemsSupplier)
      {
         this.id = id;
         this.recordUri = recordUri;
         this.titles = titles;
         this.recordIdents = recordIdents;
         this.publishedDates = publishedDates;
         this.marc = marcXml;
         this.itemsSupplier = itemsSupplier;
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
         return Collections.unmodifiableList(titles);
      }

      @Override
      public List<RecordIdentifier> getIdentifiers()
      {
         return Collections.unmodifiableList(recordIdents);
      }

      @Override
      public List<RecordIdentifier> getIdentifiers(IdType type)
      {
         // TODO seems like recordIdents should be a map?
         return recordIdents.parallelStream()
                  .filter(ident -> ident.getScheme().equals(type))
                  .collect(Collectors.toList());
      }

      @Override
      public List<Year> getPublishDates()
      {
         return publishedDates;
      }

      @Override
      public String getMarcRecordXML()
      {
         return marc.get();
      }

      private List<Item> items;
      private synchronized void loadItems()
      {
         if (items != null)
            return;

         try
         {
            this.items = new ArrayList<>(itemsSupplier.get());
         }
         catch (Exception ex)
         {
            throw new IllegalStateException("Failed to load items for record [" + this.id + "]", ex);
         }
      }

      @Override
      public List<Item> getItems()
      {
         loadItems();
         return Collections.unmodifiableList(items);
      }

      @Override
      public boolean hasItem(String itemId)
      {
         loadItems();
         Item item = items.parallelStream()
              .filter(candidate -> candidate.getItemId().equals(itemId))
              .findAny()
              .orElse(null);

         return item != null;
      }

      @Override
      public Item getItem(String itemId) throws IllegalArgumentException
      {
         loadItems();
         Item item = items.parallelStream()
               .filter(candidate -> candidate.getItemId().equals(itemId))
               .findAny()
               .orElse(null);

         if (item == null)
            throw new IllegalArgumentException("The requested item [" + itemId +"] is not associated with this record [" + id + "]");
         return item;
      }

      @Override
      public Set<String> getItemIds()
      {
         loadItems();
         return items.parallelStream()
                     .map(Item::getItemId)
                     .collect(Collectors.toSet());
      }
}