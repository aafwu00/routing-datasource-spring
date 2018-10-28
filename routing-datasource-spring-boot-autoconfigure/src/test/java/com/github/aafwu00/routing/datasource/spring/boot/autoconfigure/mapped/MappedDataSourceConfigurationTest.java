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
class MappedDataSourceConfigurationTest {
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
    void should_be_not_loaded_MappedDataSource_when_disabled() {
        loadContext("datasource.routing.enabled=false");
        assertThatThrownBy(this::dataSource).isExactlyInstanceOf(NoSuchBeanDefinitionException.class);
    }

    private LazyConnectionDataSourceProxy dataSource() {
        return context.getBean(LazyConnectionDataSourceProxy.class);
    }

    @Test
    void should_be_loaded_MappedDataSource_when_used_default_configuration() {
        loadContext("datasource.routing.enabled=true",
                    "datasource.routing.defaults.url=jdbc:h2:mem:DEFAULTS;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
                    "datasource.routing.defaults.type=org.apache.tomcat.jdbc.pool.DataSource",
                    "datasource.routing.defaults.driver-class-name=org.h2.Driver",
                    "datasource.routing.defaults.initialize=false",
                    "datasource.routing.defaults.username=sa",
                    "datasource.routing.defaults.tomcat.max-active=10",
                    "datasource.routing.mapped.first.url=jdbc:h2:mem:MAPPED1;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
                    "datasource.routing.mapped.first.type=com.zaxxer.hikari.HikariDataSource",
                    "datasource.routing.mapped.first.driver-class-name=org.h2.Driver",
                    "datasource.routing.mapped.first.initialize=false",
                    "datasource.routing.mapped.first.username=sa",
                    "datasource.routing.mapped.first.hikari.maximum-pool-size=9");
        assertAll(
            () -> assertThat(dataSource()).isNotNull(),
            () -> assertThat(dataSource().getTargetDataSource()).isInstanceOf(DelegateRoutingDataSource.class),
            () -> assertThat(defaults().getMaxActive()).isEqualTo(10),
            () -> assertThat(defaults().isTestOnBorrow()).isTrue(),
            () -> assertThat(defaults().getValidationQuery()).isEqualTo("SELECT 1"),
            () -> assertThat(first().getMaximumPoolSize()).isEqualTo(9),
            () -> assertThat(routingDataSourcePublicMetrics().metrics()).isNotEmpty()
        );
    }

    private DataSource defaults() {
        return defaultTargetDataSource();
    }

    private HikariDataSource first() {
        return targetDataSource("first", HikariDataSource.class);
    }

    private <T> T targetDataSource(String key, Class<T> target) {
        return target.cast(resolvedDataSources().get(key));
    }

    private Map resolvedDataSources() {
        return Map.class.cast(ReflectionTestUtils.getField(ruleBaseRoutingDataSource(), "resolvedDataSources"));
    }

    private DataSource defaultTargetDataSource() {
        return DataSource.class.cast(ReflectionTestUtils.getField(ruleBaseRoutingDataSource(), "defaultTargetDataSource"));
    }

    private DelegateRoutingDataSource ruleBaseRoutingDataSource() {
        return DelegateRoutingDataSource.class.cast(dataSource().getTargetDataSource());
    }

    private RoutingDataSourcePublicMetrics routingDataSourcePublicMetrics() {
        return context.getBean(RoutingDataSourcePublicMetrics.class);
    }

    @Test
    void should_be_not_loaded_MappedDataSource_when_mapped_not_exists() {
        loadContext("datasource.routing.enabled=true",
                    "datasource.routing.defaults.url=jdbc:h2:mem:DEFAULTS;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
                    "datasource.routing.defaults.driver-class-name=org.h2.Driver",
                    "datasource.routing.defaults.initialize=false",
                    "datasource.routing.defaults.username=sa");
        assertThatThrownBy(this::dataSource).isExactlyInstanceOf(NoSuchBeanDefinitionException.class);
    }

    @Test
    void should_be_not_loaded_MappedDataSource_when_defaults_not_exists() {
        loadContext("datasource.routing.enabled=true",
                    "datasource.routing.mapped.first.url=jdbc:h2:mem:MAPPED1;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
                    "datasource.routing.mapped.first.driver-class-name=org.h2.Driver",
                    "datasource.routing.mapped.first.initialize=false",
                    "datasource.routing.mapped.first.username=sa");
        assertThatThrownBy(this::dataSource).isExactlyInstanceOf(NoSuchBeanDefinitionException.class);
    }

    private void loadContext(final String... pairs) {
        addEnvironment(context, pairs);
        context.register(MappedDataSourceConfigurationTest.DefaultConfiguration.class);
        context.register(RoutingDataSourceAutoConfiguration.class);
        context.refresh();
    }

    @Configuration
    static class DefaultConfiguration {
    }
}
