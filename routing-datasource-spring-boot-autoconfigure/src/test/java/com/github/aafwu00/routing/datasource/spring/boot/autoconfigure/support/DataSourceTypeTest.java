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

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.metadata.CommonsDbcp2DataSourcePoolMetadata;
import org.springframework.boot.jdbc.metadata.HikariDataSourcePoolMetadata;
import org.springframework.boot.jdbc.metadata.TomcatDataSourcePoolMetadata;

import com.zaxxer.hikari.HikariDataSource;

import static com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.support.DataSourceType.DBCP2;
import static com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.support.DataSourceType.Hikari;
import static com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.support.DataSourceType.Tomcat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * @author Taeho Kim
 */
class DataSourceTypeTest {
    @Test
    void valueOf() {
        assertAll(
            () -> assertThat(DataSourceType.valueOf(new HikariDataSource())).contains(Hikari),
            () -> assertThat(DataSourceType.valueOf(new DataSource())).contains(Tomcat),
            () -> assertThat(DataSourceType.valueOf(new BasicDataSource())).contains(DBCP2)
        );
    }

    @Test
    void isSameType() {
        assertAll(
            () -> assertThat(Hikari.isSameType(new HikariDataSource())).isTrue(),
            () -> assertThat(Tomcat.isSameType(new DataSource())),
            () -> assertThat(DBCP2.isSameType(new BasicDataSource())).isTrue()
        );
    }

    @Test
    void getPrefix() {
        assertAll(
            () -> assertThat(Hikari.getPrefix()).isEqualTo("hikari"),
            () -> assertThat(Tomcat.getPrefix()).isEqualTo("tomcat"),
            () -> assertThat(DBCP2.getPrefix()).isEqualTo("dbcp2")
        );
    }

    @Test
    void create() {
        assertAll(
            () -> assertThat(Hikari.create(new HikariDataSource())).isInstanceOf(HikariDataSourcePoolMetadata.class),
            () -> assertThat(Tomcat.create(new DataSource())).isInstanceOf(TomcatDataSourcePoolMetadata.class),
            () -> assertThat(DBCP2.create(new BasicDataSource())).isInstanceOf(CommonsDbcp2DataSourcePoolMetadata.class)
        );
    }
}
