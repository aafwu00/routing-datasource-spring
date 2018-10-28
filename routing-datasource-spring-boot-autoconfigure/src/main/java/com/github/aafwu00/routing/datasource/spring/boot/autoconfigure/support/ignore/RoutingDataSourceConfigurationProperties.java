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

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.zaxxer.hikari.HikariDataSource;

import static com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.RoutingCondition.PREFIX;
import static com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.mapped.MappedDataSourceProperties.DEFAULTS;
import static com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.replication.ReplicationDataSourceProperties.MASTER;
import static com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.replication.ReplicationDataSourceProperties.SLAVE;
import static com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.switchable.SwitchableDataSourceProperties.SWITCH_OFF;
import static com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.switchable.SwitchableDataSourceProperties.SWITCH_ON;

/**
 * 의미는 없고 자동 완성을 위한 설정
 *
 * @author Taeho Kim
 */
final class RoutingDataSourceConfigurationProperties {
    static class Tomcat {
        private static final String NAME = ".tomcat";

        @ConfigurationProperties(prefix = PREFIX + MASTER + NAME)
        public DataSource master() {
            return new DataSource();
        }

        @ConfigurationProperties(prefix = PREFIX + SLAVE + NAME)
        public DataSource slave() {
            return new DataSource();
        }

        @ConfigurationProperties(prefix = PREFIX + SWITCH_OFF + NAME)
        public DataSource switchOff() {
            return new DataSource();
        }

        @ConfigurationProperties(prefix = PREFIX + SWITCH_ON + NAME)
        public DataSource switchOn() {
            return new DataSource();
        }

        @ConfigurationProperties(prefix = PREFIX + DEFAULTS + NAME)
        public DataSource defaults() {
            return new DataSource();
        }
    }

    static class Hikari {
        private static final String NAME = ".hikari";

        @ConfigurationProperties(prefix = PREFIX + MASTER + NAME)
        public HikariDataSource master() {
            return new HikariDataSource();
        }

        @ConfigurationProperties(prefix = PREFIX + SLAVE + NAME)
        public HikariDataSource slave() {
            return new HikariDataSource();
        }

        @ConfigurationProperties(prefix = PREFIX + SWITCH_OFF + NAME)
        public HikariDataSource switchOff() {
            return new HikariDataSource();
        }

        @ConfigurationProperties(prefix = PREFIX + SWITCH_ON + NAME)
        public HikariDataSource switchOn() {
            return new HikariDataSource();
        }

        @ConfigurationProperties(prefix = PREFIX + DEFAULTS + NAME)
        public HikariDataSource defaults() {
            return new HikariDataSource();
        }
    }

    static class Dbcp {
        private static final String NAME = ".dbcp2";

        @ConfigurationProperties(prefix = PREFIX + MASTER + NAME)
        public BasicDataSource master() {
            return new BasicDataSource();
        }

        @ConfigurationProperties(prefix = PREFIX + SLAVE + NAME)
        public BasicDataSource slave() {
            return new BasicDataSource();
        }

        @ConfigurationProperties(prefix = PREFIX + SWITCH_OFF + NAME)
        public BasicDataSource switchOff() {
            return new BasicDataSource();
        }

        @ConfigurationProperties(prefix = PREFIX + SWITCH_ON + NAME)
        public BasicDataSource switchOn() {
            return new BasicDataSource();
        }

        @ConfigurationProperties(prefix = PREFIX + DEFAULTS + NAME)
        public BasicDataSource defaults() {
            return new BasicDataSource();
        }
    }
}
