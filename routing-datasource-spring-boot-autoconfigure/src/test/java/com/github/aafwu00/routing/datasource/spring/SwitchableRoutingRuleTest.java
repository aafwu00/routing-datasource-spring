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

package com.github.aafwu00.routing.datasource.spring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class SwitchableRoutingRuleTest {
    private Switching switching;
    private SwitchableRoutingRule rule;

    @BeforeEach
    void setUp() {
        switching = mock(Switching.class);
        rule = new SwitchableRoutingRuleImpl(switching);
    }

    @Test
    void decision() {
        assertAll(
            () -> {
                doReturn(false).when(switching).isOn();
                assertThat(rule.determineCurrentLookupKey()).isEqualTo(SwitchableMode.Off);
            },
            () -> {
                doReturn(true).when(switching).isOn();
                assertThat(rule.determineCurrentLookupKey()).isEqualTo(SwitchableMode.On);
            }
        );
    }
}
