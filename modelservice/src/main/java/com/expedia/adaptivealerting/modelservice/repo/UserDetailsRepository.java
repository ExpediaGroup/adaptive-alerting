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
     * @param userName user name.
     * @param enabled  enabled flag.
     * @return User identified by the user name.
     */
    UserInfo findByUserNameAndEnabled(String userName, boolean enabled);

    /**
     * Finds a list of users by enabled flag.
     *
     * @param enabled enabled flag.
     * @return List of users identified by the enabled flag.
     */
    List<UserInfo> findAllByEnabled(boolean enabled);

}
