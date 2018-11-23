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

package com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.replication;

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
public class ReplicationDataSourceProperties implements BeanClassLoaderAware, InitializingBean {
    public static final String MASTER = "master";
    public static final String SLAVE = "slave";
    private ClassLoader classLoader;
    /**
     * Master DataSource
     */
    @NotNull
    private DataSourceProperties master;
    /**
     * Slave(Read Only) DataSource
     */
    @NotNull
    private DataSourceProperties slave;

    @Override
    public void afterPropertiesSet() throws Exception {
        afterPropertiesSet(master);
        afterPropertiesSet(slave);
    }

    private void afterPropertiesSet(final DataSourceProperties properties) throws Exception {
        properties.setBeanClassLoader(classLoader);
        properties.afterPropertiesSet();
    }

    @Override
    public void setBeanClassLoader(final ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public DataSourceProperties getMaster() {
        return master;
    }

    public void setMaster(final DataSourceProperties master) {
        this.master = master;
    }

    public DataSourceProperties getSlave() {
        return slave;
    }

    public void setSlave(final DataSourceProperties slave) {
        this.slave = slave;
    }
}
