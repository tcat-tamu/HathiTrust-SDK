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
package edu.tamu.tcat.hathitrust.data;

public interface DataAPIClient
{
   public enum DataFormat
   {
      ebm,
      pdf,
      epub,
      xml,
      json
   }

   public enum ImageFormat
   {
      raw,
      png,
      jpeg
   }

   /**
    * HathiTrust DataAPIClient /aggregate provides a compressed file that contains 3 separate files for each page of a work.
    *    jp2 image file
    *    txt file containing the text of the image file.
    *    xml file containing coords of each word contained in the image file.
    * @param String representation of the HathiTrust unique item identifier
    * @return String name of the file retreived
    */
   String getAggregate(String htid);


   String getStructure(String htid, DataFormat format);

   /**
    * HathTrust DataAPIClient /volume is currently restricted to a project outside of our scope.
    * If the future it could support pdf | epub documents
    * @param htid
    * @param format
    * @return String identifying the name of the file.
    */
   String getVolume(String htid, DataFormat format);


   String getVolumeMeta(String htid, DataFormat format);


   String getPageMeta(String htid, DataFormat format, int seqNum);


   String getPageImage(String htid, ImageFormat format, int seqNum);


   String getPageOCR(String htid, int seqNum);


   String getPageCoordOCR(String htid, int seqNum);
}
