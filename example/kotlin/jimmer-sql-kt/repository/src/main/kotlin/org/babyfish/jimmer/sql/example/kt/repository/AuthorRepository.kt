package org.babyfish.jimmer.sql.example.kt.repository

import org.babyfish.jimmer.spring.repository.KRepository
import org.babyfish.jimmer.spring.repository.orderBy
import org.babyfish.jimmer.sql.example.kt.model.Author
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.ast.query.specification.KSpecification
import org.springframework.data.domain.Sort

interface AuthorRepository : KRepository<Author, Long> { // ❶

    fun find( // ❷
        specification: KSpecification<Author>,
        sort: Sort,
        fetcher: Fetcher<Author>?
    ): List<Author> =
        sql
            .createQuery(Author::class) {
                where(specification)
                orderBy(sort) // ❻
                select(table.fetch(fetcher)) // ❼
            }
            .execute()
}

/*----------------Documentation Links----------------
❶ https://babyfish-ct.github.io/jimmer/docs/spring/repository/concept
❷ https://babyfish-ct.github.io/jimmer/docs/spring/repository/default
❸ https://babyfish-ct.github.io/jimmer/docs/query/qbe
❹ ❺ https://babyfish-ct.github.io/jimmer/docs/query/dynamic-where
❻ https://babyfish-ct.github.io/jimmer/docs/query/dynamic-order
❼ https://babyfish-ct.github.io/jimmer/docs/query/object-fetcher/
---------------------------------------------------*/