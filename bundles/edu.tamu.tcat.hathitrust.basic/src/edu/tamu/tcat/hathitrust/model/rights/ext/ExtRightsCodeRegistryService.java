package edu.tamu.tcat.hathitrust.model.rights.ext;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import edu.tamu.tcat.hathitrust.model.RightsCode;
import edu.tamu.tcat.hathitrust.model.RightsCodeRegistrationException;
import edu.tamu.tcat.hathitrust.model.RightsCodeRegistry;
import edu.tamu.tcat.hathitrust.plugin.PluginFacade;
import edu.tamu.tcat.hathitrust.plugin.PluginRegistrationException;


public class ExtRightsCodeRegistryService implements RightsCodeRegistry
{
   private static final Logger logger = Logger.getLogger(ExtRightsCodeRegistryService.class.getName());
   public static final String EXT_POINT_ID = "edu.tamu.tcat.hathitrust.rightscode";

   private PluginFacade<String, ExtRightCodeMeta> registryDelegate;

   public void activate()
   {
      registryDelegate = new PluginFacade<>(
            EXT_POINT_ID,
            ExtRightCodeMeta::new,
            elem -> elem.getAttribute("id"));

   }

   public void dispose()
   {
      registryDelegate.close();
   }

   @Override
   public RightsCode find(String key) throws RightsCodeRegistrationException
   {
      try
      {
         return registryDelegate.getPlugin(key);
      }
      catch (PluginRegistrationException e)
      {
         throw new RightsCodeRegistrationException("No rights code has been registered for [" + key + "]", e);
      }
   }

   @Override
   public Set<RightsCode> list()
   {
      Set<String> registrations = registryDelegate.getRegistrations();
      return registrations.parallelStream()
                   .map(key -> {
                         try
                         {
                            return registryDelegate.getPlugin(key);
                         }
                         catch (PluginRegistrationException e)
                         {
                            logger.log(Level.SEVERE, "Failed to retrieve plugin [" + key + "]", e);
                            return null;
                         }
                   })
                   .filter(code -> code != null)
                   .collect(Collectors.toSet());
   }

}
