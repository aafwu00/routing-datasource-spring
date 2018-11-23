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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.InvalidConfigurationPropertyValueException;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jdbc.DataSourceInitializationMode;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.config.SortedResourcesFactoryBean;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.util.StringUtils;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

/**
 * @author Taeho Kim
 */
public class TargetDataSourcesFactory<T> implements FactoryBean<TargetDataSources<T>>, InitializingBean, ApplicationContextAware {
    private final List<TargetProperties<T>> targetProperties = new ArrayList<>();
    private final Map<DataSourceProperties, DataSource> build = new ConcurrentHashMap<>();
    private final Optional<TargetProperties<T>> defaults;
    private ApplicationContext applicationContext;
    private TargetDataSources<T> target;

    public TargetDataSourcesFactory() {
        this.defaults = Optional.empty();
    }

    public TargetDataSourcesFactory(final TargetProperties<T> defaults) {
        this.defaults = Optional.of(defaults);
    }

    public TargetDataSourcesFactory(final T key, final DataSourceProperties properties, final String namePrefix) {
        this(new TargetProperties<>(key, properties, namePrefix));
    }

    public TargetDataSourcesFactory<T> with(final T key, final DataSourceProperties properties, final String namePrefix) {
        targetProperties.add(new TargetProperties<>(key, properties, namePrefix));
        return this;
    }

    public TargetDataSourcesFactory<T> with(final List<TargetProperties<T>> targetProperties) {
        this.targetProperties.addAll(targetProperties);
        return this;
    }

    @Override
    public TargetDataSources<T> getObject() {
        return target;
    }

    @Override
    public Class<?> getObjectType() {
        return TargetDataSources.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() {
        final Map<T, DataSource> dataSources = new ConcurrentHashMap<>(buildTargetDataSources());
        final Optional<DataSource> defaults = buildDefaultDataSource();
        target = new TargetDataSources<>(dataSources, defaults);
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private Optional<DataSource> buildDefaultDataSource() {
        return defaults.map(target -> buildDataSource(target.getProperties(), target.getNamePrefix()));
    }

    private Map<T, DataSource> buildTargetDataSources() {
        final Map<T, DataSource> result = new ConcurrentHashMap<>();
        targetProperties.forEach(target -> result.put(target.getKey(), buildDataSource(target.getProperties(), target.getNamePrefix())));
        return result;
    }

    private DataSource buildDataSource(final DataSourceProperties properties, final String namePrefix) {
        if (build.containsKey(properties)) {
            return build.get(properties);
        }
        final DataSourceBuilder builder = properties.initializeDataSourceBuilder();
        final DataSource result = bind(builder.build(), namePrefix);
        bindIfTomcatDataSource(result, properties);
        initializer(result, properties, namePrefix);
        build.putIfAbsent(properties, result);
        return result;
    }

    private DataSource bind(final DataSource dataSource, final String namePrefix) {
        return Binder.get(applicationContext.getEnvironment())
                     .bind(namePrefix(dataSource, namePrefix), Bindable.ofInstance(dataSource))
                     .orElse(dataSource);
    }

    private void bindIfTomcatDataSource(final DataSource dataSource, final DataSourceProperties properties) {
        if (DataSourceType.Tomcat.isSameType(dataSource)) {
            final DatabaseDriver databaseDriver = DatabaseDriver.fromJdbcUrl(properties.determineUrl());
            final String validationQuery = databaseDriver.getValidationQuery();
            if (validationQuery != null) {
                final BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(dataSource);
                beanWrapper.setPropertyValue("testOnBorrow", true);
                beanWrapper.setPropertyValue("validationQuery", validationQuery);
            }
        }
    }

    private String namePrefix(final DataSource dataSource, final String namePrefix) {
        return namePrefix + "." + DataSourceType.valueOf(dataSource)
                                                .map(DataSourceType::getPrefix)
                                                .orElse("properties");
    }

    private void initializer(final DataSource result, final DataSourceProperties properties, final String namePrefix) {
        final DataSourceInitializer initializer = new DataSourceInitializer(applicationContext, properties, result, namePrefix);
        initializer.createSchema();
        initializer.initSchema();
    }

    // org.springframework.boot.autoconfigure.jdbc.DataSourceInitializer 가 내부만 쓸수 있어서 copy
    public static class DataSourceInitializer {
        private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceInitializer.class);
        private final ApplicationContext context;
        private final DataSourceProperties properties;
        private final DataSource dataSource;
        private final String namePrefix;

        DataSourceInitializer(final ApplicationContext context,
                              final DataSourceProperties properties,
                              final DataSource dataSource,
                              final String namePrefix) {
            this.context = requireNonNull(context);
            this.properties = requireNonNull(properties);
            this.dataSource = requireNonNull(dataSource);
            this.namePrefix = requireNonNull(namePrefix);
        }

        /**
         * Create the schema if necessary.
         *
         * @return {@code true} if the schema was created
         * @see DataSourceProperties#getSchema()
         */
        public boolean createSchema() {
            final List<Resource> scripts = getScripts(namePrefix + ".schema", properties.getSchema(), "schema");
            if (!scripts.isEmpty()) {
                if (!isEnabled()) {
                    LOGGER.debug("Initialization disabled (not running DDL scripts)");
                    return false;
                }
                final String username = properties.getSchemaUsername();
                final String password = properties.getSchemaPassword();
                runScripts(scripts, username, password);
            }
            return !scripts.isEmpty();
        }

        /**
         * Initialize the schema if necessary.
         *
         * @see DataSourceProperties#getData()
         */
        public void initSchema() {
            final List<Resource> scripts = getScripts(namePrefix + ".data", properties.getData(), "data");
            if (!scripts.isEmpty()) {
                if (!isEnabled()) {
                    LOGGER.debug("Initialization disabled (not running data scripts)");
                    return;
                }
                final String username = properties.getDataUsername();
                final String password = properties.getDataPassword();
                runScripts(scripts, username, password);
            }
        }

        private boolean isEnabled() {
            final DataSourceInitializationMode mode = properties.getInitializationMode();
            if (mode == DataSourceInitializationMode.NEVER) {
                return false;
            }
            return mode != DataSourceInitializationMode.EMBEDDED || isEmbedded();
        }

        private boolean isEmbedded() {
            try {
                return EmbeddedDatabaseConnection.isEmbedded(this.dataSource);
                // CHECKSTYLE:OFF
            } catch (Exception ex) {
                // CHECKSTYLE:ON
                LOGGER.debug("Could not determine if datasource is embedded", ex);
                return false;
            }
        }

        private List<Resource> getScripts(final String propertyName, final List<String> resources, final String fallback) {
            if (resources != null) {
                return getResources(propertyName, resources, true);
            }
            final String platform = properties.getPlatform();
            final List<String> fallbackResources = new ArrayList<>();
            fallbackResources.add("classpath*:" + fallback + "-" + platform + ".sql");
            fallbackResources.add("classpath*:" + fallback + ".sql");
            return getResources(propertyName, fallbackResources, false);
        }

        private List<Resource> getResources(final String propertyName, final List<String> locations, final boolean validate) {
            final List<Resource> resources = new ArrayList<>();
            for (final String location : locations) {
                for (final Resource resource : doGetResources(location)) {
                    if (resource.exists()) {
                        resources.add(resource);
                    } else if (validate) {
                        throw new InvalidConfigurationPropertyValueException(propertyName,
                                                                             resource,
                                                                             "The specified resource does not exist.");
                    }
                }
            }
            return resources;
        }

        private Resource[] doGetResources(final String location) {
            try {
                final SortedResourcesFactoryBean factory = new SortedResourcesFactoryBean(context, singletonList(location));
                factory.afterPropertiesSet();
                return factory.getObject();
                // CHECKSTYLE:OFF
            } catch (Exception ex) {
                // CHECKSTYLE:ON
                throw new IllegalStateException("Unable to load resources from " + location, ex);
            }
        }

        private void runScripts(final List<Resource> resources, final String username, final String password) {
            if (resources.isEmpty()) {
                return;
            }
            final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.setContinueOnError(properties.isContinueOnError());
            populator.setSeparator(properties.getSeparator());
            if (properties.getSqlScriptEncoding() != null) {
                populator.setSqlScriptEncoding(properties.getSqlScriptEncoding().name());
            }
            for (final Resource resource : resources) {
                populator.addScript(resource);
            }
            DataSource dataSource = this.dataSource;
            if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
                dataSource = DataSourceBuilder.create(properties.getClassLoader())
                                              .driverClassName(properties.determineDriverClassName())
                                              .url(properties.determineUrl()).username(username)
                                              .password(password).build();
            }
            DatabasePopulatorUtils.execute(populator, dataSource);
        }
    }
}
