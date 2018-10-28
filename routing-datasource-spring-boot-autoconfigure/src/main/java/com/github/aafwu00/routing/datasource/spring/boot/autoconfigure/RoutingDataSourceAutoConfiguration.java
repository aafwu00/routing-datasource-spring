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

import java.util.Arrays;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import static com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.RoutingCondition.PREFIX;

/**
 * @author Taeho Kim
 */
@Configuration
@ConditionalOnProperty(PREFIX + "enabled")
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@Import(RoutingDataSourceAutoConfiguration.RoutingConfigurationImportSelector.class)
public class RoutingDataSourceAutoConfiguration {
    /**
     * {@link ImportSelector} to add {@link RoutingType} configuration classes.
     */
    static class RoutingConfigurationImportSelector implements ImportSelector {
        @Override
        public String[] selectImports(final AnnotationMetadata importingClassMetadata) {
            return Arrays.stream(RoutingType.values())
                         .map(RoutingType::getConfigurationClass)
                         .toArray(String[]::new);
        }
    }
}
