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

import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.github.aafwu00.routing.datasource.spring.MappedRoutingRule;
import com.github.aafwu00.routing.datasource.spring.ReplicationRoutingRule;
import com.github.aafwu00.routing.datasource.spring.ReplicationRoutingRuleImpl;
import com.github.aafwu00.routing.datasource.spring.SwitchableRoutingRule;
import com.github.aafwu00.routing.datasource.spring.SwitchableRoutingRuleImpl;
import com.github.aafwu00.routing.datasource.spring.Switching;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.RoutingCondition;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.RoutingDataSourceAvailableCondition;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.RoutingType;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.support.RoutingDataSourcePublicMetrics;

import static com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.multi.MultiRoutingDataSourceProperties.MULTI;
import static com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.multi.MultiRoutingDataSourceProperties.TARGETS;

/**
 * @author Taeho Kim
 */
@Configuration
@Conditional({RoutingCondition.class, MultiRoutingDataSourceConfiguration.MultiRoutingAvailableCondition.class})
public class MultiRoutingDataSourceConfiguration {
    @Bean
    public static MultiRoutingDataSourceRegistrar multiRoutingDataSourceRegistrar() {
        return new MultiRoutingDataSourceRegistrar();
    }

    @Bean
    @ConditionalOnMissingBean(Switching.class)
    public Switching switching() {
        return () -> false;
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(SwitchableRoutingRule.class)
    @ConditionalOnSingleCandidate(Switching.class)
    public SwitchableRoutingRule switchableRoutingRule(final Switching switching) {
        return new SwitchableRoutingRuleImpl(switching);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(ReplicationRoutingRule.class)
    public ReplicationRoutingRule replicationRoutingRule() {
        return new ReplicationRoutingRuleImpl();
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(MappedRoutingRule.class)
    public MappedRoutingRule mappedRoutingRule() {
        return () -> null;
    }

    @Configuration
    @ConditionalOnProperty(value = RoutingType.SCOPE + ".metrics.enabled", matchIfMissing = true)
    @ConditionalOnClass(PublicMetrics.class)
    static class RoutingDataSourcePublicMetricsConfiguration {
        @Bean
        public RoutingDataSourcePublicMetrics<String> routingDataSourcePublicMetrics(
            final MultiRoutingDataSourcesHolder multiRoutingDataSourcesHolder) {
            return new RoutingDataSourcePublicMetrics<>(multiRoutingDataSourcesHolder.getTargetDataSources());
        }
    }

    static class MultiRoutingAvailableCondition extends RoutingDataSourceAvailableCondition {
        MultiRoutingAvailableCondition() {
            super("Multi Routing DataSource", MULTI, TARGETS);
        }
    }
}
