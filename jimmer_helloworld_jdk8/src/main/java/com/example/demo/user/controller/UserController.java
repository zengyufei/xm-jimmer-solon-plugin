package com.example.demo.user.controller;

import com.example.demo.user.entity.User;
import com.example.demo.user.entity.UserFetcher;
import com.example.demo.user.entity.UserProps;
import com.example.demo.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.babyfish.jimmer.client.FetchBy;
import org.babyfish.jimmer.spring.core.annotation.Db;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Get;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.validation.annotation.Valid;

import java.util.List;

@Slf4j
@Valid
@Controller
@Mapping("/user")
public class UserController {


    @Db
    private JSqlClient sqlClient;

    @Db
    private UserRepository userRepository;

    private static final Fetcher<User> SIMPLE_FETCHER =
            UserFetcher.$
                    .userName();

    @Get
    @Mapping("/simpleList")
    public List<@FetchBy("SIMPLE_FETCHER") User> findSimpleAuthors() { // ❶ ❷ ❸
        return userRepository.findAll(SIMPLE_FETCHER, UserProps.USER_NAME);
    }
//
//    @Post
//    @Mapping("/list")
//    public Page<User> list(@Validated UserQuery query, PageRequest pageRequest) throws Exception {
//        final String userId = query.getUserId();
//        final String userName = query.getUserName();
//        final LocalDateTime beginCreateTime = query.getBeginCreateTime();
//        final LocalDateTime endCreateTime = query.getEndCreateTime();
//        return userRepository.pager(pageRequest)
//                .execute(sqlClient.createQuery(TABLE)
//                        // 根据 用户id 查询
//                        .whereIf(StrUtil.isNotBlank(userId), () -> TABLE.userId().eq(userId))
//                        // 根据 用户名称 模糊查询
//                        .whereIf(StrUtil.isNotBlank(userName), () -> TABLE.userName().like(userName))
//                        // 根据 创建时间 大于等于查询
//                        .whereIf(beginCreateTime != null, () -> TABLE.createTime().ge(beginCreateTime))
//                        // 根据 创建时间 小于等于查询
//                        .whereIf(endCreateTime != null, () -> TABLE.createTime().le(endCreateTime))
//                        // 默认排序 创建时间 倒排
//                        .orderBy(TABLE.createTime().desc())
//                        .select(TABLE.fetch(
//                                // 查询 用户表 所有属性（非对象）
//                                FETCHER.allScalarFields()
//                                        // 查询 创建者 对象，只显示 姓名
//                                        .create(UserFetcher.$.userName())
//                                        // 查询 修改者 对象，只显示 姓名
//                                        .update(UserFetcher.$.userName())
//                        )));
//    }
//
//    @Post
//    @Mapping("/getById")
//    public User getById(@NotNull @NotBlank String id) throws Exception {
////        final User user = userRepository.findById(id).orElse(null);
//        final User user = this.sqlClient.findById(User.class, id);
//        return user;
//    }
//
//    @Post
//    @Mapping("/add")
//    public User add(@Validated UserInput input) throws Exception {
////        final User modifiedEntity = userRepository.save(input);
//        final SimpleSaveResult<User> result = this.sqlClient.save(input);
//        final User modifiedEntity = result.getModifiedEntity();
//        return modifiedEntity;
//    }
//
//    @Post
//    @Mapping("/update")
//    public User update(@Validated UserInput input) throws Exception {
////        final User modifiedEntity = userRepository.update(input);
//        final SimpleSaveResult<User> result = this.sqlClient.update(input);
//        final User modifiedEntity = result.getModifiedEntity();
//        return modifiedEntity;
//    }
//
//    @Post
//    @Mapping("/deleteByIds")
//    @Tran
//    public int deleteByIds(List<String> ids) throws Exception {
////        final int totalAffectedRowCount = userRepository.deleteByIds(ids, DeleteMode.AUTO);
//        final DeleteResult result = this.sqlClient.deleteByIds(User.class, ids);
//        final int totalAffectedRowCount = result.getTotalAffectedRowCount();
//        return totalAffectedRowCount;
//    }
//
//    /**
//     * 主动抛出异常 - 用于测试
//     */
//    @Get
//    @Mapping("/exception")
//    public Boolean exception() throws Exception {
//        throw new NullPointerException("主动抛出异常 - 用于测试 " + DateUtil.now());
//    }

}
