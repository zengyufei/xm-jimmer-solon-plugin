package org.babyfish.jimmer.spring.repository

import org.babyfish.jimmer.spring.repository.support.Utils
import org.babyfish.jimmer.sql.kt.ast.query.KConfigurableRootQuery
import org.babyfish.jimmer.sql.kt.ast.query.fetchPage
import org.babyfish.jimmer.spring.core.page.Page
import org.babyfish.jimmer.spring.core.page.PageImpl
import org.babyfish.jimmer.spring.core.page.PageRequest
import org.babyfish.jimmer.spring.core.page.Pageable
import java.sql.Connection

fun <E> KConfigurableRootQuery<*, E>.fetchPage(
    pageIndex: Int,
    pageSize: Int,
    con: Connection? = null
): Page<E> =
    fetchPage(
        pageIndex,
        pageSize,
        con
    ) { entities, totalCount, queryImplementor ->
        PageImpl(
            entities,
            PageRequest.of(
                pageIndex,
                pageSize,
                Utils.toSort(
                    queryImplementor.javaOrders,
                    queryImplementor.javaSqlClient.metadataStrategy
                )
            ),
            totalCount.toLong()
        )
    }

fun <E> KConfigurableRootQuery<*, E>.fetchPage(
    pageable: Pageable?,
    con: Connection? = null
): Page<E> =
    if (pageable === null || pageable.isUnpaged) {
        fetchPage(0, 0, con)
    } else {
        fetchPage(pageable.pageNumber, pageable.pageSize, con)
    }
