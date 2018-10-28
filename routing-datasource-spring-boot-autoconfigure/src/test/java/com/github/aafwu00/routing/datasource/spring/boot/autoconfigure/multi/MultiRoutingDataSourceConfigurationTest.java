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

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.github.aafwu00.routing.datasource.spring.DelegateRoutingDataSource;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.RoutingDataSourceAutoConfiguration;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.support.RoutingDataSourcePublicMetrics;
import com.zaxxer.hikari.HikariDataSource;

import static com.github.aafwu00.routing.datasource.spring.ReplicationType.Master;
import static com.github.aafwu00.routing.datasource.spring.ReplicationType.Slave;
import static com.github.aafwu00.routing.datasource.spring.SwitchableMode.Off;
import static com.github.aafwu00.routing.datasource.spring.SwitchableMode.On;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.boot.test.util.EnvironmentTestUtils.addEnvironment;

/**
 * @author Taeho Kim
 */
class MultiRoutingDataSourceConfigurationTest {
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
    void should_be_not_loaded_MultiRoutingDataSource_when_routing_disabled() {
        loadContext("datasource.routing.enabled=false");
        assertThatThrownBy(this::dataSource).isExactlyInstanceOf(NoSuchBeanDefinitionException.class);
    }

    @Test
    void should_be_loaded_MultiSecondaryDataSource_when_used_default_configuration() {
        loadContext("datasource.routing.enabled=true",
                    "datasource.routing.enable-chained-transaction-manager=true",
                    "datasource.routing.multi.first.master=master1",
                    "datasource.routing.multi.first.slave=slave1",
                    "datasource.routing.multi.second.switch-off=switchoff1",
                    "datasource.routing.multi.second.switch-on=switchon1",
                    "datasource.routing.multi.third.standalone=other",
                    "datasource.routing.multi.forth.defaults=master1",
                    "datasource.routing.multi.forth.mapped.first=other",
                    "datasource.routing.targets.master1.type=org.apache.tomcat.jdbc.pool.DataSource",
                    "datasource.routing.targets.master1.url=jdbc:h2:mem:MASTER1;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
                    "datasource.routing.targets.master1.driver-class-name=org.h2.Driver",
                    "datasource.routing.targets.master1.initialize=false",
                    "datasource.routing.targets.master1.username=sa",
                    "datasource.routing.targets.master1.tomcat.max-active=10",
                    "datasource.routing.targets.slave1.type=com.zaxxer.hikari.HikariDataSource",
                    "datasource.routing.targets.slave1.url=jdbc:h2:mem:SWITCHON2;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
                    "datasource.routing.targets.slave1.driver-class-name=org.h2.Driver",
                    "datasource.routing.targets.slave1.initialize=false",
                    "datasource.routing.targets.slave1.username=sa",
                    "datasource.routing.targets.slave1.hikari.maximum-pool-size=9",
                    "datasource.routing.targets.switchoff1.type=org.apache.tomcat.jdbc.pool.DataSource",
                    "datasource.routing.targets.switchoff1.url=jdbc:h2:mem:SWITCHOFF1;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
                    "datasource.routing.targets.switchoff1.driver-class-name=org.h2.Driver",
                    "datasource.routing.targets.switchoff1.initialize=false",
                    "datasource.routing.targets.switchoff1.username=sa",
                    "datasource.routing.targets.switchoff1.tomcat.max-active=8",
                    "datasource.routing.targets.switchon1.type=com.zaxxer.hikari.HikariDataSource",
                    "datasource.routing.targets.switchon1.url=jdbc:h2:mem:SWITCHON1;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
                    "datasource.routing.targets.switchon1.driver-class-name=org.h2.Driver",
                    "datasource.routing.targets.switchon1.initialize=false",
                    "datasource.routing.targets.switchon1.username=sa",
                    "datasource.routing.targets.switchon1.hikari.maximum-pool-size=7",
                    "datasource.routing.targets.other.type=com.zaxxer.hikari.HikariDataSource",
                    "datasource.routing.targets.other.url=jdbc:h2:mem:SWITCHON2;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
                    "datasource.routing.targets.other.driver-class-name=org.h2.Driver",
                    "datasource.routing.targets.other.initialize=false",
                    "datasource.routing.targets.other.username=sa",
                    "datasource.routing.targets.other.hikari.maximum-pool-size=6"
        );
        assertAll(
            () -> assertThat(dataSource("first")).isNotNull(),
            () -> assertThat(dataSource("first").getTargetDataSource()).isInstanceOf(DelegateRoutingDataSource.class),
            () -> assertThat(context.getBean("firstTransactionManager", PlatformTransactionManager.class)).isNotNull(),
            () -> assertThat(context.getBean("firstTransactionTemplate", TransactionTemplate.class)).isNotNull(),
            () -> assertThat(master().getMaxActive()).isEqualTo(10),
            () -> assertThat(slave().getMaximumPoolSize()).isEqualTo(9),
            () -> assertThat(dataSource("second")).isNotNull(),
            () -> assertThat(dataSource("second").getTargetDataSource()).isInstanceOf(DelegateRoutingDataSource.class),
            () -> assertThat(context.getBean("secondTransactionManager", PlatformTransactionManager.class)).isNotNull(),
            () -> assertThat(off().getMaxActive()).isEqualTo(8),
            () -> assertThat(on().getMaximumPoolSize()).isEqualTo(7),
            () -> assertThat(dataSource("third", LazyConnectionDataSourceAdaptor.class)).isNotNull(),
            () -> assertThat(dataSource("third",
                                        LazyConnectionDataSourceAdaptor.class).getTargetDataSource()).isInstanceOf(HikariDataSource.class),
            () -> assertThat(context.getBean("thirdTransactionManager", PlatformTransactionManager.class)).isNotNull(),
            () -> assertThat(third().getMaximumPoolSize()).isEqualTo(6),
            () -> assertThat(dataSource("forth")).isNotNull(),
            () -> assertThat(dataSource("forth").getTargetDataSource()).isInstanceOf(DelegateRoutingDataSource.class),
            () -> assertThat(context.getBean("forthTransactionManager", PlatformTransactionManager.class)).isNotNull(),
            () -> assertThat(context.getBean("transactionManager", ChainedTransactionManager.class)).isNotNull(),
            () -> assertThat(routingDataSourcePublicMetrics().metrics()).isNotEmpty()
        );
    }

    private org.apache.tomcat.jdbc.pool.DataSource master() {
        return targetDataSource("first", Master, org.apache.tomcat.jdbc.pool.DataSource.class);
    }

    private HikariDataSource slave() {
        return targetDataSource("first", Slave, HikariDataSource.class);
    }

    private org.apache.tomcat.jdbc.pool.DataSource off() {
        return targetDataSource("second", Off, org.apache.tomcat.jdbc.pool.DataSource.class);
    }

    private HikariDataSource on() {
        return targetDataSource("second", On, HikariDataSource.class);
    }

    private <T> T targetDataSource(String name, Object key, Class<T> target) {
        return target.cast(resolvedDataSources(name).get(key));
    }

    private Map resolvedDataSources(String name) {
        return Map.class.cast(ReflectionTestUtils.getField(ruleBaseRoutingDataSource(name), "resolvedDataSources"));
    }

    private DelegateRoutingDataSource ruleBaseRoutingDataSource(String name) {
        return DelegateRoutingDataSource.class.cast(dataSource(name).getTargetDataSource());
    }

    private HikariDataSource third() {
        return HikariDataSource.class.cast(dataSource("third", LazyConnectionDataSourceAdaptor.class).getTargetDataSource());
    }

    private DataSource dataSource() {
        return context.getBean(DataSource.class);
    }

    private LazyConnectionDataSourceProxy dataSource(String name) {
        return dataSource(name, LazyConnectionDataSourceProxy.class);
    }

    private <T> T dataSource(String name, Class<T> clazz) {
        return context.getBean(name + "DataSource", clazz);
    }

    private RoutingDataSourcePublicMetrics routingDataSourcePublicMetrics() {
        return context.getBean(RoutingDataSourcePublicMetrics.class);
    }

    @Test
    void should_be_not_loaded_MultiRoutingDataSource_when_multi_not_found() {
        assertThatThrownBy(() -> loadContext("datasource.routing.enabled=true",
                                             "datasource.routing.multi.first.standalone=notfound",
                                             "datasource.routing.targets.first.url=jdbc:h2:mem:SWITCHOFF",
                                             "datasource.routing.targets.first.driver-class-name=org.h2.Driver",
                                             "datasource.routing.targets.first.initialize=false",
                                             "datasource.routing.targets.first.username=sa"))
            .isExactlyInstanceOf(BeanDefinitionValidationException.class);
    }

    @Test
    void should_be_not_loaded_MultiRoutingDataSource_when_multi_mapped_not_found() {
        assertThatThrownBy(() -> loadContext("datasource.routing.enabled=true",
                                             "datasource.routing.multi.first.defaults=first",
                                             "datasource.routing.multi.first.mapped.second=notfound",
                                             "datasource.routing.targets.first.url=jdbc:h2:mem:SWITCHOFF",
                                             "datasource.routing.targets.first.driver-class-name=org.h2.Driver",
                                             "datasource.routing.targets.first.initialize=false",
                                             "datasource.routing.targets.first.username=sa"))
            .isExactlyInstanceOf(BeanDefinitionValidationException.class);
    }

    @Test
    void should_be_not_loaded_MultiRoutingDataSource_when_multi_not_exists() {
        loadContext("datasource.routing.enabled=true",
                    "datasource.routing.targets.first.url=jdbc:h2:mem:SWITCHOFF",
                    "datasource.routing.targets.first.driver-class-name=org.h2.Driver",
                    "datasource.routing.targets.first.initialize=false",
                    "datasource.routing.targets.first.username=sa");
        assertThatThrownBy(this::dataSource).isExactlyInstanceOf(NoSuchBeanDefinitionException.class);
    }

    @Test
    void should_be_not_loaded_MultiRoutingDataSource_when_sources_not_exists() {
        loadContext("datasource.routing.enabled=true",
                    "datasource.routing.multi.first.standalone=test");
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
