package edu.tamu.tcat.hathitrust.client.v1.basic;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

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
}
