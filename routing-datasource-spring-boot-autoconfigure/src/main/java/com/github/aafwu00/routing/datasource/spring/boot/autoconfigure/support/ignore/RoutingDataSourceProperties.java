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

package com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.support.ignore;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.RoutingType;

/**
 * 의미는 없고 자동 완성을 위한 설정
 *
 * @author Taeho Kim
 */
@Validated
@ConfigurationProperties(prefix = RoutingType.SCOPE)
class RoutingDataSourceProperties {
    /**
     * Enable RoutingDataSource
     */
    private boolean enabled;
    /**
     * Routing Type
     */
    @NotNull
    private RoutingType type;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public RoutingType getType() {
        return type;
    }

    public void setType(final RoutingType type) {
        this.type = type;
    }
}
