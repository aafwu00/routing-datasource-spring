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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.boot.autoconfigure.jdbc.metadata.DataSourcePoolMetadata;

import static java.util.Objects.nonNull;

/**
 * @author Taeho Kim
 */
public class RoutingDataSourcePublicMetrics<T> implements PublicMetrics {
    private final Map<String, DataSourcePoolMetadata> metadataByPrefix = new ConcurrentHashMap<>();

    public RoutingDataSourcePublicMetrics(final TargetDataSources<T> dataSources) {
        dataSources.getDataSources().forEach(this::putMetadataIfPresent);
        dataSources.getDefaults().flatMap(this::metadata).ifPresent(metadata -> metadataByPrefix.putIfAbsent("defaults", metadata));
    }

    private void putMetadataIfPresent(final T name, final DataSource dataSource) {
        metadata(dataSource).ifPresent(metadata -> putMetadata(name, metadata));
    }

    private DataSourcePoolMetadata putMetadata(final T name, final DataSourcePoolMetadata metadata) {
        return metadataByPrefix.put(prefix(name), metadata);
    }

    private String prefix(final T name) {
        return "datasource." + name.toString().toLowerCase(Locale.getDefault());
    }

    private Optional<DataSourcePoolMetadata> metadata(final DataSource dataSource) {
        return DataSourceType.valueOf(dataSource)
                             .map(type -> type.create(dataSource));
    }

    @Override
    public Collection<Metric<?>> metrics() {
        final Set<Metric<?>> metrics = new LinkedHashSet<>();
        metadataByPrefix.forEach((prefix, metadata) -> {
            addMetric(metrics, prefix + ".active", metadata.getActive());
            addMetric(metrics, prefix + ".usage", metadata.getUsage());
            addMetric(metrics, prefix + ".max", metadata.getMax());
        });
        return metrics;
    }

    private <T extends Number> void addMetric(final Set<Metric<?>> metrics, final String name, final T value) {
        if (nonNull(value)) {
            metrics.add(new Metric<>(name, value));
        }
    }
}
