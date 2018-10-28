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
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableTransactionManagement
public class SwitchableSample {
    public static void main(final String[] args) {
        SpringApplication.run(SwitchableSample.class);
    }

    @Repository
    public class TodoRepository {
        private final JdbcTemplate template;

        public TodoRepository(final DataSource dataSource) {
            template = new JdbcTemplate(dataSource);
        }

        public List<Todo> findAll() {
            return template.query("SELECT id, title FROM todo", new BeanPropertyRowMapper<>(Todo.class));
        }
    }

    @RestController
    public static class TodoController {
        private final TodoRepository repository;

        TodoController(TodoRepository repository) {
            this.repository = repository;
        }

        @GetMapping("/todos")
        public List<Todo> todos() {
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
