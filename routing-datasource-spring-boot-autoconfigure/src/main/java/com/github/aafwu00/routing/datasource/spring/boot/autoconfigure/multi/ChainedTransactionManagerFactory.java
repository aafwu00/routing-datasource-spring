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

import java.util.Collection;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import static java.util.Objects.requireNonNull;

/**
 * @author Taeho Kim
 */
public class ChainedTransactionManagerFactory implements FactoryBean<ChainedTransactionManager>,
                                                         ApplicationContextAware,
                                                         InitializingBean {
    private final Collection<String> beanNames;
    private ApplicationContext applicationContext;
    private ChainedTransactionManager target;

    public ChainedTransactionManagerFactory(final Collection<String> beanNames) {
        this.beanNames = requireNonNull(beanNames);
    }

    @Override
    public ChainedTransactionManager getObject() {
        return target;
    }

    @Override
    public Class<?> getObjectType() {
        return ChainedTransactionManager.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        final PlatformTransactionManager[] transactionManagers = new PlatformTransactionManager[beanNames.size()];
        int count = 0;
        for (final String beanName : beanNames) {
            transactionManagers[count++] = applicationContext.getBean(beanName, PlatformTransactionManager.class);
        }
        target = new ChainedTransactionManager(transactionManagers);
    }
}
