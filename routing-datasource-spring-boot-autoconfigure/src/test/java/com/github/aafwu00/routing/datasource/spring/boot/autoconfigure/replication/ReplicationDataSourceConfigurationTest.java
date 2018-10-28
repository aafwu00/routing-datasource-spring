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

import java.util.Map;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.aafwu00.routing.datasource.spring.DelegateRoutingDataSource;
import com.github.aafwu00.routing.datasource.spring.ReplicationType;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.RoutingDataSourceAutoConfiguration;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.support.RoutingDataSourcePublicMetrics;
import com.zaxxer.hikari.HikariDataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.boot.test.util.EnvironmentTestUtils.addEnvironment;

/**
 * @author Taeho Kim
 */
class ReplicationDataSourceConfigurationTest {
    private AnnotationConfigApplicationContext context;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigApplicationContext();
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void should_be_not_loaded_ReplicationDataSource_when_routing_disabled() {
        loadContext("datasource.routing.enabled=false");
        assertThatThrownBy(this::dataSource).isExactlyInstanceOf(NoSuchBeanDefinitionException.class);
    }

    private LazyConnectionDataSourceProxy dataSource() {
        return context.getBean(LazyConnectionDataSourceProxy.class);
    }

    @Test
    void should_be_loaded_ReplicationDataSource_when_used_default_configuration() {
        loadContext("datasource.routing.enabled=true",
                    "datasource.routing.master.url=jdbc:h2:mem:MASTER;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
                    "datasource.routing.master.driver-class-name=org.h2.Driver",
                    "datasource.routing.master.initialize=false",
                    "datasource.routing.master.username=sa",
                    "datasource.routing.master.tomcat.max-active=10",
                    "datasource.routing.slave.type=com.zaxxer.hikari.HikariDataSource",
                    "datasource.routing.slave.url=jdbc:h2:mem:SLAVES;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
                    "datasource.routing.slave.driver-class-name=org.h2.Driver",
                    "datasource.routing.slave.initialize=false",
                    "datasource.routing.slave.username=sa",
                    "datasource.routing.slave.hikari.maximum-pool-size=8"
        );
        assertAll(
            () -> assertThat(dataSource()).isNotNull(),
            () -> assertThat(dataSource().getTargetDataSource()).isInstanceOf(DelegateRoutingDataSource.class),
            () -> assertThat(master().getMaxActive()).isEqualTo(10),
            () -> assertThat(master().isTestOnBorrow()).isTrue(),
            () -> assertThat(master().getValidationQuery()).isEqualTo("SELECT 1"),
            () -> assertThat(slave().getMaximumPoolSize()).isEqualTo(8),
            () -> assertThat(routingDataSourcePublicMetrics().metrics()).isNotEmpty()
        );
    }

    private DataSource master() {
        return targetDataSource(ReplicationType.Master, DataSource.class);
    }

    private HikariDataSource slave() {
        return targetDataSource(ReplicationType.Slave, HikariDataSource.class);
    }

    private <T> T targetDataSource(ReplicationType key, Class<T> target) {
        return target.cast(resolvedDataSources().get(key));
    }

    private Map resolvedDataSources() {
        return Map.class.cast(ReflectionTestUtils.getField(ruleBaseRoutingDataSource(), "resolvedDataSources"));
    }

    private DelegateRoutingDataSource ruleBaseRoutingDataSource() {
        return DelegateRoutingDataSource.class.cast(dataSource().getTargetDataSource());
    }

    private RoutingDataSourcePublicMetrics routingDataSourcePublicMetrics() {
        return context.getBean(RoutingDataSourcePublicMetrics.class);
    }

    @Test
    void should_be_not_loaded_ReplicationDataSource_when_slave_not_exists() {
        loadContext("datasource.routing.enabled=true",
                    "datasource.routing.master.url=jdbc:h2:mem:MASTER;DB_CLOSE_DELAY=-1;"
                        + "DB_CLOSE_ON_EXIT=false",
                    "datasource.routing.master.driver-class-name=org.h2.Driver",
                    "datasource.routing.master.initialize=false",
                    "datasource.routing.master.username=sa");
        assertThatThrownBy(this::dataSource).isExactlyInstanceOf(NoSuchBeanDefinitionException.class);
    }

    @Test
    void should_be_not_loaded_ReplicationDataSource_when_master_not_exists() {
        loadContext("datasource.routing.enabled=true",
                    "datasource.routing.slave.url=jdbc:h2:mem:SLAVES;DB_CLOSE_DELAY=-1;"
                        + "DB_CLOSE_ON_EXIT=false",
                    "datasource.routing.slave.driver-class-name=org.h2.Driver",
                    "datasource.routing.slave.initialize=false",
                    "datasource.routing.slave.username=sa");
        assertThatThrownBy(this::dataSource).isExactlyInstanceOf(NoSuchBeanDefinitionException.class);
    }

    private void loadContext(final String... pairs) {
        addEnvironment(context, pairs);
        context.register(DefaultConfiguration.class);
        context.register(RoutingDataSourceAutoConfiguration.class);
        context.refresh();
    }

    @Configuration
    static class DefaultConfiguration {
    }
}
