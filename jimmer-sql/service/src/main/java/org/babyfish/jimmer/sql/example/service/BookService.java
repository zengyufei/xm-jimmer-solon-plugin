package org.babyfish.jimmer.sql.example.service;

import com.xunmo.jimmer.annotation.Db;
import com.xunmo.jimmer.model.SortUtils;
import com.xunmo.jimmer.page.Page;
import com.xunmo.jimmer.page.PageRequest;
import lombok.extern.slf4j.Slf4j;
import org.babyfish.jimmer.client.FetchBy;
import org.babyfish.jimmer.client.ThrowsAll;
import org.babyfish.jimmer.sql.example.model.dto.BookInput;
import org.babyfish.jimmer.sql.example.model.dto.CompositeBookInput;
import org.babyfish.jimmer.sql.example.repository.BookRepository;
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
 * <p>
 * This demo has no business logic, its purpose is only to tell users
 * how to use jimmer with the <b>least</b> code. Therefore, this demo
 * does not follow this convention, and let services be directly
 * decorated by `@RestController`, not `@Service`.
 */
@Slf4j
@Valid
@Controller
@Mapping("/book")
public class BookService {

    @Db
    private BookRepository bookRepository;

    @Get
    @Mapping("/simpleList")
    public List<@FetchBy("SIMPLE_FETCHER") Book> findSimpleBooks() {
        return bookRepository.findAll(SIMPLE_FETCHER, BookProps.NAME, BookProps.EDITION.desc());
    }

    @Get
    @Mapping("/list")
    public Page<@FetchBy("DEFAULT_FETCHER") Book> findBooks(
            @Param(defaultValue = "0") int pageIndex,
            @Param(defaultValue = "5") int pageSize,
            // The `sortCode` also support implicit join, like `store.name asc`
            @Param(defaultValue = "name asc, edition desc") String sortCode,
            @Param(required = false) String name,
            @Param(required = false) String storeName,
            @Param(required = false) String authorName
    ) {
        return bookRepository.findBooks(
                PageRequest.of(pageIndex, pageSize, SortUtils.toSort(sortCode)),
                name,
                storeName,
                authorName,
                DEFAULT_FETCHER
        );
    }

    @Get
    @Mapping("/{id}")
    @Nullable
    public @FetchBy("COMPLEX_FETCHER") Book findComplexBook(
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

    @Put
    @Mapping
    @ThrowsAll(SaveErrorCode.class)
    public Book saveBook(@Body BookInput input) {
        return bookRepository.save(input);
    }

    @Put
    @Mapping("/composite")
    @ThrowsAll(SaveErrorCode.class)
    public Book saveCompositeBook(@Body CompositeBookInput input) {
        return bookRepository.save(input);
    }

    @Delete
    @Mapping("/{id}")
    public void deleteBook(@Path("id") long id) {
        bookRepository.deleteById(id);
    }
}
