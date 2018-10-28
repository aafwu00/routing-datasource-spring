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

import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

/**
 * @author Taeho Kim
 */
public class TargetDataSources<T> {
    private final Map<T, DataSource> dataSources;
    private final Optional<DataSource> defaults;

    public TargetDataSources(final Map<T, DataSource> dataSources) {
        this(dataSources, Optional.empty());
    }

    public TargetDataSources(final Map<T, DataSource> dataSources, final DataSource defaults) {
        this(dataSources, Optional.of(defaults));
    }

    public TargetDataSources(final Map<T, DataSource> dataSources, final Optional<DataSource> defaults) {
        this.dataSources = unmodifiableMap(requireNonNull(dataSources));
        this.defaults = requireNonNull(defaults);
    }

    public Optional<DataSource> determine(final T key) {
        return ofNullable(dataSources.get(key));
    }

    public Map<T, DataSource> getDataSources() {
        return dataSources;
    }

    public Optional<DataSource> getDefaults() {
        return defaults;
    }
}
