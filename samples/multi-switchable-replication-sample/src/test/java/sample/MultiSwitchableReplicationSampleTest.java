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

import sample.display.DisplayRepository;
import sample.product.ProductRepository;
import com.github.aafwu00.routing.datasource.spring.Switching;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Rollback
@DirtiesContext
class MultiSwitchableReplicationSampleTest {
    @Autowired
    private WebApplicationContext context;
    @MockBean(name = "displaySwitching")
    private Switching displaySwitching;
    @MockBean(name = "productSwitching")
    private Switching productSwitching;
    @Autowired
    private DisplayRepository displayRepository;
    @Autowired
    private ProductRepository productRepository;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        displayRepository.delete();
        productRepository.delete();
    }

    @Test
    void should_be_choose_master_or_slave_datasource_depend_on_readOnly_property_for_display_transaction_when_display_switch_is_off() throws
                                                                                                                                      Exception {
        doReturn(false).when(displaySwitching).isOn();
        mvc.perform(get("/display-read-only"))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
           .andExpect(jsonPath("$[0].id", is(1)))
           .andExpect(jsonPath("$[0].title", is("display")));
        mvc.perform(get("/display"))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
           .andExpect(jsonPath("$[0].id", is(1)))
           .andExpect(jsonPath("$[0].title", is("main")));
        mvc.perform(post("/display").param("title", "todo1"))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
           .andExpect(jsonPath("$[0].id", is(1)))
           .andExpect(jsonPath("$[0].title", is("display")));
        mvc.perform(post("/display-transaction").param("title", "todo2"))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
           .andExpect(jsonPath("$[0].id", is(1)))
           .andExpect(jsonPath("$[0].title", is("main")))
           .andExpect(jsonPath("$[1].title", is("todo1")))
           .andExpect(jsonPath("$[2].title", is("todo2")));
    }

    @Test
    void should_be_choose_master_or_slave_datasource_depend_on_readOnly_property_for_product_transaction_when_product_switch_is_off() throws
                                                                                                                                      Exception {
        doReturn(false).when(productSwitching).isOn();
        mvc.perform(get("/product-read-only"))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
           .andExpect(jsonPath("$[0].id", is(1)))
           .andExpect(jsonPath("$[0].title", is("product")));
        mvc.perform(get("/product"))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
           .andExpect(jsonPath("$[0].id", is(1)))
           .andExpect(jsonPath("$[0].title", is("main")));
        mvc.perform(post("/product").param("title", "todo1"))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
           .andExpect(jsonPath("$[0].id", is(1)))
           .andExpect(jsonPath("$[0].title", is("product")));
        mvc.perform(post("/product-transaction").param("title", "todo2"))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
           .andExpect(jsonPath("$[0].id", is(1)))
           .andExpect(jsonPath("$[0].title", is("main")))
           .andExpect(jsonPath("$[1].title", is("todo1")))
           .andExpect(jsonPath("$[2].title", is("todo2")));
    }

    @Test
    void should_be_choose_master_or_slave_datasource_depend_on_readOnly_property_for_each_transaction_when_all_switch_is_off() throws
                                                                                                                               Exception {
        doReturn(false).when(displaySwitching).isOn();
        doReturn(false).when(productSwitching).isOn();
        mvc.perform(get("/same-transaction"))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
           .andExpect(jsonPath("$[0]", is("display")))
           .andExpect(jsonPath("$[1]", is("product")));
    }

    @Test
    void should_be_master_datasource_when_all_switch_is_on() throws Exception {
        doReturn(true).when(displaySwitching).isOn();
        doReturn(true).when(productSwitching).isOn();
        mvc.perform(get("/same-transaction"))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
           .andExpect(jsonPath("$[0]", is("main")))
           .andExpect(jsonPath("$[1]", is("main")));
    }

    @Test
    void should_be_master_datasource_transaction_when_display_switch_is_on() throws Exception {
        doReturn(true).when(displaySwitching).isOn();
        mvc.perform(get("/display-read-only"))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
           .andExpect(jsonPath("$[0].id", is(1)))
           .andExpect(jsonPath("$[0].title", is("main")));
        mvc.perform(get("/display"))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
           .andExpect(jsonPath("$[0].id", is(1)))
           .andExpect(jsonPath("$[0].title", is("main")));
        mvc.perform(post("/display").param("title", "todo1"))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
           .andExpect(jsonPath("$[0].id", is(1)))
           .andExpect(jsonPath("$[0].title", is("main")))
           .andExpect(jsonPath("$[1].title", is("todo1")));
        mvc.perform(post("/display-transaction").param("title", "todo2"))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
           .andExpect(jsonPath("$[0].id", is(1)))
           .andExpect(jsonPath("$[0].title", is("main")))
           .andExpect(jsonPath("$[1].title", is("todo1")))
           .andExpect(jsonPath("$[2].title", is("todo2")));
    }

    @Test
    void should_be_master_datasource_transaction_when_product_switch_is_on() throws Exception {
        doReturn(true).when(productSwitching).isOn();
        mvc.perform(get("/product-read-only"))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
           .andExpect(jsonPath("$[0].id", is(1)))
           .andExpect(jsonPath("$[0].title", is("main")));
        mvc.perform(get("/product"))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
           .andExpect(jsonPath("$[0].id", is(1)))
           .andExpect(jsonPath("$[0].title", is("main")));
        mvc.perform(post("/product").param("title", "todo1"))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
           .andExpect(jsonPath("$[0].id", is(1)))
           .andExpect(jsonPath("$[0].title", is("main")))
           .andExpect(jsonPath("$[1].title", is("todo1")));
        mvc.perform(post("/product-transaction").param("title", "todo2"))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
           .andExpect(jsonPath("$[0].id", is(1)))
           .andExpect(jsonPath("$[0].title", is("main")))
           .andExpect(jsonPath("$[1].title", is("todo1")))
           .andExpect(jsonPath("$[2].title", is("todo2")));
    }
}
