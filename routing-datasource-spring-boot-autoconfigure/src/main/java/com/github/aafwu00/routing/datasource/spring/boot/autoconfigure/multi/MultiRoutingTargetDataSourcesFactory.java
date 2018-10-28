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

import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.support.TargetDataSources;

import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

/**
 * @author Taeho Kim
 */
public class MultiRoutingTargetDataSourcesFactory<T> implements FactoryBean<TargetDataSources<T>>,
                                                                InitializingBean {
    private final MultiRoutingDataSourcesHolder holder;
    private final Map<T, String> keys;
    private final Optional<String> defaults;
    private TargetDataSources<T> target;

    public MultiRoutingTargetDataSourcesFactory(final MultiRoutingDataSourcesHolder holder,
                                                final Map<T, String> keys,
                                                final Optional<String> defaults) {
        this.holder = requireNonNull(holder);
        this.keys = unmodifiableMap(requireNonNull(keys));
        this.defaults = requireNonNull(defaults);
    }

    public MultiRoutingTargetDataSourcesFactory(final MultiRoutingDataSourcesHolder holder,
                                                final Map<T, String> keys,
                                                final String defaults) {
        this(holder, keys, Optional.of(defaults));
    }

    public MultiRoutingTargetDataSourcesFactory(final MultiRoutingDataSourcesHolder holder, final Map<T, String> keys) {
        this(holder, keys, Optional.empty());
    }

    @Override
    public TargetDataSources<T> getObject() {
        return target;
    }

    @Override
    public Class<?> getObjectType() {
        return TargetDataSources.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() {
        final Map<T, DataSource> dataSources = keys.entrySet()
                                                   .stream()
                                                   .collect(toMap(Map.Entry::getKey, key -> holder.determine(key.getValue())));
        target = new TargetDataSources<>(dataSources, defaults.map(holder::determine));
    }
}
