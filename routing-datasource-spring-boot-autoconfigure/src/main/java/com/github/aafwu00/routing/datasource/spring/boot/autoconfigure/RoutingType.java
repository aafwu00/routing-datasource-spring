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

import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.mapped.MappedDataSourceConfiguration;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.multi.MultiRoutingDataSourceConfiguration;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.replication.ReplicationDataSourceConfiguration;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.switchable.SwitchableDataSourceConfiguration;

import static java.util.Objects.requireNonNull;

/**
 * @author Taeho Kim
 */
public enum RoutingType {
    Replication(ReplicationDataSourceConfiguration.class),
    Switchable(SwitchableDataSourceConfiguration.class),
    Mapped(MappedDataSourceConfiguration.class),
    MultiRouting(MultiRoutingDataSourceConfiguration.class);
    public static final String SCOPE = "datasource.routing";
    private final Class<?> configurationClass;

    RoutingType(final Class<?> configurationClass) {
        this.configurationClass = requireNonNull(configurationClass);
    }

    public static RoutingType getType(final String configurationClassName) {
        return Arrays.stream(values())
                     .filter(type -> configurationClassName.equals(type.configurationClass.getName()))
                     .findFirst()
                     .orElseGet(() -> {
                         throw new IllegalStateException("Unknown configuration class " + configurationClassName);
                     });
    }

    public String getConfigurationClass() {
        return configurationClass.getName();
    }
}
