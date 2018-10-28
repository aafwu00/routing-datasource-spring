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

package com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.multi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.aafwu00.routing.datasource.spring.boot.autoconfigure.multi.MultiRoutingDataSourceProperties.MultiDataSourceName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * @author Taeho Kim
 */
class MultiDataSourceNameTest {
    private MultiDataSourceName property;

    @BeforeEach
    void setUp() {
        property = new MultiDataSourceName();
    }

    @Test
    void testIsValid() {
        assertAll(
            () -> assertThat(property.isValid()).isFalse(),
            () -> {
                property.setStandalone("test");
                assertThat(property.isValid()).isTrue();
                property.setMaster("test");
                assertThat(property.isValid()).isTrue();
                property.setSlave("test");
                assertThat(property.isValid()).isFalse();
                property.setStandalone("");
                assertThat(property.isValid()).isTrue();
                property.setSwitchOff("test");
                assertThat(property.isValid()).isTrue();
                property.setSwitchOn("test");
                assertThat(property.isValid()).isFalse();
                property.setSlave("");
                assertThat(property.isValid()).isTrue();
            }
        );
    }

    @Test
    void testHasStandalone() {
        assertAll(
            () -> assertThat(property.hasStandalone()).isFalse(),
            () -> {
                property.setStandalone("  ");
                assertThat(property.hasStandalone()).isFalse();
                property.setStandalone("test");
                assertThat(property.hasStandalone()).isTrue();
            }
        );
    }

    @Test
    void testHasReplication() {
        assertAll(
            () -> assertThat(property.hasReplication()).isFalse(),
            () -> {
                property.setMaster("  ");
                assertThat(property.hasReplication()).isFalse();
                property.setMaster("test");
                assertThat(property.hasReplication()).isFalse();
                property.setSlave("  ");
                assertThat(property.hasReplication()).isFalse();
                property.setSlave("test");
                assertThat(property.hasReplication()).isTrue();
            }
        );
    }

    @Test
    void testHasSwitchable() {
        assertAll(
            () -> assertThat(property.hasSwitchable()).isFalse(),
            () -> {
                property.setSwitchOff("  ");
                assertThat(property.hasSwitchable()).isFalse();
                property.setSwitchOff("test");
                assertThat(property.hasSwitchable()).isFalse();
                property.setSwitchOn("  ");
                assertThat(property.hasSwitchable()).isFalse();
                property.setSwitchOn("test");
                assertThat(property.hasSwitchable()).isTrue();
            }
        );
    }
}
