package edu.tamu.tcat.hathitrust.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import edu.tamu.tcat.hathitrust.model.Record.IdType;
import edu.tamu.tcat.hathitrust.model.Record.RecordIdentifier;

public final class RecordDTO
{
   private static final Logger logger = Logger.getLogger(RecordDTO.class.getName());

   public String id;
   public String uri;
   public List<String> titles;
   public Map<String, List<String>> identifiers;
   public List<String> publicationDates;
   public String marcXml;

   public static RecordDTO create(Record record)
   {
      RecordDTO dto = new RecordDTO();
      dto.id = record.getId();
      dto.uri = record.getRecordURL().toString();
      dto.titles = new ArrayList<>(record.getTitles());
      Map<String, List<String>> idents = new HashMap<>();
      for (RecordIdentifier identifier : record.getIdentifiers())
      {
         String key = identifier.getScheme().toString();
         if (!idents.containsKey(key))
         {
            idents.put(key, new ArrayList<>());
         }

         idents.get(key).add(identifier.getId());
      }
      dto.identifiers = idents;

      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy");
      dto.publicationDates = record.getPublishDates().parallelStream()
            .map(date -> date.format(formatter))
            .collect(Collectors.toList());
      dto.marcXml = record.getMarcRecordXML();

      // TODO add support for items?

      return dto;
   }

   public static Record instantiate(RecordDTO dto)
   {
      // FIXME need to supply source for items and Marc record
      try
      {
         URI uri = new URI(dto.uri);
         List<String> titles = new ArrayList<>(dto.titles);

         return new BasicRecord(dto.id, uri, titles, parseIdentifiers(dto), parseDates(dto),
               () -> dto.marcXml,
               () -> {
                  logger.log(Level.SEVERE, "Called unimplmeneted reference to get items");
                  return new ArrayList<>();
               });
      }
      catch (URISyntaxException e)
      {
         throw new IllegalArgumentException("Invalid record URI [" + dto.uri + "]", e);
      }
   }

   private static List<RecordIdentifier> parseIdentifiers(RecordDTO dto)
   {
      List<RecordIdentifier> identifiers = new ArrayList<>();
      dto.identifiers.forEach((type, ids) ->
      {
         IdType idType = IdType.valueOf(type);
         List<BasicRecordIdentifier> values = ids.parallelStream()
            .map(ident -> new BasicRecordIdentifier(idType, ident))
            .collect(Collectors.toList());
         identifiers.addAll(values);
      });
      return identifiers;
   }

   private static List<Year> parseDates(RecordDTO dto)
   {
      return dto.publicationDates.parallelStream()
                .map(strDate -> {
                   try
                   {
                      return Year.parse(strDate);
                   }
                   catch (Exception ex)
                   {
                      logger.log(Level.WARNING, "Failed to parse supplied date [" + strDate + "].", ex);
                      return null;
                   }
                })
                .filter(d -> d != null)
                .collect(Collectors.toList());
   }
}
