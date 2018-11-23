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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import static com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.RoutingCondition.PREFIX;
import static java.util.Collections.emptyMap;

/**
 * @author Taeho Kim
 */
public class RoutingDataSourceAvailableCondition extends SpringBootCondition {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoutingDataSourceAvailableCondition.class);
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
        if (hasNotSubProperties(context, defaults)) {
            return ConditionOutcome.noMatch(message.didNotFind(PREFIX + defaults).atAll());
        }
        if (hasNotSubProperties(context, fallback)) {
            return ConditionOutcome.noMatch(message.didNotFind(PREFIX + fallback).atAll());
        }
        return ConditionOutcome.match(message.because("Routing " + PREFIX + "[" + defaults + "," + fallback + "] specified"));
    }

    private boolean hasNotSubProperties(final ConditionContext context, final String name) {
        try {
            return Binder.get(context.getEnvironment())
                         .bind(ConfigurationPropertyName.of(PREFIX + name), Bindable.mapOf(String.class, String.class))
                         .orElse(emptyMap())
                         .isEmpty();
            // CHECKSTYLE:OFF
        } catch (Exception ex) {
            // CHECKSTYLE:ON
            LOGGER.debug("`{}{}` is empty", PREFIX, name);
            return true;
        }
    }
}

