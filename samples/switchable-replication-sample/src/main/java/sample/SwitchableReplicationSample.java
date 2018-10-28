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

package sample;

import java.io.Serializable;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.aafwu00.routing.datasource.spring.ReplicationRoutingRuleImpl;
import com.github.aafwu00.routing.datasource.spring.ReplicationType;
import com.github.aafwu00.routing.datasource.spring.RoutingRule;
import com.github.aafwu00.routing.datasource.spring.Switching;

@SpringBootApplication
@EnableTransactionManagement
public class SwitchableReplicationSample {
    public static void main(final String[] args) {
        SpringApplication.run(SwitchableReplicationSample.class);
    }

    @Bean
    public Switching switching() {
        return () -> false;
    }

    @Bean
    public RoutingRule<ReplicationType> routingRule(Switching switching) {
        RoutingRule<ReplicationType> routingRule = new ReplicationRoutingRuleImpl();
        return () -> switching.isOn() ? ReplicationType.Master : routingRule.determineCurrentLookupKey();
    }

    @Repository
    public class TodoRepository {
        private final JdbcTemplate template;

        public TodoRepository(final DataSource dataSource) {
            template = new JdbcTemplate(dataSource);
        }

        @Transactional(readOnly = true)
        public List<Todo> findAll() {
            return template.query("SELECT id, title FROM todo", new BeanPropertyRowMapper<>(Todo.class));
        }

        public void store(final String contents) {
            template.update("INSERT INTO todo(title) VALUES (?)", contents);
        }

        public void delete() {
            template.update("DELETE FROM todo WHERE title LIKE 'todo%'");
        }
    }

    @RestController
    public static class TodoController {
        private final TodoRepository repository;

        TodoController(TodoRepository repository) {
            this.repository = repository;
        }

        @GetMapping("/todos-read-only")
        public List<Todo> todoReadOnly() {
            return repository.findAll();
        }

        @GetMapping("/todos")
        @Transactional
        public List<Todo> todos() {
            return repository.findAll();
        }

        @PostMapping("/todos-transaction")
        @Transactional
        public List<Todo> todosTransaction(@RequestParam("title") String title) {
            repository.store(title);
            return repository.findAll();
        }

        @PostMapping("/todos")
        public List<Todo> todos(@RequestParam("title") String title) {
            repository.store(title);
            return repository.findAll();
        }
    }

    public static class Todo implements Serializable {
        private static final long serialVersionUID = 4220424187615187948L;
        private Long id;
        private String title;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
