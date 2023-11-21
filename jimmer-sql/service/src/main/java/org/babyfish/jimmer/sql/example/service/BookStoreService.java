package org.babyfish.jimmer.sql.example.service;

import com.xunmo.jimmer.annotation.Db;
import lombok.extern.slf4j.Slf4j;
import org.babyfish.jimmer.client.FetchBy;
import org.babyfish.jimmer.client.ThrowsAll;
import org.babyfish.jimmer.sql.example.model.dto.BookStoreInput;
import org.babyfish.jimmer.sql.example.repository.BookStoreRepository;
import org.babyfish.jimmer.sql.example.model.*;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.babyfish.jimmer.sql.runtime.SaveErrorCode;
import org.jetbrains.annotations.Nullable;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.Valid;

import java.util.List;

/**
 * A real project should be a three-tier architecture consisting
 * of repository, service, and controller.
 *
 * This demo has no business logic, its purpose is only to tell users
 * how to use jimmer with the <b>least</b> code. Therefore, this demo
 * does not follow this convention, and let services be directly
 * decorated by `@RestController`, not `@Service`.
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
    public List<@FetchBy("SIMPLE_FETCHER") BookStore> findSimpleStores() {
        return bookStoreRepository.findAll(
                SIMPLE_FETCHER,
                BookStoreProps.NAME
        );
    }

    @Get@Mapping("/list")
    public List<@FetchBy("DEFAULT_FETCHER") BookStore> findStores() {
        return bookStoreRepository.findAll(
                DEFAULT_FETCHER,
                BookStoreProps.NAME
        );
    }

    @Get@Mapping("/complexList")
    public List<@FetchBy("WITH_ALL_BOOKS_FETCHER") BookStore> findComplexStores() {
        return bookStoreRepository.findAll(
                WITH_ALL_BOOKS_FETCHER,
                BookStoreProps.NAME
        );
    }

    @Get@Mapping("/{id}/withAllBooks")
    @Nullable
    public @FetchBy("WITH_ALL_BOOKS_FETCHER") BookStore findComplexStoreWithAllBooks(
            @Path("id") long id
    ) {
        return bookStoreRepository.findNullable(id, WITH_ALL_BOOKS_FETCHER);
    }

    @Get@Mapping("/{id}/withNewestBooks")
    @Nullable
    public @FetchBy("WITH_NEWEST_BOOKS_FETCHER") BookStore findComplexStoreWithNewestBooks(
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

    @Put
    @Mapping
    @ThrowsAll(SaveErrorCode.class)
    public BookStore saveBookStore(@Body BookStoreInput input) {
        return bookStoreRepository.save(input);
    }

    @Delete@Mapping("/{id}")
    public void deleteBookStore(@Path("id") long id) {
        bookStoreRepository.deleteById(id);
    }
}
