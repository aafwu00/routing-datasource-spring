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
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import sample.display.Display;
import sample.display.DisplayRepository;
import sample.display.DisplayTransactional;
import sample.product.Product;
import sample.product.ProductRepository;
import sample.product.ProductTransactional;

@SpringBootApplication
@EnableTransactionManagement
public class MultiReplicationSample {
    public static void main(final String[] args) {
        SpringApplication.run(MultiReplicationSample.class);
    }

    @RestController
    public static class SampleController {
        private final DisplayRepository displayRepository;
        private final ProductRepository productRepository;

        SampleController(DisplayRepository displayRepository, ProductRepository productRepository) {
            this.displayRepository = displayRepository;
            this.productRepository = productRepository;
        }

        @GetMapping("/display-read-only")
        public List<Display> displayReadOnly() {
            return displayRepository.findAll();
        }

        @GetMapping("/display")
        @DisplayTransactional
        public List<Display> display() {
            return displayRepository.findAll();
        }

        @PostMapping("/display-transaction")
        @DisplayTransactional
        public List<Display> displayTransaction(@RequestParam("title") String title) {
            displayRepository.store(title);
            return displayRepository.findAll();
        }

        @PostMapping("/display")
        public List<Display> display(@RequestParam("title") String title) {
            displayRepository.store(title);
            return displayRepository.findAll();
        }

        @GetMapping("/product-read-only")
        public List<Product> productReadOnly() {
            return productRepository.findAll();
        }

        @GetMapping("/product")
        @ProductTransactional
        public List<Product> product() {
            return productRepository.findAll();
        }

        @PostMapping("/product-transaction")
        @ProductTransactional
        public List<Product> productTransaction(@RequestParam("title") String title) {
            productRepository.store(title);
            return productRepository.findAll();
        }

        @PostMapping("/product")
        public List<Product> product(@RequestParam("title") String title) {
            productRepository.store(title);
            return productRepository.findAll();
        }

        @GetMapping("/product-wrong-transaction")
        @DisplayTransactional
        public List<Product> productWrongTransaction() {
            return productRepository.findAll();
        }
    }
}
