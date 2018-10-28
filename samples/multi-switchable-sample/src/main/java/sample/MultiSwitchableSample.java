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

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import sample.display.Display;
import sample.display.DisplayRepository;
import sample.product.Product;
import sample.product.ProductRepository;
import com.github.aafwu00.routing.datasource.spring.SwitchableRoutingRule;
import com.github.aafwu00.routing.datasource.spring.SwitchableRoutingRuleImpl;
import com.github.aafwu00.routing.datasource.spring.Switching;

@SpringBootApplication
@EnableTransactionManagement
public class MultiSwitchableSample {
    public static void main(final String[] args) {
        SpringApplication.run(MultiSwitchableSample.class);
    }

    @Bean
    public Switching displaySwitching() {
        return () -> true;
    }

    @Bean
    public Switching productSwitching() {
        return () -> false;
    }

    @Bean
    public SwitchableRoutingRule displayRoutingRule(Switching displaySwitching) {
        return new SwitchableRoutingRuleImpl(displaySwitching);
    }

    @Bean
    public SwitchableRoutingRule productSwitchableRoutingRuleImpl(Switching productSwitching) {
        return new SwitchableRoutingRuleImpl(productSwitching);
    }

    @RestController
    public static class SampleController {
        private final DisplayRepository displayRepository;
        private final ProductRepository productRepository;

        SampleController(DisplayRepository displayRepository, ProductRepository productRepository) {
            this.displayRepository = displayRepository;
            this.productRepository = productRepository;
        }

        @GetMapping("/display")
        public List<Display> display() {
            return displayRepository.findAll();
        }

        @GetMapping("/product")
        public List<Product> product() {
            return productRepository.findAll();
        }
    }
}
