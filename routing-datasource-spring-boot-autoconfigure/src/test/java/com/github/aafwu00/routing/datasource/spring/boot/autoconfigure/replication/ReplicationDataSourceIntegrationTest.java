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

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Taeho Kim
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ReplicationDataSourceIntegrationTest.TestApp.class,
                properties = {"datasource.routing.enabled=true",
                              "datasource.routing.type=Replication"})
@DirtiesContext
class ReplicationDataSourceIntegrationTest {
    @Autowired
    private TestApp.ReplicationDao dao;

    @AfterEach
    void tearDown() {
        dao.clear();
    }

    @Test
    void should_be_master_db_when_not_exists_transaction() {
        assertThat(dao.findAll()).containsOnly("master");
    }

    @Test
    void should_be_slave_db_when_exists_readonly_transaction() {
        assertThat(dao.findAllReadOnly()).containsOnly("slave");
    }

    @Test
    @Transactional
    void should_be_master_db_when_exists_override_transaction() {
        assertThat(dao.findAllReadOnly()).containsOnly("master");
    }

    @Test
    @Transactional
    @Rollback
    void should_be_master_db_when_exists_transaction() {
        dao.store("test");
        assertThat(dao.findAll()).contains("test");
        assertThat(dao.findAllReadOnly()).contains("test");
    }

    @Test
    void should_be_different_db_when_not_exists_transaction() {
        assertThat(dao.findAll()).doesNotContain("test");
        dao.store("test");
        assertThat(dao.findAll()).contains("test");
        assertThat(dao.findAllReadOnly()).doesNotContain("test");
    }

    @SpringBootApplication
    static class TestApp {
        @Repository
        class ReplicationDao {
            private final JdbcTemplate jdbcTemplate;

            ReplicationDao(final JdbcTemplate jdbcTemplate) {
                this.jdbcTemplate = jdbcTemplate;
            }

            public List<String> findAll() {
                return jdbcTemplate.queryForList("SELECT title FROM todo", String.class);
            }

            @Transactional(readOnly = true)
            public List<String> findAllReadOnly() {
                return jdbcTemplate.queryForList("SELECT title FROM todo", String.class);
            }

            @Transactional
            public void store(final String contents) {
                jdbcTemplate.update("INSERT INTO todo(title) VALUES (?)", contents);
            }

            @Transactional
            public void clear() {
                jdbcTemplate.update("DELETE FROM todo WHERE title NOT IN (?, ?)", "master", "slave");
            }
        }
    }
}
