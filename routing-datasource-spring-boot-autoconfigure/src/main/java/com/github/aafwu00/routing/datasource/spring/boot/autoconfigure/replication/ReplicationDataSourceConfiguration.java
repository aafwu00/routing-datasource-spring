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

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import com.github.aafwu00.routing.datasource.spring.ReplicationRoutingRuleImpl;
import com.github.aafwu00.routing.datasource.spring.ReplicationType;
import com.github.aafwu00.routing.datasource.spring.RoutingRule;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.RoutingCondition;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.RoutingDataSourceAvailableCondition;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.RoutingType;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.support.DelegateRoutingDataSourceFactory;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.support.RoutingDataSourceMetrics;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.support.TargetDataSources;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.support.TargetDataSourcesFactory;

import io.micrometer.core.instrument.binder.MeterBinder;

import static com.github.aafwu00.routing.datasource.spring.ReplicationType.Master;
import static com.github.aafwu00.routing.datasource.spring.ReplicationType.Slave;
import static com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.RoutingCondition.PREFIX;
import static com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.replication.ReplicationDataSourceProperties.MASTER;
import static com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.replication.ReplicationDataSourceProperties.SLAVE;

/**
 * @author Taeho Kim
 */
@Configuration
@Conditional({RoutingCondition.class, ReplicationDataSourceConfiguration.ReplicationAvailableCondition.class})
@EnableConfigurationProperties(ReplicationDataSourceProperties.class)
public class ReplicationDataSourceConfiguration {
    @Bean
    public DelegateRoutingDataSourceFactory<ReplicationType> dataSource(final RoutingRule<ReplicationType> routingRule,
                                                                        final TargetDataSources<ReplicationType> dataSources) {
        return new DelegateRoutingDataSourceFactory<>(routingRule, dataSources);
    }

    @Bean
    @ConditionalOnMissingBean
    public TargetDataSourcesFactory<ReplicationType> targetDataSources(final ReplicationDataSourceProperties properties) {
        return new TargetDataSourcesFactory<ReplicationType>().with(Master, properties.getMaster(), PREFIX + MASTER)
                                                              .with(Slave, properties.getSlave(), PREFIX + SLAVE);
    }

    @Bean
    @ConditionalOnMissingBean
    public RoutingRule<ReplicationType> routingRule() {
        return new ReplicationRoutingRuleImpl();
    }

    @Configuration
    @ConditionalOnProperty(value = RoutingType.SCOPE + ".metrics.enabled", matchIfMissing = true)
    @ConditionalOnClass(MeterBinder.class)
    static class RoutingDataSourceMetricsConfiguration {
        @Bean
        public RoutingDataSourceMetrics<ReplicationType> routingDataSourceMetrics(
            final TargetDataSources<ReplicationType> targetDataSources) {
            return new RoutingDataSourceMetrics<>(targetDataSources);
        }
    }

    static class ReplicationAvailableCondition extends RoutingDataSourceAvailableCondition {
        ReplicationAvailableCondition() {
            super("Replication Routing DataSource", MASTER, SLAVE);
        }
    }
}
