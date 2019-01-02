/*
 * Copyright 2018 Expedia Group, Inc.
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
package com.expedia.adaptivealerting.modelservice.service;

import com.expedia.adaptivealerting.modelservice.entity.UserInfo;
import com.expedia.adaptivealerting.modelservice.repo.UserDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Sign up service to add new users
 *
 * @author kashah
 */
@Service
public class SignUpService {

    @Autowired
    private UserDetailsRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserInfo addUser(UserInfo user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        return userRepository.save(user);
    }

}
