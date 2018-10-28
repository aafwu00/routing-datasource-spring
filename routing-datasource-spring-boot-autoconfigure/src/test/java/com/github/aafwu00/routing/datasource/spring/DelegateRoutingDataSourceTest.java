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

package com.github.aafwu00.routing.datasource.spring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author Taeho Kim
 */
class DelegateRoutingDataSourceTest {
    private DelegateRoutingDataSource<ReplicationType> dataSource;
    private ReplicationRoutingRule rule;

    @BeforeEach
    void setUp() {
        rule = mock(ReplicationRoutingRule.class);
        dataSource = new DelegateRoutingDataSource<>(rule);
    }

    @Test
    void determineCurrentLookupKey() {
        doReturn(ReplicationType.Slave).when(rule).determineCurrentLookupKey();
        assertThat(dataSource.determineCurrentLookupKey()).isEqualTo(ReplicationType.Slave);
        doReturn(ReplicationType.Master).when(rule).determineCurrentLookupKey();
        assertThat(dataSource.determineCurrentLookupKey()).isEqualTo(ReplicationType.Master);
    }
}
