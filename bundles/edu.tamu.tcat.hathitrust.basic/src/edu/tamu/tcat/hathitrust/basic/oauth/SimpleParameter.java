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
package edu.tamu.tcat.hathitrust.basic.oauth;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class SimpleParameter implements Parameter
{
   private final String key;
   private final String value;

   private SimpleParameter(String key, String value)
   {
      this.key = key;
      this.value = value;
   }

   public static SimpleParameter create(String key, String value)
   {
      if (key == null || key.trim().isEmpty())
         throw new IllegalArgumentException("A non-empty key value must be supplied.");

      if (value == null)
         value = "";

      return new SimpleParameter(key, value);
   }

   @Override
   public String getKey()
   {
      return key;
   }

   @Override
   public String getValue()
   {
      return value;
   }

   @Override
   public int compareTo(Parameter p)
   {
      // TODO check for null.
      int keyCmp = key.compareTo(p.getKey());

      return keyCmp != 0 ? keyCmp : value.compareTo(p.getValue());
   }

   @Override
   public String format()
   {
      try
      {
         return String.format("%s=%s",
               URLEncoder.encode(key, "UTF-8"),
               URLEncoder.encode(value, "UTF-8"));
      }
      catch (UnsupportedEncodingException ex)
      {
         // failed to encode utf-8
         throw new IllegalStateException(ex);
      }
   }

   @Override
   public String toString()
   {
      return format();
   }
}
