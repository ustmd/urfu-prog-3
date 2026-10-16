package com.ustmd.labwork8.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ustmd.labwork8.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

}
