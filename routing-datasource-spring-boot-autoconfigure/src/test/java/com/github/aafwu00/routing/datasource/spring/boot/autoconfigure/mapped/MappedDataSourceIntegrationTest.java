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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.aafwu00.routing.datasource.spring.RoutingRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

/**
 * @author Taeho Kim
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = MappedDataSourceIntegrationTest.TestApp.class,
                properties = {"datasource.routing.enabled=true",
                              "datasource.routing.type=Mapped"})
@DirtiesContext
class MappedDataSourceIntegrationTest {
    @Autowired
    private TestApp.MappedDao dao;
    @MockBean
    private RoutingRule<String> routingRule;

    @Test
    void should_be_defaults_when_routingRule_is_not_matched() {
        doReturn(null).when(routingRule).determineCurrentLookupKey();
        assertThat(dao.findAll()).containsOnly("defaults");
    }

    @Test
    void should_be_mapped_first_when_routingRule_is_first() {
        doReturn("first").when(routingRule).determineCurrentLookupKey();
        assertThat(dao.findAll()).containsOnly("mapped1");
    }

    @Test
    void should_be_mapped_second_when_routingRule_is_second() {
        doReturn("second").when(routingRule).determineCurrentLookupKey();
        assertThat(dao.findAll()).containsOnly("mapped2");
    }

    @SpringBootApplication
    static class TestApp {
        @Repository
        class MappedDao {
            private final JdbcTemplate jdbcTemplate;

            MappedDao(final JdbcTemplate jdbcTemplate) {
                this.jdbcTemplate = jdbcTemplate;
            }

            public List<String> findAll() {
                return jdbcTemplate.queryForList("SELECT title FROM todo", String.class);
            }
        }
    }
}
