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

import com.github.aafwu00.routing.datasource.spring.Switching;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

/**
 * @author Taeho Kim
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SwitchableDataSourceIntegrationTest.TestApp.class,
                properties = {"datasource.routing.enabled=true",
                              "datasource.routing.type=Switchable"})
@DirtiesContext
class SwitchableDataSourceIntegrationTest {
    @Autowired
    private TestApp.SwitchingDao dao;
    @MockBean
    private Switching switching;

    @Test
    void should_be_switch_off_db_when_switch_off() {
        doReturn(false).when(switching).isOn();
        assertThat(dao.findAll()).containsOnly("off");
    }

    @Test
    void should_be_switch_on_db_when_switch_on() {
        doReturn(true).when(switching).isOn();
        assertThat(dao.findAll()).containsOnly("on");
    }

    @SpringBootApplication
    static class TestApp {
        @Repository
        class SwitchingDao {
            private final JdbcTemplate jdbcTemplate;

            SwitchingDao(final JdbcTemplate jdbcTemplate) {
                this.jdbcTemplate = jdbcTemplate;
            }

            public List<String> findAll() {
                return jdbcTemplate.queryForList("SELECT title FROM todo", String.class);
            }
        }
    }
}
