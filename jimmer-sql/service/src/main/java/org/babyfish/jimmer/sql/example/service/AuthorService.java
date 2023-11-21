package org.babyfish.jimmer.sql.example.service;

import com.xunmo.jimmer.annotation.Db;
import com.xunmo.jimmer.model.SortUtils;
import lombok.extern.slf4j.Slf4j;
import org.babyfish.jimmer.client.FetchBy;
import org.babyfish.jimmer.client.ThrowsAll;
import org.babyfish.jimmer.sql.example.model.dto.AuthorInput;
import org.babyfish.jimmer.sql.example.repository.AuthorRepository;
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
@Mapping("/author")
public class AuthorService {

    @Db
    private AuthorRepository authorRepository;

    @Get
    @Mapping("/simpleList")
    public List<@FetchBy("SIMPLE_FETCHER") Author> findSimpleAuthors() {
        return authorRepository.findAll(SIMPLE_FETCHER, AuthorProps.FIRST_NAME, AuthorProps.LAST_NAME);
    }

    @Get@Mapping("/list")
    public List<@FetchBy("DEFAULT_FETCHER") Author> findAuthors(
            @Param(defaultValue = "firstName asc, lastName asc") String sortCode,
            @Param(required = false) String firstName,
            @Param(required = false) String lastName,
            @Param(required = false) Gender gender
    ) {
        return authorRepository.findByFirstNameAndLastNameAndGender(
                SortUtils.toSort(sortCode),
                firstName,
                lastName,
                gender,
                DEFAULT_FETCHER
        );
    }

    @Get@Mapping("/{id}")
    @Nullable
    public @FetchBy("COMPLEX_FETCHER") Author findComplexAuthor(
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

    @Put
    @Mapping
    @ThrowsAll(SaveErrorCode.class)
    public Author saveAuthor(AuthorInput input) {
        return authorRepository.save(input);
    }

    @Delete
    @Mapping("/{id}")
    public void deleteAuthor(@Path("id") long id) {
        authorRepository.deleteById(id);
    }
}
