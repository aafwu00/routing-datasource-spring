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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author Taeho Kim
 */
class ChainedTransactionManagerFactoryTest {
    private ChainedTransactionManagerFactory factory;

    @BeforeEach
    void setUp() {
        factory = new ChainedTransactionManagerFactory(Arrays.asList("name1", "name2"));
    }

    @Test
    void getObjectType() {
        assertThat(factory.getObjectType()).isEqualTo(ChainedTransactionManager.class);
    }

    @Test
    void isSingleton() {
        assertThat(factory.isSingleton()).isTrue();
    }

    @Test
    void afterPropertiesSet() {
        final ApplicationContext context = mock(ApplicationContext.class);
        factory.setApplicationContext(context);
        final PlatformTransactionManager manager1 = mock(PlatformTransactionManager.class);
        final PlatformTransactionManager manager2 = mock(PlatformTransactionManager.class);
        doReturn(manager1).when(context).getBean("name1", PlatformTransactionManager.class);
        doReturn(manager2).when(context).getBean("name2", PlatformTransactionManager.class);
        factory.afterPropertiesSet();
        assertThat(factory.getObject()).isSameAs(factory.getObject());
    }
}
