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

package com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.switchable;

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
import static org.springframework.boot.jdbc.DataSourceInitializationMode.NEVER;

/**
 * @author Taeho Kim
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SwitchableDataSourcePropertiesTest.Config.class,
                      initializers = ConfigFileApplicationContextInitializer.class)
@TestPropertySource(properties = {"datasource.routing.enabled=false",
                                  "datasource.routing.type=Switchable",
                                  "datasource.routing.switch-off.initializationMode=never",
                                  "datasource.routing.switch-on.initializationMode=never"})
@DirtiesContext
class SwitchableDataSourcePropertiesTest {
    @Autowired
    private SwitchableDataSourceProperties properties;

    @Test
    void should_be_loaded_yml() {
        final DataSourceProperties off = properties.getSwitchOff();
        final DataSourceProperties on = properties.getSwitchOn();
        assertAll(
            () -> assertThat(off.getUrl()).isEqualTo("jdbc:h2:mem:SWITCHOFF"),
            () -> assertThat(off.getDriverClassName()).isEqualTo("org.h2.Driver"),
            () -> assertThat(off.getUsername()).isEqualTo("sa"),
            () -> assertThat(off.getInitializationMode()).isEqualTo(NEVER),
            () -> assertThat(off.getSchema()).containsOnly("switch-off.sql"),
            () -> assertThat(on.getUrl()).isEqualTo("jdbc:h2:mem:SWITCHON"),
            () -> assertThat(on.getDriverClassName()).isEqualTo("org.h2.Driver"),
            () -> assertThat(on.getUsername()).isEqualTo("sa"),
            () -> assertThat(on.getInitializationMode()).isEqualTo(NEVER),
            () -> assertThat(on.getSchema()).containsOnly("switch-on.sql")
        );
    }

    @Configuration
    @EnableConfigurationProperties(SwitchableDataSourceProperties.class)
    static class Config {
    }
}
