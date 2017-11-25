/*
 * Copyright 2015 dc-square GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.stiffi.hivemq.plugin.plugin;

import com.hivemq.spi.HiveMQPluginModule;
import com.hivemq.spi.PluginEntryPoint;
import com.hivemq.spi.plugin.meta.Information;
import de.stiffi.hivemq.plugin.configuration.Configuration;
import de.stiffi.hivemq.plugin.database.JdbcDatabaseClusterStore;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;


/**
 * This is the plugin module class, which handles the initialization and configuration
 * of the plugin. Each plugin need to have a class, which is extending {@link HiveMQPluginModule}.
 * Also the fully qualified name of the class should be present in a file named
 * com.hivemq.spi.HiveMQPluginModule, which has to be located in META-INF/services.
 *
 * @author Christian Götz
 */
@Information(name = "HiveMQ Database Cluster Discovery Plugin", author = "Philipp Lamp", version = "1.0.0", description = "Lightweight jdbc based Cluster Plugin. First Version only supports Posgres!")
public class DatabaseClusterDiscoveryPluginModule extends HiveMQPluginModule {

    private static final Logger log = LoggerFactory.getLogger(DatabaseClusterDiscoveryPluginModule.class);

    /**
     * This method is provided to execute some custom plugin configuration stuff. Is is the place
     * to execute Google Guice bindings,etc if needed.
     */
    @Override
    protected void configurePlugin() {
    }


    /**
     * This method needs to return the main class of the plugin.
     *
     * @return callback priority
     */
    @Override
    protected Class<? extends PluginEntryPoint> entryPointClass() {
        return DatabaseClusterDiscoveryMainClass.class;
    }
}
