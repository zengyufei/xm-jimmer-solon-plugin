package org.babyfish.jimmer.sql.example.service;

import lombok.extern.slf4j.Slf4j;
import org.babyfish.jimmer.client.FetchBy;
import org.babyfish.jimmer.client.ThrowsAll;
import org.babyfish.jimmer.spring.core.annotation.Db;
import org.babyfish.jimmer.sql.example.model.*;
import org.babyfish.jimmer.sql.example.repository.BookStoreRepository;
import org.babyfish.jimmer.sql.example.service.dto.BookStoreInput;
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
@Slf4j
@Valid
@Controller
@Mapping("/bookStore")
public class BookStoreService {

    @Db
    private BookStoreRepository bookStoreRepository;


    @Get
    @Mapping("/simpleList")
    public List<@FetchBy("SIMPLE_FETCHER") BookStore> findSimpleStores() { // ❶
        return bookStoreRepository.findAll(
                SIMPLE_FETCHER,
                BookStoreProps.NAME
        );
    }

    @Get@Mapping("/list")
    public List<@FetchBy("DEFAULT_FETCHER") BookStore> findStores() { // ❷
        return bookStoreRepository.findAll(
                DEFAULT_FETCHER,
                BookStoreProps.NAME
        );
    }

    @Get@Mapping("/complexList")
    public List<@FetchBy("WITH_ALL_BOOKS_FETCHER") BookStore> findComplexStores() { // ❸
        return bookStoreRepository.findAll(
                WITH_ALL_BOOKS_FETCHER,
                BookStoreProps.NAME
        );
    }

    @Get@Mapping("/{id}/withAllBooks")
    @Nullable
    public @FetchBy("WITH_ALL_BOOKS_FETCHER") BookStore findComplexStoreWithAllBooks( // ❹
            @Path("id") long id
    ) {
        return bookStoreRepository.findNullable(id, WITH_ALL_BOOKS_FETCHER);
    }

    @Get@Mapping("/{id}/withNewestBooks")
    @Nullable
    public @FetchBy("WITH_NEWEST_BOOKS_FETCHER") BookStore findComplexStoreWithNewestBooks( // ❺
            @Path("id") long id
    ) {
        return bookStoreRepository.findNullable(id, WITH_NEWEST_BOOKS_FETCHER);
    }

    private static final Fetcher<BookStore> SIMPLE_FETCHER =
            BookStoreFetcher.$.name();

    private static final Fetcher<BookStore> DEFAULT_FETCHER =
            BookStoreFetcher.$.allScalarFields();

    private static final Fetcher<BookStore> WITH_ALL_BOOKS_FETCHER =
            BookStoreFetcher.$
                    .allScalarFields()
                    .avgPrice()
                    .books(
                            BookFetcher.$
                                    .allScalarFields()
                                    .tenant(false)
                                    .authors(
                                            AuthorFetcher.$
                                                    .allScalarFields()
                                    )
                    );

    private static final Fetcher<BookStore> WITH_NEWEST_BOOKS_FETCHER =
            BookStoreFetcher.$
                    .allScalarFields()
                    .avgPrice()
                    .newestBooks(
                            BookFetcher.$
                                    .allScalarFields()
                                    .tenant(false)
                                    .authors(
                                            AuthorFetcher.$
                                                    .allScalarFields()
                                    )
                    );

    @Put@Mapping
    @ThrowsAll(SaveErrorCode.class) // ❻
    public BookStore saveBookStore(@Body BookStoreInput input) { // ❼
        return bookStoreRepository.save(input);
    }

    @Delete@Mapping("/{id}")
    public void deleteBookStore(@Path("id") long id) {
        bookStoreRepository.deleteById(id);
    }
}

/*----------------Documentation Links----------------
❶ ❷ ❸ ❹ ❺ https://babyfish-ct.github.io/jimmer/docs/spring/client/api#declare-fetchby
❻ https://babyfish-ct.github.io/jimmer/docs/spring/client/error#allow-to-throw-all-exceptions-of-family
❼ https://babyfish-ct.github.io/jimmer/docs/mutation/save-command/input-dto/
---------------------------------------------------*/
