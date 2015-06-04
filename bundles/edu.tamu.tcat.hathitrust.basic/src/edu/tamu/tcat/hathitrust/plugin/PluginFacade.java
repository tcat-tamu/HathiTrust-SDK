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
package edu.tamu.tcat.hathitrust.plugin;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryEventListener;
import org.eclipse.core.runtime.Platform;

/**
 * Manages tasks associated with loading, unloading and providing access to plugins registered
 * through the Eclipse extension point system.
 *
 * Note: This will be moved to an appropriate place within the OSGi Utitilies framework once
 * it reaches a suitable level of maturity.
 *
 */
public class PluginFacade<K, T> implements AutoCloseable
{
   // TODO consider removing IConfigurationElement from the API to eliminate dependency on
   //      eclipse and allow this system to extend to other plugin strategies.
   // TODO Move to OSGi repo.

   private final static Logger logger = Logger.getLogger(PluginFacade.class.getName());

   private final String extPointId;
   private final Function<IConfigurationElement, T> factory;
   private final Function<IConfigurationElement, K> keyGenerator;

   private final ConcurrentMap<K, T> activePlugins = new ConcurrentHashMap<>();

   private volatile RegistryEventListener ears;

   /**
    *
    * @param extPointId
    * @param factory Given the plugin metadata supplied by an {@code IConfigurationElement}.
    * @param keyGenerator Generates unique, immutable keys from a supplied
    *       {@code IConfigurationElement}. These objects must be valid hash keys.
    */
   public PluginFacade(String extPointId,
                       Function<IConfigurationElement, T> factory,
                       Function<IConfigurationElement, K> keyGenerator)
   {
      this.extPointId = extPointId;
      this.factory = factory;
      this.keyGenerator = keyGenerator;

      loadExtensions();
   }

   /**
    * Loads all currently registered plugins for the corresponding extension point
    * and attaches a listener to the {@link IExtensionRegistry} that will be notified
    * when new extensions become available or loaded extensions are removed.
    */
   private void loadExtensions()
   {
      IExtensionRegistry registry = Platform.getExtensionRegistry();
      ears = new RegistryEventListener();
      registry.addListener(ears, extPointId);

      // register any currently loaded transformers
      IExtension[] extensions = registry.getExtensionPoint(extPointId).getExtensions();
      Stream.of(extensions)
            .flatMap(ext -> Stream.of(ext.getConfigurationElements()))
            .forEach(ears::addPlugin);
   }

   @Override
   public void close()
   {
      if (ears != null)
      {
         IExtensionRegistry registry = Platform.getExtensionRegistry();
         registry.removeListener(ears);
      }

      ears = null;
      activePlugins.clear();
   }

   public boolean isClosed()
   {
      return ears == null;
   }

   public Set<K> getRegistrations()
   {
      return new HashSet<>(activePlugins.keySet());
   }

   public boolean isRegistered(K key)
   {
      return activePlugins.containsKey(key);
   }

   public T getPlugin(K key) throws PluginRegistrationException
   {
      T plugin = activePlugins.get(key);
      if (plugin == null)
         throw new PluginRegistrationException("No plugin is registered for key [" + key + "]");

      return plugin;
   }

   private class RegistryEventListener implements IRegistryEventListener
   {

      @Override
      public void added(IExtension[] extensions)
      {
         Stream.of(extensions)
               .flatMap(ext -> Stream.of(ext.getConfigurationElements()))
               .forEach(this::addPlugin);
      }

      @Override
      public void removed(IExtension[] extensions)
      {
         Stream.of(extensions)
               .flatMap(ext -> Stream.of(ext.getConfigurationElements()))
               .forEach(this::removePlugin);
      }

      private void addPlugin(IConfigurationElement elem)
      {
         try
         {
            K key = keyGenerator.apply(elem);
            activePlugins.computeIfAbsent(key, junk -> factory.apply(elem));
         }
         catch (Exception ex)
         {
            logger.log(Level.WARNING, "Failed to add plugin for supplied configuration [" + elem + "]", ex);
         }
      }

      private void removePlugin(IConfigurationElement elem)
      {
         try
         {
            K key = keyGenerator.apply(elem);
            activePlugins.remove(key);
         }
         catch (Exception ex)
         {
            logger.log(Level.WARNING, "Failed to remove plugin for supplied configuration [" + elem + "]", ex);
         }
      }

      @Override
      public void added(IExtensionPoint[] extensionPoints)
      {
         // no-op
      }

      @Override
      public void removed(IExtensionPoint[] extensionPoints)
      {
         // no-op
      }

   }
}
