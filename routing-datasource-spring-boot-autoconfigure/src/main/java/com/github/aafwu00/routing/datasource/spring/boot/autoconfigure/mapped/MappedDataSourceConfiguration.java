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
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.RoutingCondition;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.RoutingDataSourceAvailableCondition;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.RoutingType;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.support.DelegateRoutingDataSourceFactory;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.support.RoutingDataSourcePublicMetrics;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.support.TargetDataSources;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.support.TargetDataSourcesFactory;

import static com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.RoutingCondition.PREFIX;
import static com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.mapped.MappedDataSourceProperties.DEFAULTS;
import static com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.mapped.MappedDataSourceProperties.MAPPED;

/**
 * @author Taeho Kim
 */
@Configuration
@Conditional({RoutingCondition.class, MappedDataSourceConfiguration.MappedAvailableCondition.class})
@EnableConfigurationProperties(MappedDataSourceProperties.class)
public class MappedDataSourceConfiguration {
    @Bean
    public DelegateRoutingDataSourceFactory<String> dataSource(final RoutingRule<String> routingRule,
                                                               final TargetDataSources<String> dataSources) {
        return new DelegateRoutingDataSourceFactory<>(routingRule, dataSources);
    }

    @Bean
    @ConditionalOnMissingBean
    public TargetDataSourcesFactory<String> targetDataSources(final MappedDataSourceProperties properties) {
        final TargetDataSourcesFactory<String> result = new TargetDataSourcesFactory<>(DEFAULTS,
                                                                                       properties.getDefaults(),
                                                                                       PREFIX + DEFAULTS);
        properties.getMapped().forEach((key, dataSourceProperties) -> result.with(key, dataSourceProperties, PREFIX + MAPPED + "." + key));
        return result;
    }

    @Bean
    @ConditionalOnMissingBean
    public RoutingRule<String> routingRule() {
        return () -> "";
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

    static class MappedAvailableCondition extends RoutingDataSourceAvailableCondition {
        MappedAvailableCondition() {
            super("Mapped Routing DataSource", DEFAULTS, MAPPED);
        }
    }
}
