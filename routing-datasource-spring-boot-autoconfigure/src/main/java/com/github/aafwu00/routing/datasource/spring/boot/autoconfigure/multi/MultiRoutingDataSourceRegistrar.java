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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.ObjectUtils;

import com.github.aafwu00.routing.datasource.spring.MappedRoutingRule;
import com.github.aafwu00.routing.datasource.spring.ReplicationRoutingRule;
import com.github.aafwu00.routing.datasource.spring.ReplicationType;
import com.github.aafwu00.routing.datasource.spring.RoutingRule;
import com.github.aafwu00.routing.datasource.spring.SwitchableMode;
import com.github.aafwu00.routing.datasource.spring.SwitchableRoutingRule;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.RoutingType;
import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.support.DelegateRoutingDataSourceFactory;

import static com.github.aafwu00.routing.datasource.spring.ReplicationType.Master;
import static com.github.aafwu00.routing.datasource.spring.ReplicationType.Slave;
import static com.github.aafwu00.routing.datasource.spring.SwitchableMode.Off;
import static com.github.aafwu00.routing.datasource.spring.SwitchableMode.On;
import static com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.multi.MultiRoutingDataSourceProperties.MultiDataSourceName;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;
import static org.springframework.util.ClassUtils.getShortName;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.StringUtils.uncapitalize;

/**
 * @author Taeho Kim
 */
public class MultiRoutingDataSourceRegistrar implements BeanDefinitionRegistryPostProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiRoutingDataSourceRegistrar.class);
    private static final String TARGET_DATA_SOURCES_HOLDER = uncapitalize("targetDataSourcesHolder");
    private static final String PRIMARY_TRANSACTION_MANAGER_NAME = uncapitalize("transactionManager");
    private static final String PRIMARY_TRANSACTION_TEMPLATE_NAME = uncapitalize("transactionTemplate");

    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // do nothing
    }

    @Override
    public void postProcessBeanDefinitionRegistry(final BeanDefinitionRegistry registry) throws BeansException {
        final MultiRoutingDataSourceProperties properties = properties(registry);
        if (!properties.isValid()) {
            throw new BeanDefinitionValidationException("MultiRoutingDataSourceProperties is not valid");
        }
        registerTargetDataSourcesHolder(registry, properties);
        properties.getMulti().forEach((name, property) -> {
            if (property.hasStandalone()) {
                registerStandalone(registry, name, property);
            } else if (property.hasSwitchable()) {
                registerSwitchable(registry, name, property);
            } else if (property.hasReplication()) {
                registerReplication(registry, name, property);
            } else if (property.hasMapped()) {
                registerMapped(registry, name, property);
            } else {
                throw new BeanDefinitionValidationException("MultiRoutingDataSourceProperties is not valid, '" + name + "' is not exists");
            }
            registerTransactionManagerIfNotExists(registry, name);
            registerTransactionTemplateIfNotExists(registry, name);
        });
        if (properties.isEnableChainedTransactionManager()) {
            registerPrimaryTransactionManager(registry, properties.getMulti().keySet());
            registerPrimaryTransactionTemplate(registry);
        }
    }

    private MultiRoutingDataSourceProperties properties(final BeanDefinitionRegistry registry) {
        final ConfigurableEnvironment environment = beanFactory(registry).getBean(ConfigurableEnvironment.class);
        return Binder.get(environment)
                     .bind(RoutingType.SCOPE, Bindable.of(MultiRoutingDataSourceProperties.class))
                     .get();
    }

    private ListableBeanFactory beanFactory(final BeanDefinitionRegistry registry) {
        return (ListableBeanFactory) registry;
    }

    private void registerTargetDataSourcesHolder(final BeanDefinitionRegistry registry,
                                                 final MultiRoutingDataSourceProperties properties) {
        registry.registerBeanDefinition(TARGET_DATA_SOURCES_HOLDER,
                                        genericBeanDefinition(MultiRoutingDataSourcesHolder.class)
                                            .addConstructorArgValue(properties)
                                            .setRole(ROLE_INFRASTRUCTURE)
                                            .getBeanDefinition());
    }

    private void registerStandalone(final BeanDefinitionRegistry registry,
                                    final String name,
                                    final MultiDataSourceName property) {
        registerStandaloneDataSource(registry, name, property.getStandalone());
    }

    private void registerSwitchable(final BeanDefinitionRegistry registry,
                                    final String name,
                                    final MultiDataSourceName property) {
        registerSwitchableTargetDataSources(registry, name, property);
        registerRoutingDataSource(registry, name, SwitchableRoutingRule.class);
    }

    private void registerReplication(final BeanDefinitionRegistry registry,
                                     final String name,
                                     final MultiDataSourceName property) {
        registerReplicationTargetDataSources(registry, name, property);
        registerRoutingDataSource(registry, name, ReplicationRoutingRule.class);
    }

    private void registerMapped(final BeanDefinitionRegistry registry,
                                final String name,
                                final MultiDataSourceName property) {
        registerMappedTargetDataSources(registry, name, property);
        registerRoutingDataSource(registry, name, MappedRoutingRule.class);
    }

    private void registerReplicationTargetDataSources(final BeanDefinitionRegistry registry,
                                                      final String name,
                                                      final MultiDataSourceName property) {
        final Map<ReplicationType, String> keys = new HashMap<>();
        keys.put(Master, property.getMaster());
        keys.put(Slave, property.getSlave());
        registerTargetDataSources(registry, name, keys);
    }

    private void registerSwitchableTargetDataSources(final BeanDefinitionRegistry registry,
                                                     final String name,
                                                     final MultiDataSourceName property) {
        final Map<SwitchableMode, String> keys = new HashMap<>();
        keys.put(Off, property.getSwitchOff());
        keys.put(On, property.getSwitchOn());
        registerTargetDataSources(registry, name, keys);
    }

    private void registerMappedTargetDataSources(final BeanDefinitionRegistry registry,
                                                 final String name,
                                                 final MultiDataSourceName property) {
        registerTargetDataSources(registry, name, property.getMapped(), property.getDefaults());
    }

    private <T> void registerTargetDataSources(final BeanDefinitionRegistry registry,
                                               final String name,
                                               final Map<T, String> keys,
                                               final String defaults) {
        final String beanName = targetDataSourcesName(name);
        final AbstractBeanDefinition beanDefinition = genericBeanDefinition(MultiRoutingTargetDataSourcesFactory.class)
            .addConstructorArgReference(TARGET_DATA_SOURCES_HOLDER)
            .addConstructorArgValue(keys)
            .addConstructorArgValue(defaults)
            .getBeanDefinition();
        registry.registerBeanDefinition(beanName, beanDefinition);
        LOGGER.info("MultiRoutingTargetDataSourcesFactory bean registered: `{}`", beanName);
    }

    private <T> void registerTargetDataSources(final BeanDefinitionRegistry registry, final String name, final Map<T, String> keys) {
        final String beanName = targetDataSourcesName(name);
        final AbstractBeanDefinition beanDefinition = genericBeanDefinition(MultiRoutingTargetDataSourcesFactory.class)
            .addConstructorArgReference(TARGET_DATA_SOURCES_HOLDER)
            .addConstructorArgValue(keys)
            .getBeanDefinition();
        registry.registerBeanDefinition(beanName, beanDefinition);
        LOGGER.info("MultiRoutingTargetDataSourcesFactory bean registered: `{}`", beanName);
    }

    private String targetDataSourcesName(final String name) {
        return uncapitalize(name + "TargetDataSources");
    }

    private void registerRoutingDataSource(final BeanDefinitionRegistry registry,
                                           final String name,
                                           final Class<? extends RoutingRule> clazz) {
        final String beanName = dataSourceName(name);
        final AbstractBeanDefinition beanDefinition = genericBeanDefinition(DelegateRoutingDataSourceFactory.class)
            .addConstructorArgReference(routingRuleName(registry, name, clazz))
            .addConstructorArgReference(targetDataSourcesName(name))
            .getBeanDefinition();
        registry.registerBeanDefinition(beanName, beanDefinition);
        LOGGER.info("DelegateRoutingDataSourceFactory bean registered: `{}` , name:`{}`, class:`{}`:{} ", beanName, name, clazz);
    }

    private String routingRuleName(final BeanDefinitionRegistry registry, final String name, final Class<? extends RoutingRule> clazz) {
        final ListableBeanFactory beanFactory = beanFactory(registry);
        final String[] beanNames = beanFactory.getBeanNamesForType(clazz);
        if (ObjectUtils.isEmpty(beanNames)) {
            throw new NoSuchBeanDefinitionException(clazz);
        }
        final List<String> names = Arrays.stream(beanNames)
                                         .filter(beanName -> startsWithRoutingRuleName(beanName, name, clazz))
                                         .collect(Collectors.toList());
        if (hasOnlyOnce(names)) {
            LOGGER.warn("`{}`'s RoutingRule is bean:`{}`, class:`{}`", name, first(names), clazz);
            return first(names);
        }
        if (!isEmpty(names)) {
            throw new NoUniqueBeanDefinitionException(clazz, names);
        }
        for (final String beanName : beanNames) {
            final BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
            if (beanDefinition.isPrimary()) {
                LOGGER.warn("`{}`'s RoutingRule is primary bean:`{}`, class:`{}`", name, beanName, clazz);
                return beanName;
            }
        }
        throw new NoSuchBeanDefinitionException(clazz);
    }

    private boolean startsWithRoutingRuleName(final String beanName,
                                              final String name,
                                              final Class<? extends RoutingRule> clazz) {
        return beanName.startsWith(name + getShortName(clazz)) || beanName.startsWith(name + getShortName(RoutingRule.class));
    }

    private boolean hasOnlyOnce(final List<?> names) {
        return !isEmpty(names) && names.size() == 1;
    }

    private <T> T first(final List<T> names) {
        return names.get(0);
    }

    private void registerStandaloneDataSource(final BeanDefinitionRegistry registry, final String name, final String standalone) {
        final String beanName = dataSourceName(name);
        final AbstractBeanDefinition beanDefinition = genericBeanDefinition(LazyConnectionDataSourceAdaptor.class)
            .addConstructorArgReference(TARGET_DATA_SOURCES_HOLDER)
            .addConstructorArgValue(standalone)
            .getBeanDefinition();
        registry.registerBeanDefinition(beanName, beanDefinition);
        LOGGER.info("LazyConnectionDataSourceAdaptor bean registered: `{}`, name:`{}`, standalone:`{}`", beanName, name, standalone);
    }

    private String dataSourceName(final String name) {
        return uncapitalize(name + "DataSource");
    }

    private void registerTransactionManagerIfNotExists(final BeanDefinitionRegistry registry, final String name) {
        final String beanName = transactionManagerName(name);
        if (registry.containsBeanDefinition(beanName)) {
            LOGGER.info("bean already contains: `{}`", beanName);
            return;
        }
        final AbstractBeanDefinition beanDefinition = genericBeanDefinition(DataSourceTransactionManager.class)
            .addConstructorArgReference(dataSourceName(name))
            .getBeanDefinition();
        registry.registerBeanDefinition(beanName, beanDefinition);
        LOGGER.info("DataSourceTransactionManager bean registered: `{}`, name:`{}`", beanName, name);
    }

    private String transactionManagerName(final String name) {
        return uncapitalize(name + "TransactionManager");
    }

    private void registerTransactionTemplateIfNotExists(final BeanDefinitionRegistry registry, final String name) {
        final String beanName = uncapitalize(name + "TransactionTemplate");
        if (registry.containsBeanDefinition(beanName)) {
            LOGGER.info("bean already contains: `{}`", beanName);
            return;
        }
        final AbstractBeanDefinition beanDefinition = genericBeanDefinition(TransactionTemplate.class)
            .addConstructorArgReference(transactionManagerName(name))
            .getBeanDefinition();
        registry.registerBeanDefinition(beanName, beanDefinition);
        LOGGER.info("TransactionTemplate bean registered: `{}`, name:`{}`", beanName, name);
    }

    private void registerPrimaryTransactionManager(final BeanDefinitionRegistry registry, final Collection<String> keys) {
        final String beanName = PRIMARY_TRANSACTION_MANAGER_NAME;
        final List<String> transactionNames = keys.stream().map(this::transactionManagerName).collect(Collectors.toList());
        final AbstractBeanDefinition beanDefinition = genericBeanDefinition(ChainedTransactionManagerFactory.class)
            .addConstructorArgValue(transactionNames)
            .getBeanDefinition();
        beanDefinition.setPrimary(true);
        registry.registerBeanDefinition(beanName, beanDefinition);
        LOGGER.info("ChainedTransactionManagerFactory bean registered: `{}`, names:`{}`", beanName, keys);
    }

    private void registerPrimaryTransactionTemplate(final BeanDefinitionRegistry registry) {
        final String beanName = PRIMARY_TRANSACTION_TEMPLATE_NAME;
        final AbstractBeanDefinition beanDefinition = genericBeanDefinition(TransactionTemplate.class)
            .addConstructorArgReference(PRIMARY_TRANSACTION_MANAGER_NAME)
            .getBeanDefinition();
        beanDefinition.setPrimary(true);
        registry.registerBeanDefinition(beanName, beanDefinition);
        LOGGER.info("TransactionTemplate bean registered: `{}`", beanName);
    }
}
