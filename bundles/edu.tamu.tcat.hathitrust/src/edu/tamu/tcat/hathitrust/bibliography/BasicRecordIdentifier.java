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

import edu.tamu.tcat.hathitrust.bibliography.Record.IdType;
import edu.tamu.tcat.hathitrust.bibliography.Record.RecordIdentifier;

/**
 * Basic implementation of a record identifier.
 */
public final class BasicRecordIdentifier implements RecordIdentifier
{
   private final IdType idType;
   private final String id;

   public BasicRecordIdentifier(IdType idType, String id) {
      this.idType = idType;
      this.id = id;
   }

   @Override
   public IdType getScheme()
   {
      return idType;
   }

   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public String toString()
   {
      return idType + ":" + id;
   }
   @Override
   public boolean equals(Object obj)
   {
      if (!(obj instanceof BasicRecordIdentifier))
         return false;

      BasicRecordIdentifier other = (BasicRecordIdentifier)obj;
      return other.id.equalsIgnoreCase(id) && other.idType.equals(idType);
   }

   @Override
   public int hashCode()
   {
      int result = 17;
      result = 31 * result + idType.hashCode();
      result = 31 * result + id.toLowerCase().hashCode();

      return result;
   }
}