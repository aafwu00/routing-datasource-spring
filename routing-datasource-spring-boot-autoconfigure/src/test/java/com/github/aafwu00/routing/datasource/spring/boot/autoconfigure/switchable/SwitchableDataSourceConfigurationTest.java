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
import com.github.aafwu00.routing.datasource.spring.SwitchableMode;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.RoutingDataSourceAutoConfiguration;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.support.RoutingDataSourceMetrics;
import com.zaxxer.hikari.HikariDataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * @author Taeho Kim
 */
class SwitchableDataSourceConfigurationTest {
    private ApplicationContextRunner contextRunner;

    @BeforeEach
    void setUp() {
        contextRunner = new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(RoutingDataSourceAutoConfiguration.class));
    }

    @Test
    void should_be_not_loaded_SwitchingDataSource_when_replication_disabled() {
        contextRunner.withPropertyValues("datasource.routing.enabled=false")
                     .run(context -> assertThat(context).doesNotHaveBean(LazyConnectionDataSourceProxy.class));
    }

    @Test
    void should_be_loaded_SwitchableDataSource_when_used_default_configuration() {
        contextRunner.withPropertyValues("datasource.routing.enabled=true",
                                         "datasource.routing.switch-off.url=jdbc:h2:mem:SWITCHOFF;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
                                         "datasource.routing.switch-off.type=org.apache.tomcat.jdbc.pool.DataSource",
                                         "datasource.routing.switch-off.driver-class-name=org.h2.Driver",
                                         "datasource.routing.switch-off.initializationMode=never",
                                         "datasource.routing.switch-off.username=sa",
                                         "datasource.routing.switch-off.tomcat.max-active=10",
                                         "datasource.routing.switch-on.url=jdbc:h2:mem:SWITCHON;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
                                         "datasource.routing.switch-on.driver-class-name=org.h2.Driver",
                                         "datasource.routing.switch-on.initializationMode=never",
                                         "datasource.routing.switch-on.username=sa",
                                         "datasource.routing.switch-on.hikari.maximum-pool-size=9")
                     .run(context -> assertAll(
                         () -> assertThat(context).hasSingleBean(LazyConnectionDataSourceProxy.class),
                         () -> assertThat(dataSource(context).getTargetDataSource()).isInstanceOf(DelegateRoutingDataSource.class),
                         () -> assertThat(off(context).getMaxActive()).isEqualTo(10),
                         () -> assertThat(off(context).isTestOnBorrow()).isTrue(),
                         () -> assertThat(off(context).getValidationQuery()).isEqualTo("SELECT 1"),
                         () -> assertThat(on(context).getMaximumPoolSize()).isEqualTo(9),
                         () -> assertThat(context).hasSingleBean(RoutingDataSourceMetrics.class)
                     ));
    }

    private LazyConnectionDataSourceProxy dataSource(final AssertableApplicationContext context) {
        return context.getBean(LazyConnectionDataSourceProxy.class);
    }

    private DataSource off(final AssertableApplicationContext context) {
        return targetDataSource(context, SwitchableMode.Off, DataSource.class);
    }

    private HikariDataSource on(final AssertableApplicationContext context) {
        return targetDataSource(context, SwitchableMode.On, HikariDataSource.class);
    }

    private <T> T targetDataSource(final AssertableApplicationContext context, final SwitchableMode key, final Class<T> target) {
        return target.cast(resolvedDataSources(context).get(key));
    }

    private Map resolvedDataSources(final AssertableApplicationContext context) {
        return Map.class.cast(ReflectionTestUtils.getField(ruleBaseRoutingDataSource(context), "resolvedDataSources"));
    }

    private DelegateRoutingDataSource ruleBaseRoutingDataSource(final AssertableApplicationContext context) {
        return DelegateRoutingDataSource.class.cast(dataSource(context).getTargetDataSource());
    }

    @Test
    void should_be_not_loaded_SwitchableDataSource_when_on_not_exists() {
        contextRunner.withPropertyValues("datasource.routing.enabled=true",
                                         "datasource.routing.switch-off.url=jdbc:h2:mem:SWITCHOFF;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
                                         "datasource.routing.switch-off.driver-class-name=org.h2.Driver",
                                         "datasource.routing.switch-off.initializationMode=never",
                                         "datasource.routing.switch-off.username=sa")
                     .run(context -> assertThat(context).doesNotHaveBean(LazyConnectionDataSourceProxy.class));
    }

    @Test
    void should_be_not_loaded_SwitchableDataSource_when_off_not_exists() {
        contextRunner.withPropertyValues("datasource.routing.enabled=true",
                                         "datasource.routing.switch-on.url=jdbc:h2:mem:SWITCHON;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
                                         "datasource.routing.switch-on.driver-class-name=org.h2.Driver",
                                         "datasource.routing.switch-on.initializationMode=never",
                                         "datasource.routing.switch-on.username=sa")
                     .run(context -> assertThat(context).doesNotHaveBean(LazyConnectionDataSourceProxy.class));
    }
}
