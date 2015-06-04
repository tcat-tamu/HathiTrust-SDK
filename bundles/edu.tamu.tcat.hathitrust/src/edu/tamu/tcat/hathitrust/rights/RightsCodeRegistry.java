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
package edu.tamu.tcat.hathitrust.rights;

import java.util.Set;

/**
 * Provides a central point of access for retrieving {@link RightsCode} defined by an
 * application.
 */
public interface RightsCodeRegistry
{
   // impl note - immediately obvious strategies for implementing this include
   //      a database, configuration file(s), enumeration, eclipse extension points.

   /**
    * @return The rights code with the specified key.
    * @throws RightsCodeRegistrationException If no code with the provided key could be found.
    */
   RightsCode find(String key) throws RightsCodeRegistrationException;

   /**
    * @return All rights codes registered for within this application.
    */
   Set<RightsCode> list();
}
