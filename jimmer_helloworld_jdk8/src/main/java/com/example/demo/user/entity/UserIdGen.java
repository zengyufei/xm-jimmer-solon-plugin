package com.example.demo.user.entity;

import cn.hutool.core.util.IdUtil;
import org.babyfish.jimmer.sql.meta.UserIdGenerator;

public class UserIdGen implements UserIdGenerator<String> {
    @Override
    public String generate(Class<?> entityType) {
        return IdUtil.nanoId(20);
    }
}
