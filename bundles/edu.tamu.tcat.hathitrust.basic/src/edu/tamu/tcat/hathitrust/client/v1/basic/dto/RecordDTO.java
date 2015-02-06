package edu.tamu.tcat.hathitrust.client.v1.basic.dto;

import java.util.List;

public class RecordDTO
{
   String recordURL;
   List<String> titles;
   List<String> isbns;
   List<String> issns;
   List<String> lccns;
   List<String> oclcs;

   // TODO add dependency on Jackson, serialize directly as needed
   String marcXml;
}
