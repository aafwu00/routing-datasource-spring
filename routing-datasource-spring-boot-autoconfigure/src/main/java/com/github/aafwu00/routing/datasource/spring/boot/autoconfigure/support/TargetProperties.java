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

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

import static java.util.Objects.requireNonNull;

/**
 * @author Taeho Kim
 */
public class TargetProperties<T> {
    private final T key;
    private final DataSourceProperties properties;
    private final String namePrefix;

    public TargetProperties(final T key, final DataSourceProperties properties, final String namePrefix) {
        this.key = requireNonNull(key);
        this.namePrefix = requireNonNull(namePrefix);
        this.properties = requireNonNull(properties);
    }

    public T getKey() {
        return key;
    }

    public DataSourceProperties getProperties() {
        return properties;
    }

    public String getNamePrefix() {
        return namePrefix;
    }
}
