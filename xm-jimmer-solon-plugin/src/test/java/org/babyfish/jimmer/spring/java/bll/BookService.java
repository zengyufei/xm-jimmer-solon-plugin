package org.babyfish.jimmer.spring.java.bll;

import org.babyfish.jimmer.client.FetchBy;
import org.babyfish.jimmer.spring.core.annotation.Db;
import org.babyfish.jimmer.spring.core.page.Page;
import org.babyfish.jimmer.spring.java.dal.BookRepository;
import org.babyfish.jimmer.spring.java.model.*;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
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
                null
        );
    }

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

    private static final Fetcher<Book> COMPLEX_FETCHER =
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
