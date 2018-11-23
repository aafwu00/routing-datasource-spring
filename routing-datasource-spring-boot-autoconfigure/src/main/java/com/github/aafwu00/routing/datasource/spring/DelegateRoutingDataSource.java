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

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import static java.util.Objects.requireNonNull;

/**
 * @author Taeho Kim
 */
public class DelegateRoutingDataSource<T> extends AbstractRoutingDataSource {
    private final RoutingRule<T> routingRule;

    public DelegateRoutingDataSource(final RoutingRule<T> routingRule) {
        super();
        this.routingRule = requireNonNull(routingRule);
    }

    @Override
    protected T determineCurrentLookupKey() {
        return routingRule.determineCurrentLookupKey();
    }
}
