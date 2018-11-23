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
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;

/**
 * @author Taeho Kim
 */
public class RoutingCondition extends SpringBootCondition {
    public static final String PREFIX = RoutingType.SCOPE + ".";
    private static final String TYPE = PREFIX + "type";

    @Override
    public ConditionOutcome getMatchOutcome(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        final ConditionMessage.Builder message = messageBuilder(metadata);
        if (!environment(context).containsProperty(TYPE)) {
            return ConditionOutcome.match(message.because("automatic routing type"));
        }
        return getStrictTypeMatchOutcome(context, metadata, message);
    }

    private ConditionMessage.Builder messageBuilder(final AnnotatedTypeMetadata metadata) {
        String sourceClass = "";
        if (metadata instanceof ClassMetadata) {
            sourceClass = ClassMetadata.class.cast(metadata).getClassName();
        }
        return ConditionMessage.forCondition("Routing DataSource", sourceClass);
    }

    private ConditionOutcome getStrictTypeMatchOutcome(final ConditionContext context,
                                                       final AnnotatedTypeMetadata metadata,
                                                       final ConditionMessage.Builder message) {
        final String type = environment(context).getProperty(TYPE);
        final BindResult<RoutingType> bindResult = bind(context);
        if (bindResult.isBound()) {
            if (routingType(metadata).equals(bindResult.get())) {
                return ConditionOutcome.match(message.because(type + " routing type"));
            }
            return ConditionOutcome.noMatch(message.didNotFind("'" + type + "' is wrong").atAll());
        }
        return ConditionOutcome.noMatch(message.because(type + " routing type"));
    }

    private Environment environment(final ConditionContext context) {
        return context.getEnvironment();
    }

    private BindResult<RoutingType> bind(final ConditionContext context) {
        return Binder.get(environment(context))
                     .bind(TYPE, Bindable.of(RoutingType.class));
    }

    private RoutingType routingType(final AnnotatedTypeMetadata metadata) {
        return RoutingType.getType(AnnotationMetadata.class.cast(metadata).getClassName());
    }
}
