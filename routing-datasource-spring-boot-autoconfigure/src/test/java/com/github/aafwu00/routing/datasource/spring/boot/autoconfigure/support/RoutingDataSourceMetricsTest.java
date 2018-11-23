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

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * @author Taeho Kim
 */
class RoutingDataSourceMetricsTest {
    @Test
    void metrics() {
        final Map<String, DataSource> dataSources = new HashMap<>();
        dataSources.put("key1", new BasicDataSource());
        dataSources.put("KEY2", new BasicDataSource());
        final TargetDataSources<String> targetDataSources = new TargetDataSources<>(dataSources);
        final RoutingDataSourceMetrics<String> metrics = new RoutingDataSourceMetrics<>(targetDataSources);
        final SimpleMeterRegistry register = new SimpleMeterRegistry();
        metrics.bindTo(register);
        assertAll(
            () -> assertThat(register.get("datasource.key1.active").gauge().value()).isNotNegative(),
            () -> assertThat(register.get("datasource.key1.usage").gauge().value()).isNotNegative(),
            () -> assertThat(register.get("datasource.key1.max").gauge().value()).isNotNegative(),
            () -> assertThat(register.get("datasource.key1.min").gauge().value()).isNotNegative(),
            () -> assertThat(register.get("datasource.key2.active").gauge().value()).isNotNegative(),
            () -> assertThat(register.get("datasource.key2.usage").gauge().value()).isNotNegative(),
            () -> assertThat(register.get("datasource.key2.max").gauge().value()).isNotNegative(),
            () -> assertThat(register.get("datasource.key2.min").gauge().value()).isNotNegative()
        );
    }
}
