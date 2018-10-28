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

package com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.mapped;

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
@ContextConfiguration(classes = MappedDataSourcePropertiesTest.Config.class,
                      initializers = ConfigFileApplicationContextInitializer.class)
@TestPropertySource(properties = {"datasource.routing.enabled=false",
                                  "datasource.routing.type=Mapped",
                                  "datasource.routing.defaults.initialize=false",
                                  "datasource.routing.mapped.first.initialize=false",
                                  "datasource.routing.mapped.second.initialize=false"})
@DirtiesContext
class MappedDataSourcePropertiesTest {
    @Autowired
    private MappedDataSourceProperties properties;

    @Test
    void should_be_loaded_yml() {
        final DataSourceProperties defaults = properties.getDefaults();
        final Map<String, DataSourceProperties> mapped = properties.getMapped();
        final DataSourceProperties mapped1 = mapped.get("first");
        final DataSourceProperties mapped2 = mapped.get("second");
        assertAll(
            () -> assertThat(defaults.getUrl()).isEqualTo("jdbc:h2:mem:DEFAULTS"),
            () -> assertThat(defaults.getDriverClassName()).isEqualTo("org.h2.Driver"),
            () -> assertThat(defaults.getUsername()).isEqualTo("sa"),
            () -> assertThat(defaults.isInitialize()).isEqualTo(false),
            () -> assertThat(defaults.getSchema()).containsOnly("defaults.sql"),
            () -> assertThat(mapped1.getUrl()).isEqualTo("jdbc:h2:mem:MAPPED1"),
            () -> assertThat(mapped1.getDriverClassName()).isEqualTo("org.h2.Driver"),
            () -> assertThat(mapped1.getUsername()).isEqualTo("sa"),
            () -> assertThat(mapped1.isInitialize()).isEqualTo(false),
            () -> assertThat(mapped1.getSchema()).containsOnly("mapped1.sql"),
            () -> assertThat(mapped2.getUrl()).isEqualTo("jdbc:h2:mem:MAPPED2"),
            () -> assertThat(mapped2.getDriverClassName()).isEqualTo("org.h2.Driver"),
            () -> assertThat(mapped2.getUsername()).isEqualTo("sa"),
            () -> assertThat(mapped2.isInitialize()).isEqualTo(false),
            () -> assertThat(mapped2.getSchema()).containsOnly("mapped2.sql")
        );
    }

    @Configuration
    @EnableConfigurationProperties(MappedDataSourceProperties.class)
    static class Config {
    }
}
