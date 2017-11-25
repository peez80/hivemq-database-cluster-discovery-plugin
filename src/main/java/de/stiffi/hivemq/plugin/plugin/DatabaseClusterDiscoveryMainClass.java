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

import de.stiffi.hivemq.plugin.callbacks.*;
import com.hivemq.spi.PluginEntryPoint;
import com.hivemq.spi.callback.events.OnConnectCallback;
import com.hivemq.spi.callback.registry.CallbackRegistry;
import de.stiffi.hivemq.plugin.configuration.Configuration;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * This is the main class of the plugin, which is instanciated during the HiveMQ start up process.
 *
 */
public class DatabaseClusterDiscoveryMainClass extends PluginEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(DatabaseClusterDiscoveryMainClass.class);

    private final ClusterCallback clusterCallback;
    private final Configuration config;


    /**
     * This is the injected constructor.
     *
     * @param clusterCallback
     */
    @Inject
    public DatabaseClusterDiscoveryMainClass(final ClusterCallback clusterCallback, final Configuration config) {
        this.clusterCallback = clusterCallback;
        this.config = config;
    }

    /**
     * This method is executed after the instanciation of the whole class. It is used to initialize
     * the implemented callbacks and make them known to the HiveMQ core.
     */
    @PostConstruct
    public void postConstruct()    {
        initFlyway();
        getCallbackRegistry().addCallback(clusterCallback);
    }

    /**
     * On Startup we initialize the database with flyway for zero-configuration.
     * User just has to provide the database.
     *
     * TODO: Once we support multiple databases probably we have to deal with different timestamp formats?!
     *   --> evt. change to long and UTC unix timestamp...
     *
     * Let's see how this behaves on multiple instances starting.
     */
    private void initFlyway() {
        log.info("Updating Database if necessary");
        Flyway flyway = new Flyway();
        flyway.setDataSource(config.getJdbcUrl(), config.getJdbcUser(), config.getJdbcPassword());
        flyway.migrate();
    }
}
