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

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.support.TargetDataSources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author Taeho Kim
 */
class MultiRoutingTargetDataSourcesFactoryTest {
    private MultiRoutingDataSourcesHolder holder;
    private MultiRoutingTargetDataSourcesFactory<String> factory;

    @BeforeEach
    void setUp() {
        holder = mock(MultiRoutingDataSourcesHolder.class);
        final Map<String, String> keys = new HashMap<>();
        keys.put("firstKey", "firstName");
        keys.put("secondKey", "secondName");
        factory = new MultiRoutingTargetDataSourcesFactory<>(holder, keys, "defaults");
    }

    @Test
    void getObjectType() {
        assertThat(factory.getObjectType()).isEqualTo(TargetDataSources.class);
    }

    @Test
    void isSingleton() {
        assertThat(factory.isSingleton()).isTrue();
    }

    @Test
    void afterPropertiesSet() {
        final DataSource dataSource = mock(DataSource.class);
        final DataSource dataSource1 = mock(DataSource.class);
        final DataSource dataSource2 = mock(DataSource.class);
        doReturn(dataSource).when(holder).determine("defaults");
        doReturn(dataSource1).when(holder).determine("firstName");
        doReturn(dataSource2).when(holder).determine("secondName");
        factory.afterPropertiesSet();
        final TargetDataSources<String> target = factory.getObject();
        assertAll(
            () -> assertThat(target.determine("firstKey")).contains(dataSource1),
            () -> assertThat(target.determine("secondKey")).contains(dataSource2),
            () -> assertThat(target.getDefaults()).contains(dataSource),
            () -> assertThat(factory.getObject()).isSameAs(target)
        );
    }
}
