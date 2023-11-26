package org.babyfish.jimmer.sql.example.service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.babyfish.jimmer.client.FetchBy;
import org.babyfish.jimmer.client.ThrowsAll;
import org.babyfish.jimmer.spring.core.annotation.Db;
import org.babyfish.jimmer.spring.core.page.Page;
import org.babyfish.jimmer.spring.core.page.PageRequest;
import org.babyfish.jimmer.spring.model.SortUtils;
import org.babyfish.jimmer.sql.example.model.*;
import org.babyfish.jimmer.sql.example.repository.BookRepository;
import org.babyfish.jimmer.sql.example.service.dto.BookInput;
import org.babyfish.jimmer.sql.example.service.dto.BookSpecification;
import org.babyfish.jimmer.sql.example.service.dto.CompositeBookInput;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.babyfish.jimmer.sql.runtime.SaveErrorCode;
import org.jetbrains.annotations.Nullable;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.Valid;

import java.math.BigDecimal;
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
@Api("书本模型")
@Valid
@Controller
@Mapping("/book")
public class BookService {

    @Db
    private BookRepository bookRepository;

    @ApiOperation("简单查询列表接口")
    @Get
    @Mapping("/simpleList")
    public List<@FetchBy("SIMPLE_FETCHER") Book> findSimpleBooks() { // ❶
        return bookRepository.findAll(SIMPLE_FETCHER, BookProps.NAME, BookProps.EDITION.desc());
    }

    /**
     * The functionality of this method is the same as
     * {@link #findBooksBySuperQBE(int, int, String, BookSpecification)}
     */
    @ApiOperation("普通分页查询列表接口")
    @Get
    @Mapping("/list")
    public Page<@FetchBy("DEFAULT_FETCHER") Book> findBooks( // ❷
                                                             @Param(defaultValue = "0") int pageIndex,
                                                             @Param(defaultValue = "5") int pageSize,
                                                             // The `sortCode` also support implicit join, like `store.name asc`
                                                             @Param(defaultValue = "name asc, edition desc") String sortCode,
                                                             @Param(required = false) String name,
                                                             @Param(required = false) BigDecimal minPrice,
                                                             @Param(required = false) BigDecimal maxPrice,
                                                             @Param(required = false) String storeName,
                                                             @Param(required = false) String authorName
    ) {
        return bookRepository.findBooks(
                PageRequest.of(pageIndex, pageSize, SortUtils.toSort(sortCode)),
                name,
                minPrice,
                maxPrice,
                storeName,
                authorName,
                DEFAULT_FETCHER
        );
    }

    /**
     * The functionality of this method is the same as
     * {@link #findBooks(int, int, String, String, BigDecimal, BigDecimal, String, String)}
     */
    @ApiOperation("超级分页查询列表接口")
    @Get
    @Mapping("/list/bySuperQBE")
    public Page<@FetchBy("DEFAULT_FETCHER") Book> findBooksBySuperQBE(
            @Param(defaultValue = "0") int pageIndex,
            @Param(defaultValue = "5") int pageSize,
            // The `sortCode` also support implicit join, like `store.name asc`
            @Param(defaultValue = "name asc, edition desc") String sortCode,
            BookSpecification specification
    ) {
        return bookRepository.find(
                PageRequest.of(pageIndex, pageSize, SortUtils.toSort(sortCode)),
                specification,
                DEFAULT_FETCHER
        );
    }

    @ApiOperation("单个查询接口")
    @Get
    @Mapping("/{id}")
    @Nullable
    public @FetchBy("COMPLEX_FETCHER") Book findComplexBook( // ❸
                                                             @Path("id") long id
    ) {
        return bookRepository.findNullable(id, COMPLEX_FETCHER);
    }

    private static final Fetcher<Book> SIMPLE_FETCHER =
            BookFetcher.$.name().edition();

    private static final Fetcher<Book> DEFAULT_FETCHER =
            BookFetcher.$
                    .allScalarFields()
                    .tenant(false)
                    .store(
                            BookStoreFetcher.$
                                    .name()
                    )
                    .authors(
                            AuthorFetcher.$
                                    .firstName()
                                    .lastName()
                    );

    private static final Fetcher<Book> COMPLEX_FETCHER =
            BookFetcher.$
                    .allScalarFields()
                    .tenant(false)
                    .store(
                            BookStoreFetcher.$
                                    .allScalarFields()
                                    .avgPrice()
                    )
                    .authors(
                            AuthorFetcher.$
                                    .allScalarFields()
                    );

    @ApiOperation("新增接口")
    @Put
    @Mapping
    @ThrowsAll(SaveErrorCode.class) // ❹
    public Book saveBook(@Body BookInput input) { // ❺
        return bookRepository.save(input);
    }

    @ApiOperation("新增接口 - 多表关系")
    @Put
    @Mapping("/composite")
    @ThrowsAll(SaveErrorCode.class) // ❻
    public Book saveCompositeBook(@Body CompositeBookInput input) { // ❼
        return bookRepository.save(input);
    }

    @ApiOperation("删除接口")
    @Delete
    @Mapping("/{id}")
    public void deleteBook(@Path("id") long id) {
        bookRepository.deleteById(id);
    }
}

/*----------------Documentation Links----------------
❶ ❷ ❸ https://babyfish-ct.github.io/jimmer/docs/spring/client/api#declare-fetchby
❹ ❻ https://babyfish-ct.github.io/jimmer/docs/spring/client/error#allow-to-throw-all-exceptions-of-family
❺ ❼ https://babyfish-ct.github.io/jimmer/docs/mutation/save-command/input-dto/
---------------------------------------------------*/
