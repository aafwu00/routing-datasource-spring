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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.aafwu00.routing.datasource.spring.DelegateRoutingDataSource;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.RoutingDataSourceAutoConfiguration;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.support.RoutingDataSourceMetrics;
import com.zaxxer.hikari.HikariDataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * @author Taeho Kim
 */
class MappedDataSourceConfigurationTest {
    private ApplicationContextRunner contextRunner;

    @BeforeEach
    void setUp() {
        contextRunner = new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(RoutingDataSourceAutoConfiguration.class));
    }

    @Test
    void should_be_not_loaded_MappedDataSource_when_replication_disabled() {
        contextRunner.withPropertyValues("datasource.routing.enabled=false")
                     .run(context -> assertThat(context).doesNotHaveBean(LazyConnectionDataSourceProxy.class));
    }

    @Test
    void should_be_loaded_MappedDataSource_when_used_default_configuration() {
        contextRunner.withPropertyValues("datasource.routing.enabled=true",
                                         "datasource.routing.defaults.url=jdbc:h2:mem:DEFAULTS;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
                                         "datasource.routing.defaults.type=org.apache.tomcat.jdbc.pool.DataSource",
                                         "datasource.routing.defaults.driver-class-name=org.h2.Driver",
                                         "datasource.routing.defaults.initializationMode=never",
                                         "datasource.routing.defaults.username=sa",
                                         "datasource.routing.defaults.tomcat.max-active=10",
                                         "datasource.routing.mapped.first.url=jdbc:h2:mem:MAPPED1;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
                                         "datasource.routing.mapped.first.driver-class-name=org.h2.Driver",
                                         "datasource.routing.mapped.first.initializationMode=never",
                                         "datasource.routing.mapped.first.username=sa",
                                         "datasource.routing.mapped.first.hikari.maximum-pool-size=9")
                     .run(context -> assertAll(
                         () -> assertThat(context).hasSingleBean(LazyConnectionDataSourceProxy.class),
                         () -> assertThat(dataSource(context).getTargetDataSource()).isInstanceOf(DelegateRoutingDataSource.class),
                         () -> assertThat(defaults(context).getMaxActive()).isEqualTo(10),
                         () -> assertThat(defaults(context).isTestOnBorrow()).isTrue(),
                         () -> assertThat(defaults(context).getValidationQuery()).isEqualTo("SELECT 1"),
                         () -> assertThat(mappedFirst(context).getMaximumPoolSize()).isEqualTo(9),
                         () -> assertThat(context).hasSingleBean(RoutingDataSourceMetrics.class)
                     ));
    }

    private LazyConnectionDataSourceProxy dataSource(final AssertableApplicationContext context) {
        return context.getBean(LazyConnectionDataSourceProxy.class);
    }

    private DataSource defaults(final AssertableApplicationContext context) {
        return defaultTargetDataSource(context);
    }

    private HikariDataSource mappedFirst(final AssertableApplicationContext context) {
        return targetDataSource(context, "first", HikariDataSource.class);
    }

    private <T> T targetDataSource(final AssertableApplicationContext context, final String key, final Class<T> target) {
        return target.cast(resolvedDataSources(context).get(key));
    }

    private DataSource defaultTargetDataSource(final AssertableApplicationContext context) {
        return DataSource.class.cast(ReflectionTestUtils.getField(ruleBaseRoutingDataSource(context), "defaultTargetDataSource"));
    }

    private Map resolvedDataSources(final AssertableApplicationContext context) {
        return Map.class.cast(ReflectionTestUtils.getField(ruleBaseRoutingDataSource(context), "resolvedDataSources"));
    }

    private DelegateRoutingDataSource ruleBaseRoutingDataSource(final AssertableApplicationContext context) {
        return DelegateRoutingDataSource.class.cast(dataSource(context).getTargetDataSource());
    }

    @Test
    void should_be_not_loaded_MappedDataSource_when_on_not_exists() {
        contextRunner.withPropertyValues("datasource.routing.enabled=true",
                                         "datasource.routing.defaults.url=jdbc:h2:mem:DEFAULTS;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
                                         "datasource.routing.defaults.driver-class-name=org.h2.Driver",
                                         "datasource.routing.defaults.initializationMode=never",
                                         "datasource.routing.defaults.username=sa")
                     .run(context -> assertThat(context).doesNotHaveBean(LazyConnectionDataSourceProxy.class));
    }

    @Test
    void should_be_not_loaded_MappedDataSource_when_off_not_exists() {
        contextRunner.withPropertyValues("datasource.routing.enabled=true",
                                         "datasource.routing.mapped.first.url=jdbc:h2:mem:MAPPED1;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
                                         "datasource.routing.mapped.first.driver-class-name=org.h2.Driver",
                                         "datasource.routing.mapped.first.initializationMode=never",
                                         "datasource.routing.mapped.first.username=sa")
                     .run(context -> assertThat(context).doesNotHaveBean(LazyConnectionDataSourceProxy.class));
    }
}
