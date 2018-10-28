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

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.metrics.Metric;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * @author Taeho Kim
 */
class RoutingDataSourcePublicMetricsTest {
    @Test
    void metrics() {
        final Map<String, DataSource> dataSources = new HashMap<>();
        dataSources.put("key1", new BasicDataSource());
        dataSources.put("KEY2", new BasicDataSource());
        final TargetDataSources<String> targetDataSources = new TargetDataSources<>(dataSources);
        final RoutingDataSourcePublicMetrics<String> metrics = new RoutingDataSourcePublicMetrics<>(targetDataSources);
        assertAll(
            () -> assertThat(metricNames(metrics)).contains("datasource.key1.active"),
            () -> assertThat(metricNames(metrics)).contains("datasource.key1.usage"),
            () -> assertThat(metricNames(metrics)).contains("datasource.key1.max"),
            () -> assertThat(metricNames(metrics)).contains("datasource.key2.active"),
            () -> assertThat(metricNames(metrics)).contains("datasource.key2.usage"),
            () -> assertThat(metricNames(metrics)).contains("datasource.key2.max")
        );
    }

    private Stream<String> metricNames(final RoutingDataSourcePublicMetrics<String> metrics) {
        return metrics.metrics().stream().map(Metric::getName);
    }
}
