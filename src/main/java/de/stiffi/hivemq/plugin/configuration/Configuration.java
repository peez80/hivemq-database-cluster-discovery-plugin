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

package de.stiffi.hivemq.plugin.configuration;

import com.google.inject.Inject;

import java.util.Properties;

/**
 * @author Christian Götz
 */
public class Configuration {


    private final Properties properties;

    @Inject
    public Configuration(PluginReader pluginReader) {
        properties = pluginReader.getProperties();
    }


    public String getJdbcUrl() {
        return properties.getProperty("jdbcUrl");
    }
    public String getJdbcUser() {
        return properties.getProperty("jdbcUser");
    }
    public String getJdbcPassword() {
        return properties.getProperty("jdbcPassword");
    }

    public long getInactiveTimeoutMs() {
        return Long.parseLong(properties.getProperty("inactiveTimeoutMs", "30000"));
    }
}
