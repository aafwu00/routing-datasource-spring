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

package com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.switchable;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.validation.annotation.Validated;

import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.RoutingType;

/**
 * @author Taeho Kim
 */
@Validated
@ConfigurationProperties(prefix = RoutingType.SCOPE)
public class SwitchableDataSourceProperties implements BeanClassLoaderAware, EnvironmentAware, InitializingBean {
    public static final String SWITCH_OFF = "switchOff";
    public static final String SWITCH_ON = "switchOn";
    private ClassLoader classLoader;
    private Environment environment;
    /**
     * Switch Off DataSource
     */
    @NotNull
    private DataSourceProperties switchOff;
    /**
     * Switch On DataSource
     */
    @NotNull
    private DataSourceProperties switchOn;

    @Override
    public void afterPropertiesSet() throws Exception {
        afterPropertiesSet(switchOff);
        afterPropertiesSet(switchOn);
    }

    private void afterPropertiesSet(final DataSourceProperties properties) throws Exception {
        properties.setBeanClassLoader(classLoader);
        properties.setEnvironment(environment);
        properties.afterPropertiesSet();
    }

    @Override
    public void setBeanClassLoader(final ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void setEnvironment(final Environment environment) {
        this.environment = environment;
    }

    public DataSourceProperties getSwitchOff() {
        return switchOff;
    }

    public void setSwitchOff(final DataSourceProperties switchOff) {
        this.switchOff = switchOff;
    }

    public DataSourceProperties getSwitchOn() {
        return switchOn;
    }

    public void setSwitchOn(final DataSourceProperties switchOn) {
        this.switchOn = switchOn;
    }
}
