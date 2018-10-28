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

package com.github.aafwu00.routing.datasource.spring.boot.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import static com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.RoutingCondition.PREFIX;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * @author Taeho Kim
 */
public class RoutingDataSourceAvailableCondition extends SpringBootCondition {
    private final String condition;
    private final String fallback;
    private final String defaults;

    public RoutingDataSourceAvailableCondition(final String condition, final String fallback, final String defaults) {
        super();
        this.condition = condition;
        this.fallback = fallback;
        this.defaults = defaults;
    }

    @Override
    public ConditionOutcome getMatchOutcome(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        final ConditionMessage.Builder message = ConditionMessage.forCondition(condition);
        final RelaxedPropertyResolver resolver = new RelaxedPropertyResolver(context.getEnvironment(), PREFIX);
        if (isEmpty(resolver.getSubProperties(defaults + "."))) {
            return ConditionOutcome.noMatch(message.didNotFind(PREFIX + defaults).atAll());
        }
        if (isEmpty(resolver.getSubProperties(fallback + "."))) {
            return ConditionOutcome.noMatch(message.didNotFind(PREFIX + fallback).atAll());
        }
        return ConditionOutcome.match(message.because("Routing " + PREFIX + "[" + defaults + "," + fallback + "] specified"));
    }
}
