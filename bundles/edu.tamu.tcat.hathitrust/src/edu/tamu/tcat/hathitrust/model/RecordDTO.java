package edu.tamu.tcat.hathitrust.model;

import java.net.URI;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Map;

public class RecordDTO
{

   public static RecordDTO create(Record record)
   {
      return null;
   }

   public static Record instantiate(RecordDTO dto)
   {
      return null;
   }

   private class BasicRecord implements Record
   {

      @Override
      public String getId()
      {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public URI getRecordURL()
      {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public List<String> getTitles()
      {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public List<RecordIdentifier> getIdentifiers(IdType type)
      {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public List<TemporalAccessor> getPublishDates()
      {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public String getMarcRecordXML()
      {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public List<Item> getItems()
      {
         // TODO Auto-generated method stub
         return null;
      }
   }


   public String id;
   public String uri;
   public List<String> titles;
   public Map<String, List<String>> identifiers;
   public List<String> publicationDates;
   public String markXml;

}
