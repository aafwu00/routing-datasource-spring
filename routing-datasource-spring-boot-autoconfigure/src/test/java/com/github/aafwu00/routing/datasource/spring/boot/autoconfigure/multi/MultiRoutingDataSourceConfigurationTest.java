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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.github.aafwu00.routing.datasource.spring.DelegateRoutingDataSource;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.RoutingDataSourceAutoConfiguration;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.support.RoutingDataSourceMetrics;
import com.zaxxer.hikari.HikariDataSource;

import static com.github.aafwu00.routing.datasource.spring.ReplicationType.Master;
import static com.github.aafwu00.routing.datasource.spring.ReplicationType.Slave;
import static com.github.aafwu00.routing.datasource.spring.SwitchableMode.Off;
import static com.github.aafwu00.routing.datasource.spring.SwitchableMode.On;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * @author Taeho Kim
 */
class MultiRoutingDataSourceConfigurationTest {
    private ApplicationContextRunner contextRunner;

    @BeforeEach
    void setUp() {
        contextRunner = new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(RoutingDataSourceAutoConfiguration.class));
    }

    @Test
    void should_be_not_loaded_MultiRoutingDataSource_when_routing_disabled() {
        contextRunner.withPropertyValues("datasource.routing.enabled=false")
                     .run(context -> assertThat(context).doesNotHaveBean(LazyConnectionDataSourceProxy.class));
    }

    @Test
    void should_be_loaded_MultiSecondaryDataSource_when_used_default_configuration() {
        contextRunner.withPropertyValues("datasource.routing.enabled=true",
                                         "datasource.routing.enable-chained-transaction-manager=true",
                                         "datasource.routing.multi.first.master=master1",
                                         "datasource.routing.multi.first.slave=slave1",
                                         "datasource.routing.multi.second.switch-off=switchoff1",
                                         "datasource.routing.multi.second.switch-on=switchon1",
                                         "datasource.routing.multi.third.standalone=other",
                                         "datasource.routing.multi.forth.defaults=master1",
                                         "datasource.routing.multi.forth.mapped.first=other",
                                         "datasource.routing.targets.master1.type=org.apache.tomcat.jdbc.pool.DataSource",
                                         "datasource.routing.targets.master1.url=jdbc:h2:mem:MASTER1;DB_CLOSE_DELAY=-1",
                                         "datasource.routing.targets.master1.driver-class-name=org.h2.Driver",
                                         "datasource.routing.targets.master1.initializationMode=never",
                                         "datasource.routing.targets.master1.username=sa",
                                         "datasource.routing.targets.master1.tomcat.max-active=10",
                                         "datasource.routing.targets.slave1.type=com.zaxxer.hikari.HikariDataSource",
                                         "datasource.routing.targets.slave1.url=jdbc:h2:mem:SWITCHON2;DB_CLOSE_DELAY=-1",
                                         "datasource.routing.targets.slave1.driver-class-name=org.h2.Driver",
                                         "datasource.routing.targets.slave1.initializationMode=never",
                                         "datasource.routing.targets.slave1.username=sa",
                                         "datasource.routing.targets.slave1.hikari.maximum-pool-size=9",
                                         "datasource.routing.targets.switchoff1.type=org.apache.tomcat.jdbc.pool.DataSource",
                                         "datasource.routing.targets.switchoff1.url=jdbc:h2:mem:SWITCHOFF1;DB_CLOSE_DELAY=-1",
                                         "datasource.routing.targets.switchoff1.driver-class-name=org.h2.Driver",
                                         "datasource.routing.targets.switchoff1.initializationMode=never",
                                         "datasource.routing.targets.switchoff1.username=sa",
                                         "datasource.routing.targets.switchoff1.tomcat.max-active=8",
                                         "datasource.routing.targets.switchon1.type=com.zaxxer.hikari.HikariDataSource",
                                         "datasource.routing.targets.switchon1.url=jdbc:h2:mem:SWITCHON1;DB_CLOSE_DELAY=-1",
                                         "datasource.routing.targets.switchon1.driver-class-name=org.h2.Driver",
                                         "datasource.routing.targets.switchon1.initializationMode=never",
                                         "datasource.routing.targets.switchon1.username=sa",
                                         "datasource.routing.targets.switchon1.hikari.maximum-pool-size=7",
                                         "datasource.routing.targets.other.type=com.zaxxer.hikari.HikariDataSource",
                                         "datasource.routing.targets.other.url=jdbc:h2:mem:SWITCHON2;DB_CLOSE_DELAY=-1",
                                         "datasource.routing.targets.other.driver-class-name=org.h2.Driver",
                                         "datasource.routing.targets.other.initializationMode=never",
                                         "datasource.routing.targets.other.username=sa",
                                         "datasource.routing.targets.other.hikari.maximum-pool-size=6")
                     .run(context -> assertAll(
                         () -> assertThat(dataSource(context, "first")).isNotNull(),
                         () -> assertThat(dataSource(context, "first").getTargetDataSource()).isInstanceOf(DelegateRoutingDataSource.class),
                         () -> assertThat(context).hasBean("firstTransactionManager"),
                         () -> assertThat(context.getBean("firstTransactionManager", PlatformTransactionManager.class)).isNotNull(),
                         () -> assertThat(context).hasBean("firstTransactionTemplate"),
                         () -> assertThat(context.getBean("firstTransactionTemplate", TransactionTemplate.class)).isNotNull(),
                         () -> assertThat(master(context).getMaxActive()).isEqualTo(10),
                         () -> assertThat(slave(context).getMaximumPoolSize()).isEqualTo(9),
                         () -> assertThat(dataSource(context, "second")).isNotNull(),
                         () -> assertThat(dataSource(context,
                                                     "second").getTargetDataSource()).isInstanceOf(DelegateRoutingDataSource.class),
                         () -> assertThat(context).hasBean("secondTransactionManager"),
                         () -> assertThat(off(context).getMaxActive()).isEqualTo(8),
                         () -> assertThat(on(context).getMaximumPoolSize()).isEqualTo(7),
                         () -> assertThat(dataSource(context, "third", LazyConnectionDataSourceAdaptor.class)).isNotNull(),
                         () -> assertThat(dataSource(context,
                                                     "third",
                                                     LazyConnectionDataSourceAdaptor.class).getTargetDataSource()).isInstanceOf(
                             HikariDataSource.class),
                         () -> assertThat(context.getBean("thirdTransactionManager", PlatformTransactionManager.class)).isNotNull(),
                         () -> assertThat(third(context).getMaximumPoolSize()).isEqualTo(6),
                         () -> assertThat(context).hasSingleBean(ChainedTransactionManager.class),
                         () -> assertThat(context).hasSingleBean(RoutingDataSourceMetrics.class)
                     ));
    }

    private org.apache.tomcat.jdbc.pool.DataSource master(final AssertableApplicationContext context) {
        return targetDataSource(context, "first", Master, org.apache.tomcat.jdbc.pool.DataSource.class);
    }

    private HikariDataSource slave(final AssertableApplicationContext context) {
        return targetDataSource(context, "first", Slave, HikariDataSource.class);
    }

    private org.apache.tomcat.jdbc.pool.DataSource off(final AssertableApplicationContext context) {
        return targetDataSource(context, "second", Off, org.apache.tomcat.jdbc.pool.DataSource.class);
    }

    private HikariDataSource on(final AssertableApplicationContext context) {
        return targetDataSource(context, "second", On, HikariDataSource.class);
    }

    private <T> T targetDataSource(final AssertableApplicationContext context, final String name, final Object key, final Class<T> target) {
        return target.cast(resolvedDataSources(context, name).get(key));
    }

    private Map resolvedDataSources(final AssertableApplicationContext context, final String name) {
        return Map.class.cast(ReflectionTestUtils.getField(ruleBaseRoutingDataSource(context, name), "resolvedDataSources"));
    }

    private DelegateRoutingDataSource ruleBaseRoutingDataSource(final AssertableApplicationContext context, final String name) {
        return DelegateRoutingDataSource.class.cast(dataSource(context, name).getTargetDataSource());
    }

    private HikariDataSource third(final AssertableApplicationContext context) {
        return HikariDataSource.class.cast(dataSource(context, "third", LazyConnectionDataSourceAdaptor.class).getTargetDataSource());
    }

    private LazyConnectionDataSourceProxy dataSource(final AssertableApplicationContext context, String name) {
        return dataSource(context, name, LazyConnectionDataSourceProxy.class);
    }

    private <T> T dataSource(final AssertableApplicationContext context, final String name, final Class<T> clazz) {
        return context.getBean(name + "DataSource", clazz);
    }

    @Test
    void should_be_not_loaded_MultiRoutingDataSource_when_multi_not_found() {
        contextRunner.withPropertyValues("datasource.routing.enabled=true",
                                         "datasource.routing.multi.first.standalone=notfound",
                                         "datasource.routing.targets.first.url=jdbc:h2:mem:SWITCHOFF",
                                         "datasource.routing.targets.first.driver-class-name=org.h2.Driver",
                                         "datasource.routing.targets.first.initializationMode=never",
                                         "datasource.routing.targets.first.username=sa")
                     .run(context -> assertThat(context).hasFailed()
                                                        .getFailure()
                                                        .isExactlyInstanceOf(BeanDefinitionValidationException.class));
    }

    @Test
    void should_be_not_loaded_MultiRoutingDataSource_when_multi_mapped_not_found() {
        contextRunner.withPropertyValues("datasource.routing.enabled=true",
                                         "datasource.routing.multi.first.defaults=first",
                                         "datasource.routing.multi.first.mapped.second=notfound",
                                         "datasource.routing.targets.first.url=jdbc:h2:mem:SWITCHOFF",
                                         "datasource.routing.targets.first.driver-class-name=org.h2.Driver",
                                         "datasource.routing.targets.first.initialize=false",
                                         "datasource.routing.targets.first.username=sa")
                     .run(context -> assertThat(context).hasFailed()
                                                        .getFailure()
                                                        .isExactlyInstanceOf(BeanDefinitionValidationException.class));
    }

    @Test
    void should_be_not_loaded_MultiRoutingDataSource_when_multi_not_exists() {
        contextRunner.withPropertyValues("datasource.routing.enabled=true",
                                         "datasource.routing.targets.first.url=jdbc:h2:mem:SWITCHOFF",
                                         "datasource.routing.targets.first.driver-class-name=org.h2.Driver",
                                         "datasource.routing.targets.first.initializationMode=never",
                                         "datasource.routing.targets.first.username=sa")
                     .run(context -> assertThat(context).doesNotHaveBean(LazyConnectionDataSourceProxy.class));
    }

    @Test
    void should_be_not_loaded_MultiRoutingDataSource_when_sources_not_exists() {
        contextRunner.withPropertyValues("datasource.routing.enabled=true",
                                         "datasource.routing.multi.first.standalone=test")
                     .run(context -> assertThat(context).doesNotHaveBean(LazyConnectionDataSourceProxy.class));
    }
}
