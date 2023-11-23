package com.example.demo.user.repository;

import com.example.demo.user.entity.User;
import org.babyfish.jimmer.Specification;
import org.babyfish.jimmer.spring.core.annotation.Db;
import org.babyfish.jimmer.spring.core.page.Page;
import org.babyfish.jimmer.spring.core.page.Pageable;
import org.babyfish.jimmer.spring.repository.JRepository;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.jetbrains.annotations.Nullable;


@Db
public interface UserRepository extends JRepository<User, String> {

    Page<User> find(
            Pageable pageable,
            Specification<User> specification,
            @Nullable Fetcher<User> fetcher
    );

}
