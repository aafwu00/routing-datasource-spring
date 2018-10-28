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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.github.aafwu00.routing.datasource.spring.RoutingRule;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Rollback
@DirtiesContext
class SwitchableSampleTest {
    @Autowired
    private WebApplicationContext context;
    @MockBean
    private RoutingRule<String> routingRule;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void should_be_defaults_datasource_when_routing_is_not_matched() throws Exception {
        doReturn(null).when(routingRule).determineCurrentLookupKey();
        mvc.perform(get("/todos"))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
           .andExpect(jsonPath("$[0].id", is(1)))
           .andExpect(jsonPath("$[0].title", is("defaults")));
    }

    @Test
    void should_be_first_datasource_when_routing_is_first() throws Exception {
        doReturn("first").when(routingRule).determineCurrentLookupKey();
        mvc.perform(get("/todos"))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
           .andExpect(jsonPath("$[0].id", is(1)))
           .andExpect(jsonPath("$[0].title", is("first")));
    }

    @Test
    void should_be_second_datasource_when_routing_is_second() throws Exception {
        doReturn("second").when(routingRule).determineCurrentLookupKey();
        mvc.perform(get("/todos"))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
           .andExpect(jsonPath("$[0].id", is(1)))
           .andExpect(jsonPath("$[0].title", is("second")));
    }
}
