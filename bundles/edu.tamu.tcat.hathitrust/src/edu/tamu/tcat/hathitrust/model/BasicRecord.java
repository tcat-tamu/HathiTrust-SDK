package edu.tamu.tcat.hathitrust.model;

import java.net.URI;
import java.time.Year;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

      @Override
      public List<Item> getItems()
      {
         return itemsSupplier.get();
      }
}