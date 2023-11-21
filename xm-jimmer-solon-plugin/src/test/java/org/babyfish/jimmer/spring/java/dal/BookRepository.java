package org.babyfish.jimmer.spring.java.dal;

import org.babyfish.jimmer.Specification;
import org.babyfish.jimmer.spring.java.model.dto.BookView;
import org.babyfish.jimmer.spring.repository.JRepository;
import org.babyfish.jimmer.spring.java.model.*;
import org.babyfish.jimmer.sql.ast.Predicate;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.jetbrains.annotations.Nullable;
import org.babyfish.jimmer.spring.core.page.Page;
import org.babyfish.jimmer.spring.core.page.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface BookRepository extends JRepository<Book, Long> {

    BookTable table = BookTable.$;

    List<Book> findByNameOrderByNameAscEditionDesc(
            @Nullable String name,
            @Nullable Fetcher<Book> fetcher
    );

    BookView findByNameAndEdition(String name, int edition);

    int countByName(@Nullable String name);

    Page<Book> findByNameLikeIgnoreCaseAndStoreNameOrderByNameAscEditionDesc(
            Pageable pageable,
            @Nullable Fetcher<Book> fetcher,
            @Nullable String name,
            @Nullable String storeName
    );

    List<BigDecimal> findDistinctPriceByPriceBetween(
            @Nullable BigDecimal min,
            @Nullable BigDecimal max
    );

    Page<Book> findByNameAndEdition(
            String name,
            int edition,
            Pageable pageable,
            Fetcher<Book> fetcher
    );

    default Page<Book> findBooks(
            int pageIndex,
            int pageSize,
            @Nullable String name,
            @Nullable String storeName,
            @Nullable String authorName,
            Fetcher<Book> fetcher
    ) {
        AuthorTableEx author = AuthorTableEx.$;
        return pager(
                pageIndex,
                pageSize
        ).execute(
                sql().createQuery(table)
                        .whereIf(name != null, table.name().ilike(name))
                        .whereIf(storeName != null, table.store().name().ilike(name))
                        .whereIf(
                                authorName != null,
                                table.id().in(
                                        sql().createSubQuery(author)
                                                .where(
                                                        Predicate.or(
                                                                author.firstName().ilike(authorName),
                                                                author.lastName().ilike(authorName)
                                                        )
                                                )
                                                .select(author.books().id())
                                )
                        )
                        .select(table.fetch(fetcher))
        );
    }

    List<Book> findAll(Specification<Book> specification);
}
