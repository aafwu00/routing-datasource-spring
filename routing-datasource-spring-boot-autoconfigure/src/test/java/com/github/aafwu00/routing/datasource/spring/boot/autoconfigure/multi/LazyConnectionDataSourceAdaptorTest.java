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

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author Taeho Kim
 */
class LazyConnectionDataSourceAdaptorTest {
    private DataSource targetDataSource;
    private Connection connection;
    private MultiRoutingDataSourcesHolder holder;
    private String name;

    @BeforeEach
    void setUp() {
        targetDataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        holder = mock(MultiRoutingDataSourcesHolder.class);
        name = "test";
    }

    @Test
    void initialization() throws SQLException {
        doReturn(targetDataSource).when(holder).determine(name);
        doReturn(connection).when(targetDataSource).getConnection();
        final LazyConnectionDataSourceAdaptor lazyConnectionDataSourceAdaptor = new LazyConnectionDataSourceAdaptor(holder, name);
        assertThat(lazyConnectionDataSourceAdaptor.getTargetDataSource()).isEqualTo(targetDataSource);
    }
}
