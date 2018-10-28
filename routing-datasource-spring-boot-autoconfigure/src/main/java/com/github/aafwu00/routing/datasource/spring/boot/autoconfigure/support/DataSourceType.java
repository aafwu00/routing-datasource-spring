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

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.jdbc.metadata.CommonsDbcp2DataSourcePoolMetadata;
import org.springframework.boot.autoconfigure.jdbc.metadata.DataSourcePoolMetadata;
import org.springframework.boot.autoconfigure.jdbc.metadata.HikariDataSourcePoolMetadata;
import org.springframework.boot.autoconfigure.jdbc.metadata.TomcatDataSourcePoolMetadata;

import static java.util.Objects.requireNonNull;
import static org.springframework.util.ClassUtils.getConstructorIfAvailable;
import static org.springframework.util.ClassUtils.getDefaultClassLoader;
import static org.springframework.util.ClassUtils.resolveClassName;

/**
 * @author Taeho Kim
 */
public enum DataSourceType {
    Hikari("hikari", "com.zaxxer.hikari.HikariDataSource", HikariDataSourcePoolMetadata.class),
    DBCP2("dbcp2", "org.apache.commons.dbcp2.BasicDataSource", CommonsDbcp2DataSourcePoolMetadata.class),
    Tomcat("tomcat", "org.apache.tomcat.jdbc.pool.DataSource", TomcatDataSourcePoolMetadata.class);
    private final String prefix;
    private final String poolClassName;
    private final Class<? extends DataSourcePoolMetadata> metadataClass;

    DataSourceType(final String prefix, final String poolClassName, final Class<? extends DataSourcePoolMetadata> metadata) {
        this.prefix = requireNonNull(prefix);
        this.poolClassName = requireNonNull(poolClassName);
        this.metadataClass = metadata;
    }

    public static Optional<DataSourceType> valueOf(final DataSource dataSource) {
        return Arrays.stream(DataSourceType.values())
                     .filter(type -> type.isSameType(dataSource)).findFirst();
    }

    public boolean isSameType(final DataSource dataSource) {
        return isSameType(dataSource.getClass().getCanonicalName());
    }

    public boolean isSameType(final String className) {
        return poolClassName.equals(className);
    }

    public String getPrefix() {
        return prefix;
    }

    public DataSourcePoolMetadata create(final DataSource dataSource) {
        final Class<?> parameter = resolveClassName(poolClassName, getDefaultClassLoader());
        final Constructor<? extends DataSourcePoolMetadata> constructor = getConstructorIfAvailable(metadataClass, parameter);
        return BeanUtils.instantiateClass(constructor, dataSource);
    }
}
