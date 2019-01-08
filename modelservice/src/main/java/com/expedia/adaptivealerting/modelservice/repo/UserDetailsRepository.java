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
package com.expedia.adaptivealerting.modelservice.repo;

import com.expedia.adaptivealerting.modelservice.entity.UserInfo;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Spring Data repository for user info.
 *
 * @author Karan Shah
 */
public interface UserDetailsRepository extends CrudRepository<UserInfo, Long> {

    /**
     * Finds a valid user by its name, if any.
     *
     * @param username username.
     * @param enabled  enabled flag.
     * @return User identified by the user name.
     */
    UserInfo findByUsernameAndEnabled(String username, boolean enabled);

    /**
     * Finds a list of users by enabled flag.
     *
     * @param enabled enabled flag.
     * @return List of users identified by the enabled flag.
     */
    List<UserInfo> findAllByEnabled(boolean enabled);


    @Override
    UserInfo save(UserInfo userInfo);

}
