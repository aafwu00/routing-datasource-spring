/*
 * Copyright 2017-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.multi;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * @author Taeho Kim
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MultiRoutingDataSourcePropertiesTest.Config.class,
                      initializers = ConfigFileApplicationContextInitializer.class)
@TestPropertySource(properties = {"datasource.routing.type=MultiRouting",
                                  "datasource.routing.source.first.master=dp1",
                                  "datasource.routing.targets.switchoff1.initialize=false",
                                  "datasource.routing.targets.switchoff2.initialize=false",
                                  "datasource.routing.targets.switchon1.initialize=false",
                                  "datasource.routing.targets.switchon2.initialize=false",
                                  "datasource.routing.targets.master1.initialize=false",
                                  "datasource.routing.targets.master2.initialize=false",
                                  "datasource.routing.targets.slave1.initialize=false",
                                  "datasource.routing.targets.slave2.initialize=false"})
@DirtiesContext
class MultiRoutingDataSourcePropertiesTest {
    @Autowired
    private MultiRoutingDataSourceProperties properties;

    @Test
    void should_be_loaded_yml() {
        final Map<String, DataSourceProperties> sources = properties.getTargets();
        final Map<String, MultiRoutingDataSourceProperties.MultiDataSourceName> multi = properties.getMulti();
        assertAll(
            () -> assertThat(sources.get("switchoff1").getUrl()).isEqualTo("jdbc:h2:mem:SWITCHOFF1"),
            () -> assertThat(sources.get("switchoff1").getDriverClassName()).isEqualTo("org.h2.Driver"),
            () -> assertThat(sources.get("switchoff1").getUsername()).isEqualTo("sa"),
            () -> assertThat(sources.get("switchoff1").isInitialize()).isFalse(),
            () -> assertThat(sources.get("switchoff1").getSchema()).containsOnly("switch-off-first.sql"),
            () -> assertThat(sources.get("slave2").getUrl()).isEqualTo("jdbc:h2:mem:SLAVES2"),
            () -> assertThat(sources.get("slave2").getDriverClassName()).isEqualTo("org.h2.Driver"),
            () -> assertThat(sources.get("slave2").getUsername()).isEqualTo("sa"),
            () -> assertThat(sources.get("slave2").isInitialize()).isFalse(),
            () -> assertThat(sources.get("slave2").getSchema()).containsOnly("slave-second.sql"),
            () -> assertThat(multi.get("first").getMaster()).isEqualTo("master1"),
            () -> assertThat(multi.get("first").getSlave()).isEqualTo("slave1"),
            () -> assertThat(multi.get("second").getSwitchOff()).isEqualTo("switchoff1"),
            () -> assertThat(multi.get("second").getSwitchOn()).isEqualTo("switchon1"),
            () -> assertThat(multi.get("third").getStandalone()).isEqualTo("switchoff2")
        );
    }

    @Configuration
    @EnableConfigurationProperties(MultiRoutingDataSourceProperties.class)
    static class Config {
    }
}
