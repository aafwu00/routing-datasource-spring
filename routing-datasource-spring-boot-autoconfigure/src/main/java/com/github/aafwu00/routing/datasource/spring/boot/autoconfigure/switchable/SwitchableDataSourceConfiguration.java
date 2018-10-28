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

import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import com.github.aafwu00.routing.datasource.spring.RoutingRule;
import com.github.aafwu00.routing.datasource.spring.SwitchableMode;
import com.github.aafwu00.routing.datasource.spring.SwitchableRoutingRuleImpl;
import com.github.aafwu00.routing.datasource.spring.Switching;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.RoutingCondition;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.RoutingDataSourceAvailableCondition;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.RoutingType;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.support.DelegateRoutingDataSourceFactory;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.support.RoutingDataSourcePublicMetrics;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.support.TargetDataSources;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.support.TargetDataSourcesFactory;

import static com.github.aafwu00.routing.datasource.spring.SwitchableMode.Off;
import static com.github.aafwu00.routing.datasource.spring.SwitchableMode.On;
import static com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.RoutingCondition.PREFIX;
import static com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.switchable.SwitchableDataSourceProperties.SWITCH_OFF;
import static com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.switchable.SwitchableDataSourceProperties.SWITCH_ON;

/**
 * @author Taeho Kim
 */
@Configuration
@Conditional({RoutingCondition.class, SwitchableDataSourceConfiguration.SwitchableAvailableCondition.class})
@EnableConfigurationProperties(SwitchableDataSourceProperties.class)
public class SwitchableDataSourceConfiguration {
    @Bean
    public DelegateRoutingDataSourceFactory<SwitchableMode> dataSource(final RoutingRule<SwitchableMode> routingRule,
                                                                       final TargetDataSources<SwitchableMode> dataSources) {
        return new DelegateRoutingDataSourceFactory<>(routingRule, dataSources);
    }

    @Bean
    @ConditionalOnMissingBean
    public TargetDataSourcesFactory<SwitchableMode> targetDataSources(final SwitchableDataSourceProperties properties) {
        return new TargetDataSourcesFactory<SwitchableMode>()
            .with(Off, properties.getSwitchOff(), PREFIX + SWITCH_OFF)
            .with(On, properties.getSwitchOn(), PREFIX + SWITCH_ON);
    }

    @Bean
    @ConditionalOnMissingBean
    public RoutingRule<SwitchableMode> routingRule(final Switching switching) {
        return new SwitchableRoutingRuleImpl(switching);
    }

    @Bean
    @ConditionalOnMissingBean
    public Switching switching() {
        return () -> false;
    }

    @Configuration
    @ConditionalOnProperty(value = RoutingType.SCOPE + ".metrics.enabled", matchIfMissing = true)
    @ConditionalOnClass(PublicMetrics.class)
    static class RoutingDataSourcePublicMetricsConfiguration {
        @Bean
        public RoutingDataSourcePublicMetrics<SwitchableMode> routingDataSourcePublicMetrics(
            final TargetDataSources<SwitchableMode> targetDataSources) {
            return new RoutingDataSourcePublicMetrics<>(targetDataSources);
        }
    }

    static class SwitchableAvailableCondition extends RoutingDataSourceAvailableCondition {
        SwitchableAvailableCondition() {
            super("Switchable Routing DataSource", SWITCH_OFF, SWITCH_ON);
        }
    }
}
