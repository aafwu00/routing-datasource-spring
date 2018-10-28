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

import java.util.Locale;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;

/**
 * @author Taeho Kim
 */
public class RoutingCondition extends SpringBootCondition {
    public static final String PREFIX = RoutingType.SCOPE + ".";

    @Override
    public ConditionOutcome getMatchOutcome(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        String sourceClass = "";
        if (metadata instanceof ClassMetadata) {
            sourceClass = ClassMetadata.class.cast(metadata).getClassName();
        }
        final ConditionMessage.Builder message = ConditionMessage.forCondition("Routing DataSource", sourceClass);
        final RelaxedPropertyResolver resolver = new RelaxedPropertyResolver(context.getEnvironment(), PREFIX);
        if (!resolver.containsProperty("type")) {
            return ConditionOutcome.match(message.because("automatic routing type"));
        }
        final RoutingType routingType = RoutingType.getType(AnnotationMetadata.class.cast(metadata).getClassName());
        final String value = resolver.getProperty("type").replace('-', '_').toUpperCase(Locale.getDefault());
        if (value.equals(routingType.name().toUpperCase(Locale.getDefault()))) {
            return ConditionOutcome.match(message.because(value + " routing type"));
        }
        return ConditionOutcome.noMatch(message.because(value + " routing type"));
    }
}
