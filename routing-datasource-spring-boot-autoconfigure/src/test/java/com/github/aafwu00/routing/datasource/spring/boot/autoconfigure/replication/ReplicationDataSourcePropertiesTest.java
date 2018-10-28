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

package com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.replication;

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
@ContextConfiguration(classes = ReplicationDataSourcePropertiesTest.Config.class,
                      initializers = ConfigFileApplicationContextInitializer.class)
@TestPropertySource(properties = {"datasource.routing.enabled=false",
                                  "datasource.routing.type=Replication",
                                  "datasource.routing.master.initialize=false",
                                  "datasource.routing.slave.initialize=false"})
@DirtiesContext
class ReplicationDataSourcePropertiesTest {
    @Autowired
    private ReplicationDataSourceProperties properties;

    @Test
    void should_be_loaded_yml() {
        final DataSourceProperties master = properties.getMaster();
        final DataSourceProperties slave = properties.getSlave();
        assertAll(
            () -> assertThat(master.getUrl()).isEqualTo("jdbc:h2:mem:MASTER"),
            () -> assertThat(master.getDriverClassName()).isEqualTo("org.h2.Driver"),
            () -> assertThat(master.getUsername()).isEqualTo("sa"),
            () -> assertThat(master.isInitialize()).isFalse(),
            () -> assertThat(master.getSchema()).containsOnly("master.sql"),
            () -> assertThat(slave.getUrl()).isEqualTo("jdbc:h2:mem:SLAVE"),
            () -> assertThat(slave.getDriverClassName()).isEqualTo("org.h2.Driver"),
            () -> assertThat(slave.getUsername()).isEqualTo("sa"),
            () -> assertThat(slave.isInitialize()).isFalse(),
            () -> assertThat(slave.getSchema()).containsOnly("slave.sql")
        );
    }

    @Configuration
    @EnableConfigurationProperties(ReplicationDataSourceProperties.class)
    static class Config {
    }
}
