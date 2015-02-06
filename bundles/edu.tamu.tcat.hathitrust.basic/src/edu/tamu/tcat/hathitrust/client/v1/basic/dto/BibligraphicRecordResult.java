package edu.tamu.tcat.hathitrust.client.v1.basic.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BibligraphicRecordResult
{

   public Map<String, RecordDTO> records = new HashMap<>();
   public List<ItemDTO> items;

}
