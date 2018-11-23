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

import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.StringUtils.hasText;

/**
 * @author Taeho Kim
 */
@Validated
@ConfigurationProperties(prefix = RoutingType.SCOPE)
public class MultiRoutingDataSourceProperties implements BeanClassLoaderAware, InitializingBean {
    public static final String MULTI = "multi";
    public static final String TARGETS = "targets";
    private ClassLoader classLoader;
    /**
     * Enable ChainedTransactionManager, Off Bean
     */
    private boolean enableChainedTransactionManager;
    @NotNull
    @NotEmpty
    @Valid
    private Map<String, MultiDataSourceName> multi;
    @NotNull
    @NotEmpty
    @Valid
    private Map<String, DataSourceProperties> targets;

    @Override
    public void afterPropertiesSet() throws Exception {
        afterPropertiesSet(targets.values());
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

    public boolean isEnableChainedTransactionManager() {
        return enableChainedTransactionManager;
    }

    public void setEnableChainedTransactionManager(final boolean enableChainedTransactionManager) {
        this.enableChainedTransactionManager = enableChainedTransactionManager;
    }

    public Map<String, MultiDataSourceName> getMulti() {
        return multi;
    }

    public void setMulti(final Map<String, MultiDataSourceName> multi) {
        this.multi = multi;
    }

    public Map<String, DataSourceProperties> getTargets() {
        return targets;
    }

    public void setTargets(final Map<String, DataSourceProperties> targets) {
        this.targets = targets;
    }

    public boolean isValid() {
        for (final MultiDataSourceName name : multi.values()) {
            if (isInvalid(name)) {
                return false;
            }
        }
        return true;
    }

    private boolean isInvalid(final MultiDataSourceName name) {
        return !name.isValid() || isInvalidStandalone(name) || isInvalidReplication(name) || isInvalidSwitchable(name) || isInvalidMapped(
            name);
    }

    private boolean isInvalidStandalone(final MultiDataSourceName name) {
        return name.hasStandalone() && !containStandalone(name);
    }

    private boolean isInvalidReplication(final MultiDataSourceName name) {
        return name.hasReplication() && !containReplication(name);
    }

    private boolean isInvalidSwitchable(final MultiDataSourceName name) {
        return name.hasSwitchable() && !containSwitchable(name);
    }

    private boolean isInvalidMapped(final MultiDataSourceName name) {
        return name.hasMapped() && !containMapped(name);
    }

    private boolean containSwitchable(final MultiDataSourceName name) {
        return targets.containsKey(name.getSwitchOff()) && targets.containsKey(name.getSwitchOn());
    }

    private boolean containReplication(final MultiDataSourceName name) {
        return targets.containsKey(name.getMaster()) && targets.containsKey(name.getSlave());
    }

    private boolean containStandalone(final MultiDataSourceName name) {
        return targets.containsKey(name.getStandalone());
    }

    private boolean containMapped(final MultiDataSourceName name) {
        for (final String value : name.getMapped().values()) {
            if (!targets.containsKey(value)) {
                return false;
            }
        }
        return true;
    }

    @Validated
    public static class MultiDataSourceName {
        /**
         * Not Routing DataSource Name
         */
        private String standalone;
        /**
         * Master DataSource Name
         */
        private String master;
        /**
         * Slave(Read Only) DataSource Name
         */
        private String slave;
        /**
         * Switch Off DataSource Name
         */
        private String switchOff;
        /**
         * Switch On DataSource Name
         */
        private String switchOn;
        /**
         * Default DataSource Name
         */
        private String defaults;
        /**
         * Mapped DataSource Name
         */
        private Map<String, String> mapped;

        public boolean isValid() {
            return hasOnlyStandalone() || hasOnlyReplication() || hasOnlySwitchable() || hasOnlyMapped();
        }

        private boolean hasOnlyStandalone() {
            return hasStandalone() && !hasReplication() && !hasSwitchable() && !hasMapped();
        }

        private boolean hasOnlyReplication() {
            return !hasStandalone() && hasReplication() && !hasSwitchable() && !hasMapped();
        }

        private boolean hasOnlySwitchable() {
            return !hasStandalone() && !hasReplication() && hasSwitchable() && !hasMapped();
        }

        private boolean hasOnlyMapped() {
            return !hasStandalone() && !hasReplication() && !hasSwitchable() && hasMapped();
        }

        public boolean hasStandalone() {
            return hasText(standalone);
        }

        public boolean hasReplication() {
            return hasText(master) && hasText(slave);
        }

        public boolean hasSwitchable() {
            return hasText(switchOff) && hasText(switchOn);
        }

        public boolean hasMapped() {
            return hasText(defaults) && !isEmpty(mapped);
        }

        public String getStandalone() {
            return standalone;
        }

        public void setStandalone(final String standalone) {
            this.standalone = standalone;
        }

        public String getMaster() {
            return master;
        }

        public void setMaster(final String master) {
            this.master = master;
        }

        public String getSlave() {
            return slave;
        }

        public void setSlave(final String slave) {
            this.slave = slave;
        }

        public String getSwitchOff() {
            return switchOff;
        }

        public void setSwitchOff(final String switchOff) {
            this.switchOff = switchOff;
        }

        public String getSwitchOn() {
            return switchOn;
        }

        public void setSwitchOn(final String switchOn) {
            this.switchOn = switchOn;
        }

        public String getDefaults() {
            return defaults;
        }

        public void setDefaults(final String defaults) {
            this.defaults = defaults;
        }

        public Map<String, String> getMapped() {
            return mapped;
        }

        public void setMapped(final Map<String, String> mapped) {
            this.mapped = mapped;
        }
    }
}
