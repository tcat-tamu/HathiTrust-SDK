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
