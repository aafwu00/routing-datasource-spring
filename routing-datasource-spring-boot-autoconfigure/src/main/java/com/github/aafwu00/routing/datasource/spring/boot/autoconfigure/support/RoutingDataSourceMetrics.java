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

package com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.support;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ToDoubleFunction;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.metadata.DataSourcePoolMetadata;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

/**
 * @author Taeho Kim
 */
public class RoutingDataSourceMetrics<T> implements MeterBinder {
    private final Map<String, DataSourcePoolMetadata> metadataByPrefix = new ConcurrentHashMap<>();

    public RoutingDataSourceMetrics(final TargetDataSources<T> dataSources) {
        dataSources.getDataSources().forEach(this::putIfPresent);
        dataSources.getDefaults().flatMap(this::metadata).ifPresent(metadata -> metadataByPrefix.putIfAbsent("defaults", metadata));
    }

    private void putIfPresent(final T name, final DataSource dataSource) {
        metadata(dataSource).ifPresent(metadata -> put(name, metadata));
    }

    private Optional<DataSourcePoolMetadata> metadata(final DataSource dataSource) {
        return DataSourceType.valueOf(dataSource)
                             .map(type -> type.create(dataSource));
    }

    private void put(final T name, final DataSourcePoolMetadata metadata) {
        metadataByPrefix.put(key(name), metadata);
    }

    private String key(final T name) {
        return "datasource." + name.toString().toLowerCase(Locale.getDefault());
    }

    @Override
    public void bindTo(final MeterRegistry registry) {
        metadataByPrefix.forEach((prefix, metadata) -> {
            bindTo(registry, prefix, "active", metadata, DataSourcePoolMetadata::getActive);
            bindTo(registry, prefix, "usage", metadata, DataSourcePoolMetadata::getUsage);
            bindTo(registry, prefix, "max", metadata, DataSourcePoolMetadata::getMax);
            bindTo(registry, prefix, "min", metadata, DataSourcePoolMetadata::getMin);
        });
    }

    private void bindTo(final MeterRegistry registry,
                        final String prefix,
                        final String name,
                        final DataSourcePoolMetadata metadata,
                        final ToDoubleFunction<DataSourcePoolMetadata> function) {
        Gauge.builder(prefix + "." + name, metadata, function)
             .tag("datasource", name)
             .register(registry);
    }
}
