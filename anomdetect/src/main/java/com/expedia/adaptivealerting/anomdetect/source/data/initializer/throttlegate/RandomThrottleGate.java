/*
 * Copyright 2018-2019 Expedia Group, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expedia.adaptivealerting.anomdetect.source.data.initializer.throttlegate;

import lombok.extern.slf4j.Slf4j;

import java.util.Random;

@Slf4j
public class RandomThrottleGate implements ThrottleGate {

    double throttleGateLikelihood;
    Random randomNumberGenerator;

    public RandomThrottleGate(double throttleGateLikelihood) {
        this.throttleGateLikelihood = throttleGateLikelihood;
        this.randomNumberGenerator = new Random();
    }

    @Override
    public boolean isOpen() {
        double chance = randomNumberGenerator.nextDouble();
        boolean result = chance <= throttleGateLikelihood;
        log.debug(String.format("Generated random number %.2f, which is %s than throttleGateLikelihood (%.2f). ThrottleGate is %s.",
                chance,
                (result ? "less" : "greater"),
                throttleGateLikelihood,
                (result ? "OPEN" : "CLOSED")));
        return result;
    }
}
