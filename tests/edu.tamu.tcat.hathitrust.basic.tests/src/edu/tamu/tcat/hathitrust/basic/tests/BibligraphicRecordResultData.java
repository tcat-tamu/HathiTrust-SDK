package edu.tamu.tcat.hathitrust.basic.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.tamu.tcat.hathitrust.Record;
import edu.tamu.tcat.hathitrust.client.v1.basic.dto.ItemDTO;
import edu.tamu.tcat.hathitrust.client.v1.basic.dto.RecordDTO;

public class BibligraphicRecordResultData
{
//   private final BibligraphicRecordResult bibRecord;

   public BibligraphicRecordResultData()
   {
   }

   private Map<String,RecordDTO> buildRecordDTO()
   {
      String recordNum = "012392537";
      RecordDTO record = new RecordDTO();
      record.isbns = new ArrayList<>();
      record.issns = new ArrayList<>();

      record.lccns = new ArrayList<>();
      record.lccns.add("a31001072");

      record.oclcs = new ArrayList<>();
      record.oclcs.add("379388464");

      record.marcXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><collection><record><leader>00925cam a2200205   4500</leader>"
                     + "<controlfield tag=\"001\">012392537</controlfield><controlfield tag=\"003\">MiAaHDL</controlfield>"
                     + "<controlfield tag=\"005\">20130625000000.0</controlfield><controlfield tag=\"006\">m        d        </controlfield>"
                     + "<controlfield tag=\"007\">cr bn ---auaua</controlfield><controlfield tag=\"008\">900820s1796    nyu           00010 eng c</controlfield>"
                     + "<datafield tag=\"010\" ind1=\" \" ind2=\" \">"
                        + "<subfield code=\"a\">a 31001072</subfield></datafield>"
                     + "<datafield tag=\"035\" ind1=\" \" ind2=\" \">"
                        + "<subfield code=\"a\">sdr-ucd000142266</subfield></datafield>"
                     + "<datafield tag=\"035\" ind1=\" \" ind2=\" \">"
                        + "<subfield code=\"a\">(OCoLC)379388464</subfield></datafield>"
                     + "<datafield tag=\"090\" ind1=\" \" ind2=\" \">"
                        + "<subfield code=\"a\">BL2742</subfield>"
                        + "<subfield code=\"b\">.W5</subfield></datafield>"
                     + "<datafield tag=\"100\" ind1=\"1\" ind2=\" \">"
                        + "<subfield code=\"a\">Winchester, Elhanan,</subfield>"
                        + "<subfield code=\"d\">1751-1797.</subfield></datafield>"
                     + "<datafield tag=\"245\" ind1=\"1\" ind2=\"2\">"
                        + "<subfield code=\"a\">A defence of revelation,</subfield>"
                        + "<subfield code=\"b\">in ten letters to Thomas Paine; being an answer to his first part of the Age of Reason.</subfield>"
                        + "<subfield code=\"c\">By Elhanan Winchester ...</subfield></datafield>"
                     + "<datafield tag=\"260\" ind1=\"0\" ind2=\" \">"
                        + "<subfield code=\"a\">First printed at New-York:</subfield>"
                        + "<subfield code=\"a\">London:</subfield><subfield code=\"b\">Re-printed for the editor, by T. Gillet, and sold by T. A. Teulon; [etc., etc.]</subfield>"
                        + "<subfield code=\"c\">1796.</subfield></datafield>"
                     + "<datafield tag=\"300\" ind1=\" \" ind2=\" \">"
                        + "<subfield code=\"a\">viii, 113 p.</subfield>"
                        + "<subfield code=\"c\">22 cm.</subfield></datafield>"
                     + "<datafield tag=\"538\" ind1=\" \" ind2=\" \">"
                        + "<subfield code=\"a\">Mode of access: Internet.</subfield></datafield>"
                     + "<datafield tag=\"600\" ind1=\"1\" ind2=\"0\">"
                        + "<subfield code=\"a\">Paine, Thomas,</subfield>"
                        + "<subfield code=\"d\">1737-1809.</subfield><subfield code=\"t\">Age of reason.</subfield></datafield>"
                     + "<datafield tag=\"690\" ind1=\" \" ind2=\"4\">"
                        + "<subfield code=\"a\">1796.</subfield>"
                        + "<subfield code=\"%\">CHR.</subfield></datafield>"
                     + "<datafield tag=\"797\" ind1=\"1\" ind2=\"1\">"
                        + "<subfield code=\"a\">England.</subfield>"
                        + "<subfield code=\"c\">London.</subfield>"
                        + "<subfield code=\"d\">1796.</subfield>"
                        + "<subfield code=\"%\">IMP</subfield></datafield>"
                     + "<datafield tag=\"797\" ind1=\"1\" ind2=\"1\">"
                        + "<subfield code=\"a\">United States.</subfield>"
                        + "<subfield code=\"b\">New York.</subfield>"
                        + "<subfield code=\"c\">New York.</subfield>"
                        + "<subfield code=\"d\">1796.</subfield>"
                        + "<subfield code=\"%\">IMP</subfield></datafield>"
                     + "<datafield tag=\"CID\" ind1=\" \" ind2=\" \">"
                        + "<subfield code=\"a\">012392537</subfield></datafield>"
                     + "<datafield tag=\"DAT\" ind1=\"0\" ind2=\" \">"
                        + "<subfield code=\"a\">20130531193230.0</subfield>"
                        + "<subfield code=\"b\">20130625000000.0</subfield></datafield>"
                     + "<datafield tag=\"DAT\" ind1=\"1\" ind2=\" \">"
                        + "<subfield code=\"a\">20130715163250.0</subfield>"
                        + "<subfield code=\"b\">2013-08-07T13:20:40Z</subfield></datafield>"
                     + "<datafield tag=\"DAT\" ind1=\"2\" ind2=\" \">"
                        + "<subfield code=\"a\">2013-08-01T06:25:05Z</subfield></datafield>"
                     + "<datafield tag=\"CAT\" ind1=\" \" ind2=\" \">"
                        + "<subfield code=\"a\">SDR-UCD</subfield>"
                        + "<subfield code=\"d\">EX LIBRIS - ALEPH</subfield>"
                        + "<subfield code=\"l\">loader.pl-002-002</subfield></datafield>"
                     + "<datafield tag=\"FMT\" ind1=\" \" ind2=\" \">"
                        + "<subfield code=\"a\">BK</subfield></datafield>"
                     + "<datafield tag=\"HOL\" ind1=\" \" ind2=\" \">"
                        + "<subfield code=\"0\">sdr-ucd000142266</subfield>"
                        + "<subfield code=\"a\">uc1</subfield>"
                        + "<subfield code=\"b\">SDR</subfield>"
                        + "<subfield code=\"c\">UCD</subfield>"
                        + "<subfield code=\"p\">uc1.31175001466104</subfield>"
                        + "<subfield code=\"s\">UC</subfield>"
                        + "<subfield code=\"1\">000142266</subfield></datafield>"
                     + "<datafield tag=\"974\" ind1=\" \" ind2=\" \">"
                        + "<subfield code=\"b\">UC</subfield>"
                        + "<subfield code=\"c\">UCD</subfield>"
                        + "<subfield code=\"d\">20130807</subfield>"
                        + "<subfield code=\"s\">google</subfield>"
                        + "<subfield code=\"u\">uc1.31175001466104</subfield>"
                        + "<subfield code=\"y\">1796</subfield>"
                        + "<subfield code=\"r\">pd</subfield></datafield>"
                     + "</record></collection>";
      record.publishDates = new ArrayList<>();
      record.publishDates.add("1796");

      record.recordURL = "http://catalog.hathitrust.org/Record/012392537";
      record.titles = new ArrayList<>();
      record.titles.add("A defence of revelation, in ten letters to Thomas Paine; being an answer to his first part of the Age of Reason.");
      record.titles.add("defence of revelation, in ten letters to Thomas Paine; being an answer to his first part of the Age of Reason.");
      Map<String, RecordDTO> records = new HashMap<>();
      records.put(recordNum, record);

      return records;
   }

   private List<ItemDTO> buildItemDTO()
   {

      ItemDTO item = new ItemDTO();
      item.enumcron = "false";
      item.fromRecord = "012392537";
      item.htid = "uc1.31175001466104";
      item.itemURL = "http://hdl.handle.net/2027/uc1.31175001466104";
      item.lastUpdate = "20130807";
      item.orig = "University of California";
      item.rightsCode = "pd";
      item.usRightsString = "Full view";

      List<ItemDTO> items = new ArrayList<>();
      items.add(item);

      return items;
   }

   public Record getBibRecord()
   {
      Map<String,RecordDTO> recordEntrys = buildRecordDTO();
      List<Record> records = new ArrayList<>();
      for (Entry<String, RecordDTO> entry : recordEntrys.entrySet())
      {
         records.add(RecordDTO.instantiate(entry, buildItemDTO()));
      }

      return records.get(0);
   }

}
