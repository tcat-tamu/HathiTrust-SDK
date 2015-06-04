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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import edu.tamu.tcat.hathitrust.rights.RightsCode;
import edu.tamu.tcat.hathitrust.rights.RightsCodeRegistry;


public class ItemDTO
{
   public String itemId;
   public String recordId;
   public String itemUri;
   public String originatingInstitution;
   public String rightsCode;
   public String lastUpdate;
   public String sortKey;
   public String rightsLabel;

   public static ItemDTO create(Item item)
   {
      throw new UnsupportedOperationException();
   }

   public static Item instantiate(ItemDTO dto, RightsCodeRegistry registry)
   {
      // HACK: at the moment, this is pretty brittle
      //       need to think about how to handle mal-formed data -
      //          throw or get as good an approximation as possible?
      //          that choice should be based on real use cases with API
      try
      {
         URI uri = URI.create(dto.itemUri);
         RightsCode rights = registry.find(dto.rightsCode);
         LocalDate date = LocalDate.parse(dto.lastUpdate, DateTimeFormatter.BASIC_ISO_DATE);
         return new BasicItem(dto.itemId,
                              dto.recordId,
                              uri,
                              dto.originatingInstitution,
                              rights,
                              date,
                              dto.sortKey,
                              dto.rightsLabel);
      }
      catch (Exception ex)
      {
         throw new IllegalArgumentException("Failed to correctly parse dto [" + dto + "]", ex);
      }
   }
}
