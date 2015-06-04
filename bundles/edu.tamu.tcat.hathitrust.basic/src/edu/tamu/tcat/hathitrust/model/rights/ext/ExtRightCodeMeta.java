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
package edu.tamu.tcat.hathitrust.model.rights.ext;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.IConfigurationElement;

import edu.tamu.tcat.hathitrust.rights.RightsCode;

/**
 *  Reads Eclipse plugin definitions {@link IConfigurationElement} to define a
 *  {@link RightsCode} established by HathiTrust.
 */
public class ExtRightCodeMeta implements RightsCode
{
   private final IConfigurationElement config;
   private final int id;
   private final URI uri;
   private final String key;
   private final RightsType type;
   private final String title;
   private final String description;

   public ExtRightCodeMeta(IConfigurationElement elem)
   {
      config = elem;
      key = config.getAttribute("id");
      title = config.getAttribute("title");
      description = config.getAttribute("description");

      String typeStr = config.getAttribute("type");
      switch (typeStr.toLowerCase().trim())
      {
         case "access":
            type = RightsType.Access;
            break;
         case "copyright":
            type = RightsType.Copyright;
            break;
         default:
            throw new IllegalStateException("Invalid rights code configuration for [" + key + "]. Expected type of 'access' or 'copyright' but found [" + typeStr + "]");

      }

      String uriString = config.getAttribute("uri");
      String idCode = config.getAttribute("code");
      try
      {
         uri = (uriString != null && !uriString.trim().isEmpty()) ? new URI(uriString) : null;
         id = Integer.parseInt(idCode);
      }
      catch (URISyntaxException ex)
      {
         throw new IllegalStateException("Invalid rights code configuration for [" + key + "]. Expected valid URI but found [" + uriString + "]", ex);
      }
      catch (NumberFormatException nfe)
      {
         throw new IllegalStateException("Invalid rights code configuration for [" + key + "]. Expected numeric rights code id but found [" + idCode + "]", nfe);
      }
   }

   @Override
   public int getId()
   {
      return id;
   }

   @Override
   public URI getUri()
   {
      return uri;
   }

   @Override
   public String getKey()
   {
      return key;
   }

   @Override
   public RightsType getType()
   {
      return type;
   }

   @Override
   public String getTitle()
   {
      return title;
   }

   @Override
   public String getDescription()
   {
      return description;
   }

}
