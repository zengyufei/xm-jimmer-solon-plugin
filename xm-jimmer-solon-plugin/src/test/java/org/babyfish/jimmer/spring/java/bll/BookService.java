package org.babyfish.jimmer.spring.java.bll;

import org.babyfish.jimmer.client.FetchBy;
import org.babyfish.jimmer.spring.core.annotation.Db;
import org.babyfish.jimmer.spring.core.page.Page;
import org.babyfish.jimmer.spring.java.dal.BookRepository;
import org.babyfish.jimmer.spring.java.model.*;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Get;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Param;

@Controller
public class BookService {

    @Db
    private BookRepository bookRepository;

    @Get
    @Mapping("/defaultBooks")
    public Page<Book> findDefaultBooks(
            @Param(defaultValue="0") int pageIndex,
            @Param(defaultValue="10") int pageSize,
            @Param String name,
            @Param String storeName,
            @Param String authorName
    ) {
        return bookRepository.findBooks(
                pageIndex,
                pageSize,
                name,
                storeName,
                authorName,
                null
        );
    }

    /**
     * Find books with simple format
     * @param pageIndex Page index starts from 1
     * @param pageSize How many rows in on page
     * @param name Optional value to filter `name`
     * @param storeName Optional value to filter `store.name`
     * @param authorName Optional value to filter `authors.name`
     * @return The books with simple format
     */
    @Get
    @Mapping("/simpleBooks")
    public Page<@FetchBy("SIMPLE_FETCHER") Book> findSimpleBooks(
            @Param int pageIndex,
            @Param int pageSize,
            @Param String name,
            @Param String storeName,
            @Param String authorName
    ) {
        return bookRepository.findBooks(
                pageIndex,
                pageSize,
                name,
                storeName,
                authorName,
                SIMPLE_FETCHER
        );
    }

    /**
     * Find books with complex format
     * @param pageIndex Page index starts from 1
     * @param pageSize How many rows in on page
     * @param name Optional value to filter `name`
     * @param storeName Optional value to filter `store.name`
     * @param authorName Optional value to filter `authors.name`
     * @return The books with complex format
     */
    @Get
    @Mapping("/complexBooks")
    public Page<@FetchBy("COMPLEX_FETCHER") Book> findComplexBooks(
            @Param int pageIndex,
            @Param int pageSize,
            @Param String name,
            @Param String storeName,
            @Param String authorName
    ) {
        return bookRepository.findBooks(
                pageIndex,
                pageSize,
                name,
                storeName,
                authorName,
                COMPLEX_FETCHER
        );
    }

    private static final Fetcher<Book> SIMPLE_FETCHER =
            BookFetcher.$.name();

    private static final BookFetcher COMPLEX_FETCHER =
            BookFetcher.$
                    .allScalarFields()
                    .store(
                            BookStoreFetcher.$.name()
                    )
                    .authors(
                            AuthorFetcher.$
                                    .allScalarFields()
                                    .gender(false)
                    );
}
