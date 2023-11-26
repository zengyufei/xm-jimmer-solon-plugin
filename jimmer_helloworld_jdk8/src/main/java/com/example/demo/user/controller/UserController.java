package com.example.demo.user.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.example.demo.user.entity.User;
import com.example.demo.user.entity.UserFetcher;
import com.example.demo.user.entity.UserProps;
import com.example.demo.user.entity.dto.UserDelInput;
import com.example.demo.user.entity.dto.UserGetInput;
import com.example.demo.user.entity.dto.UserInput;
import com.example.demo.user.entity.dto.UserSpecification;
import com.example.demo.user.repository.UserRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.babyfish.jimmer.client.FetchBy;
import org.babyfish.jimmer.spring.core.annotation.Db;
import org.babyfish.jimmer.spring.core.page.Page;
import org.babyfish.jimmer.spring.core.page.PageRequest;
import org.babyfish.jimmer.spring.model.SortUtils;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.jetbrains.annotations.Nullable;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.Valid;

import java.util.List;

@Api("用户接口")
@Slf4j
@Valid
@Controller
@Mapping("/user")
public class UserController {

    @Db
    private UserRepository userRepository;

    private static final Fetcher<User> SIMPLE_FETCHER =
            UserFetcher.$
                    .userName()
                    .status()
                    .isSystemDefault();

    private static final Fetcher<User> DEFAULT_FETCHER =
            UserFetcher.$
                    .allScalarFields();
    private static final Fetcher<User> GET_FETCHER =
            UserFetcher.$
                    .allScalarFields();

    @ApiOperation("简单查询列表接口")
    @Post
    @Mapping("/simpleList")
    public List<@FetchBy("SIMPLE_FETCHER") User> simpleList() {
        return userRepository.findAll(SIMPLE_FETCHER, UserProps.USER_NAME, UserProps.STATUS, UserProps.IS_SYSTEM_DEFAULT);
    }

    @ApiOperation("超级分页查询列表接口")
    @Post
    @Mapping("/list")
    public Page<@FetchBy("DEFAULT_FETCHER") User> list(
            @Param(defaultValue = "0") int pageIndex,
            @Param(defaultValue = "5") int pageSize,
            @Param(defaultValue = "createTime asc") String sortCode,
            UserSpecification specification
    ) {
        return userRepository.find(
                PageRequest.of(pageIndex, pageSize, SortUtils.toSort(sortCode)),
                specification,
                DEFAULT_FETCHER
        );
    }


    @ApiOperation("单个查询接口")
    @Post
    @Mapping("/get")
    @Nullable
    public @FetchBy("GET_FETCHER") User getById(UserGetInput input) {
        final String userId = input.getUserId();
        if (StrUtil.isNotBlank(userId)) {
            return userRepository.findNullable(userId, GET_FETCHER);
        }
        return null;
    }

    @ApiOperation("新增接口")
    @Post
    @Mapping("/add")
    public User add(UserInput input) {
        return userRepository.save(input);
    }

    @ApiOperation("删除接口")
    @Post
    @Mapping("/del")
    public void delById(UserDelInput input) {
        final String userId = input.getUserId();
        if (StrUtil.isNotBlank(userId)) {
            userRepository.deleteById(userId);
        }
    }

    @ApiOperation("主动抛出异常")
    @Get
    @Mapping("/exception")
    public Boolean exception() throws Exception {
        throw new NullPointerException("主动抛出异常 - 用于测试 " + DateUtil.now());
    }

}
