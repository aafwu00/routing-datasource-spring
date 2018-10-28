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

import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.support.TargetDataSources;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.support.TargetDataSourcesFactory;

import static com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.RoutingCondition.PREFIX;
import static com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.multi.MultiRoutingDataSourceProperties.TARGETS;
import static java.util.Objects.requireNonNull;

/**
 * @author Taeho Kim
 */
public class MultiRoutingDataSourcesHolder implements InitializingBean,
                                                      ApplicationContextAware {
    private final TargetDataSourcesFactory<String> factory = new TargetDataSourcesFactory<>();
    private final MultiRoutingDataSourceProperties properties;

    public MultiRoutingDataSourcesHolder(final MultiRoutingDataSourceProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Override
    public void afterPropertiesSet() {
        afterPropertiesSet(properties.getTargets());
        factory.afterPropertiesSet();
    }

    private void afterPropertiesSet(final Map<String, DataSourceProperties> properties) {
        properties.forEach((name, property) -> factory.with(name, property, PREFIX + TARGETS + "." + name));
    }

    public DataSource determine(final String key) {
        return factory.getObject()
                      .determine(key)
                      .orElseThrow(() -> new IllegalArgumentException("key : '" + key + "' is not exists"));
    }

    public TargetDataSources<String> getTargetDataSources() {
        return factory.getObject();
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        factory.setApplicationContext(applicationContext);
    }
}
