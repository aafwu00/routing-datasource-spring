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

package com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.mapped;

import java.util.Collection;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.RoutingType;

/**
 * @author Taeho Kim
 */
@Validated
@ConfigurationProperties(prefix = RoutingType.SCOPE)
public class MappedDataSourceProperties implements BeanClassLoaderAware, InitializingBean {
    public static final String DEFAULTS = "defaults";
    public static final String MAPPED = "mapped";
    private ClassLoader classLoader;
    @NotNull
    @Valid
    private DataSourceProperties defaults;
    @NotNull
    @NotEmpty
    @Valid
    private Map<String, DataSourceProperties> mapped;

    @Override
    public void afterPropertiesSet() throws Exception {
        afterPropertiesSet(defaults);
        afterPropertiesSet(mapped.values());
    }

    private void afterPropertiesSet(final Collection<DataSourceProperties> properties) throws Exception {
        for (final DataSourceProperties property : properties) {
            afterPropertiesSet(property);
        }
    }

    private void afterPropertiesSet(final DataSourceProperties properties) throws Exception {
        properties.setBeanClassLoader(classLoader);
        properties.afterPropertiesSet();
    }

    @Override
    public void setBeanClassLoader(final ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public DataSourceProperties getDefaults() {
        return defaults;
    }

    public void setDefaults(final DataSourceProperties defaults) {
        this.defaults = defaults;
    }

    public Map<String, DataSourceProperties> getMapped() {
        return mapped;
    }

    public void setMapped(final Map<String, DataSourceProperties> mapped) {
        this.mapped = mapped;
    }
}
