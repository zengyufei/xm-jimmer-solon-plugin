package org.babyfish.jimmer.sql.example.repository;

import org.babyfish.jimmer.Specification;
import org.babyfish.jimmer.spring.core.page.Page;
import org.babyfish.jimmer.spring.core.page.Pageable;
import org.babyfish.jimmer.spring.repository.JRepository;
import org.babyfish.jimmer.spring.repository.SpringOrders;
import org.babyfish.jimmer.sql.ast.Expression;
import org.babyfish.jimmer.sql.ast.Predicate;
import org.babyfish.jimmer.sql.ast.tuple.Tuple2;
import org.babyfish.jimmer.sql.example.model.AuthorTableEx;
import org.babyfish.jimmer.sql.example.model.Book;
import org.babyfish.jimmer.sql.example.model.BookTable;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface BookRepository extends JRepository<Book, Long> { // ❶

    BookTable book = BookTable.$;

    /**
     * Manually implement complex query.
     *
     * <p>The functionality of this method is the same as the super QBE method
     * {@link #find(Pageable, Specification, Fetcher)}</p>
     */
    default Page<Book> findBooks( // ❷
                                  Pageable pageable,
                                  @Nullable String name,
                                  @Nullable BigDecimal minPrice,
                                  @Nullable BigDecimal maxPrice,
                                  @Nullable String storeName,
                                  @Nullable String authorName,
                                  @Nullable Fetcher<Book> fetcher
    ) {
        AuthorTableEx author = AuthorTableEx.$;
        return pager(pageable) // ❸
                .execute(
                        sql()
                                .createQuery(book)
                                .whereIf( // ❹
                                        name != null && !name.isEmpty(),
                                        book.name().ilike(name)
                                )
                                .whereIf(minPrice != null, () -> book.price().ge(minPrice))
                                .whereIf(maxPrice != null, () -> book.price().le(maxPrice))
                                .whereIf( // ❺
                                        storeName != null && !storeName.isEmpty(),
                                        book.store().name().ilike(storeName) // ❻
                                )
                                .whereIf( // ❼
                                        authorName != null && !authorName.isEmpty(),
                                        book.id().in(sql()
                                                .createSubQuery(author) //  ❽
                                                .where(
                                                        Predicate.or(
                                                                author.firstName().ilike(authorName),
                                                                author.lastName().ilike(authorName)
                                                        )
                                                )
                                                .select(author.books().id()) // ❾
                                        )
                                )
                                .orderBy(SpringOrders.toOrders(book, pageable.getSort())) // ❿
                                .select(book.fetch(fetcher)) // ⓫
                );
    }

    /**
     * Super QBE.
     *
     * <p>The functionality of this method is the same as the manual method
     * {@link #findBooks(Pageable, String, BigDecimal, BigDecimal, String, String, Fetcher)}</p>
     */
    Page<Book> find(
            Pageable pageable,
            Specification<Book> specification,
            @Nullable Fetcher<Book> fetcher
    );

    default Map<Long, BigDecimal> findAvgPriceGroupByStoreId(Collection<Long> storeIds) {
        return Tuple2.toMap(
                sql()
                        .createQuery(book)
                        .where(book.store().id().in(storeIds)) // ⓬
                        .groupBy(book.store().id()) // ⓭
                        .select(
                                book.store().id(), // ⓮
                                book.price().avg()
                        )
                        .execute()
        );
    }

    default Map<Long, List<Long>> findNewestIdsGroupByStoreId(Collection<Long> storeIds) {
        return Tuple2.toMultiMap(
                sql()
                        .createQuery(book)
                        .where(
                                Expression.tuple(book.name(), book.edition()).in(
                                        sql().createSubQuery(book) // ⓯
                                                // Apply root predicate to sub query is faster here.
                                                .where(book.store().id().in(storeIds)) // ⓰
                                                .groupBy(book.name())
                                                .select(
                                                        book.name(),
                                                        book.edition().max()
                                                )
                                )
                        )
                        .select(
                                book.store().id(), // ⓱
                                book.id()
                        )
                        .execute()
        );
    }
}

/*----------------Documentation Links----------------
❶ https://babyfish-ct.github.io/jimmer/docs/spring/repository/concept

❷ https://babyfish-ct.github.io/jimmer/docs/spring/repository/default

❸ https://babyfish-ct.github.io/jimmer/docs/spring/repository/default#pagination
  https://babyfish-ct.github.io/jimmer/docs/query/paging/

❹ ❺ ❼ https://babyfish-ct.github.io/jimmer/docs/query/dynamic-where

❻ https://babyfish-ct.github.io/jimmer/docs/query/dynamic-join/

❽ ⓯ https://babyfish-ct.github.io/jimmer/docs/query/sub-query

❾ https://babyfish-ct.github.io/jimmer/docs/query/dynamic-join/optimization#half-joins

❿ https://babyfish-ct.github.io/jimmer/docs/query/dynamic-order

⓫ https://babyfish-ct.github.io/jimmer/docs/query/object-fetcher/

⓬ ⓭ ⓮ ⓰ ⓱ https://babyfish-ct.github.io/jimmer/docs/query/dynamic-join/optimization#ghost-joins
---------------------------------------------------*/
