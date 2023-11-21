package com.example.demo.user.repository;

import com.example.demo.user.entity.User;
import org.babyfish.jimmer.spring.core.annotation.Db;
import org.babyfish.jimmer.spring.repository.JRepository;

@Db
public interface UserRepository extends JRepository<User, String> {
}
