package edu.tamu.tcat.hathitrust.client.v1.basic;

import java.time.Year;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.tamu.tcat.hathitrust.bibliography.Record;

public class RecordDTO
{
   private static final Logger logger = Logger.getLogger(RecordDTO.class.getName());

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

   public static Record instantiate(String id, RecordDTO dto)
   {
      // FIXME need to supply source for items and Marc record
//      try
//      {
//         URI uri = new URI(dto.recordURL);
//         List<String> titles = new ArrayList<>(dto.titles);
//
//         return new BasicRecord(id, uri, titles, parseIdentifiers(dto), parseDates(dto),
//               () -> dto.marcXml,
//               () -> {
//                  logger.log(Level.SEVERE, "Called unimplmeneted reference to get items");
//                  return new ArrayList<>();
//               });
//      }
//      catch (URISyntaxException e)
//      {
//         throw new IllegalArgumentException("Invalid record URI [" + dto.recordURL + "]", e);
//      }
      throw new UnsupportedOperationException();
   }

   private static List<Year> parseDates(RecordDTO dto)
   {
      return dto.publishDates.parallelStream()
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
