package org.babyfish.jimmer.sql.example.service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.babyfish.jimmer.client.FetchBy;
import org.babyfish.jimmer.client.ThrowsAll;
import org.babyfish.jimmer.spring.core.annotation.Db;
import org.babyfish.jimmer.spring.model.SortUtils;
import org.babyfish.jimmer.sql.example.model.*;
import org.babyfish.jimmer.sql.example.repository.AuthorRepository;
import org.babyfish.jimmer.sql.example.service.dto.AuthorInput;
import org.babyfish.jimmer.sql.example.service.dto.AuthorSpecification;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.babyfish.jimmer.sql.runtime.SaveErrorCode;
import org.jetbrains.annotations.Nullable;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.Valid;

import java.util.List;

/*
 * Why add spring web annotations to the service class?
 *
 * The success and popularity of rich client technologies represented by React, Vue and Angular
 * have greatly reduced the significance of the Controller layer on the spring server side.
 *
 * Moreover, over-bloated code structures are not conducive to demonstrating the capabilities
 * of the framework with small examples. Therefore, this example project no longer adheres to
 * dogmatism and directly adds spring web annotations to the service class.
 */
@Api("作者模型")
@Valid
@Controller
@Mapping("/author")
public class AuthorService {

    @Db
    private AuthorRepository authorRepository;

    @ApiOperation("简单查询列表接口")
    @Get
    @Mapping("/simpleList")
    public List<@FetchBy("SIMPLE_FETCHER") Author> findSimpleAuthors() { // ❶ ❷ ❸
        return authorRepository.findAll(SIMPLE_FETCHER, AuthorProps.FIRST_NAME, AuthorProps.LAST_NAME);
    }

    @ApiOperation("超级分页查询列表接口")
    @Post@Mapping("/list")
    public List<@FetchBy("DEFAULT_FETCHER") Author> findAuthors( // ❷
            AuthorSpecification specification
            //@Param(defaultValue = "firstName asc, lastName asc") String sortCode
    ) {
        return authorRepository.find(
                specification,
                SortUtils.toSort("firstName"),
                DEFAULT_FETCHER
        );
    }

    @ApiOperation("单个查询接口")
    @Get@Mapping("/{id}")
    @Nullable
    public @FetchBy("COMPLEX_FETCHER") Author findComplexAuthor( // ❸
            @Path("id") long id
    ) {
        return authorRepository.findNullable(id, COMPLEX_FETCHER);
    }

    private static final Fetcher<Author> SIMPLE_FETCHER =
            AuthorFetcher.$
                    .firstName()
                    .lastName();

    private static final Fetcher<Author> DEFAULT_FETCHER =
            AuthorFetcher.$
                    .allScalarFields();

    private static final Fetcher<Author> COMPLEX_FETCHER =
            AuthorFetcher.$
                    .allScalarFields()
                    .books(
                            BookFetcher.$
                                    .allScalarFields()
                                    .tenant(false)
                                    .store(
                                            BookStoreFetcher.$
                                                    .allScalarFields()
                                                    .avgPrice()
                                    )
                    );

    @ApiOperation("新增接口")
    @Put
    @Mapping
    @ThrowsAll(SaveErrorCode.class) // ❹
    public Author saveAuthor(AuthorInput input) { // ❺
        return authorRepository.save(input);
    }

    @ApiOperation("删除接口")
    @Delete
    @Mapping("/{id}")
    public void deleteAuthor(@Path("id") long id) {
        authorRepository.deleteById(id);
    }
}

/*----------------Documentation Links----------------
❶ ❷ ❸ https://babyfish-ct.github.io/jimmer/docs/spring/client/api#declare-fetchby
❹ https://babyfish-ct.github.io/jimmer/docs/spring/client/error#allow-to-throw-all-exceptions-of-family
❺ https://babyfish-ct.github.io/jimmer/docs/mutation/save-command/input-dto/
---------------------------------------------------*/
