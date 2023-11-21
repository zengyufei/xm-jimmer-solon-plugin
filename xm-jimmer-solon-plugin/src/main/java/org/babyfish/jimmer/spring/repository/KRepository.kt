package org.babyfish.jimmer.spring.repository

import org.babyfish.jimmer.Input
import org.babyfish.jimmer.View
import org.babyfish.jimmer.meta.ImmutableType
import org.babyfish.jimmer.spring.core.PagingAndSortingRepository
import org.babyfish.jimmer.sql.ast.mutation.DeleteMode
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.mutation.KBatchSaveResult
import org.babyfish.jimmer.sql.kt.ast.mutation.KSaveCommandDsl
import org.babyfish.jimmer.sql.kt.ast.mutation.KSimpleSaveResult
import org.babyfish.jimmer.sql.kt.ast.query.KConfigurableRootQuery
import org.babyfish.jimmer.sql.kt.ast.query.SortDsl
import org.babyfish.jimmer.spring.core.page.Page
import org.babyfish.jimmer.spring.core.page.Pageable
import org.babyfish.jimmer.spring.core.page.Sort
import java.util.*
import kotlin.reflect.KClass

//@NoRepositoryBean // 不兼容 solon
interface KRepository<E: Any, ID: Any> : PagingAndSortingRepository<E, ID> {

    val sql: KSqlClient

    val type: ImmutableType

    val entityType: KClass<E>

    @Deprecated("Replaced by KConfigurableQuery<E, R>.fetchPage, will be removed in 1.0")
    fun pager(pageIndex: Int, pageSize: Int): Pager

    @Deprecated("Replaced by KConfigurableQuery<E, R>.fetchPage, will be removed in 1.0")
    fun pager(pageable: Pageable): Pager

    fun findNullable(id: ID, fetcher: Fetcher<E>? = null): E?

    override fun findById(id: ID): Optional<E> =
        Optional.ofNullable(findNullable(id))

    fun findById(id: ID, fetcher: Fetcher<E>): Optional<E> =
        Optional.ofNullable(findNullable(id, fetcher))

//    @AliasFor("findAllById")
    fun findByIds(ids: Iterable<ID>, fetcher: Fetcher<E>? = null): List<E>

//    @AliasFor("findByIds")
    override fun findAllById(ids: Iterable<ID>): List<E> = 
        findByIds(ids)

    fun findMapByIds(ids: Iterable<ID>, fetcher: Fetcher<E>? = null): Map<ID, E>

    override fun findAll(): List<E> =
        findAll(null)

    fun findAll(fetcher: Fetcher<E>? = null): List<E>

    fun findAll(fetcher: Fetcher<E>? = null, block: (SortDsl<E>.() -> Unit)): List<E>

    override fun findAll(sort: Sort): List<E> =
        findAll(null, sort)

    fun findAll(fetcher: Fetcher<E>? = null, sort: Sort): List<E>

    fun findAll(
        pageIndex: Int,
        pageSize: Int,
        fetcher: Fetcher<E>? = null,
        block: (SortDsl<E>.() -> Unit)? = null
    ): Page<E>

    fun findAll(
        pageIndex: Int,
        pageSize: Int,
        fetcher: Fetcher<E>? = null,
        sort: Sort
    ): Page<E>

    override fun findAll(pageable: Pageable): Page<E>

    fun findAll(pageable: Pageable, fetcher: Fetcher<E>? = null): Page<E>

    override fun existsById(id: ID): Boolean =
        findNullable(id) != null
    
    override fun count(): Long

    fun insert(input: Input<E>): E =
        save(input.toEntity(), SaveMode.INSERT_ONLY).modifiedEntity

    fun insert(entity: E): E =
        save(entity, SaveMode.INSERT_ONLY).modifiedEntity

    fun update(input: Input<E>): E =
        save(input.toEntity(), SaveMode.UPDATE_ONLY).modifiedEntity

    fun update(entity: E): E =
        save(entity, SaveMode.UPDATE_ONLY).modifiedEntity

    fun save(input: Input<E>): E =
        save(input.toEntity(), SaveMode.UPSERT).modifiedEntity

    override fun <S: E> save(entity: S): S =
        save(entity, SaveMode.UPSERT).modifiedEntity

    fun save(input: Input<E>, mode: SaveMode): KSimpleSaveResult<E> =
        save(input.toEntity(), mode)

    fun <S: E> save(entity: S, mode: SaveMode): KSimpleSaveResult<S> =
        save(entity) {
            setMode(mode)
        }

    fun save(input: Input<E>, block: KSaveCommandDsl.() -> Unit): KSimpleSaveResult<E> =
        save(input.toEntity(), block)

    fun <S: E> save(entity: S, block: KSaveCommandDsl.() -> Unit): KSimpleSaveResult<S>

    override fun <S : E> saveAll(entities: Iterable<S>): List<S> =
        saveAll(entities, SaveMode.UPSERT).simpleResults.map { it.modifiedEntity }

    fun <S : E> saveAll(entities: Iterable<S>, mode: SaveMode): KBatchSaveResult<S> =
        saveAll(entities) {
            setMode(mode)
        }

    fun <S : E> saveAll(entities: Iterable<S>, block: KSaveCommandDsl.() -> Unit): KBatchSaveResult<S>

    override fun delete(entity: E) {
        delete(entity, DeleteMode.AUTO)
    }

    fun delete(entity: E, mode: DeleteMode): Int

    override fun deleteById(id: ID) {
        deleteById(id, DeleteMode.AUTO)
    }

    fun deleteById(id: ID, mode: DeleteMode): Int

//    @AliasFor("deleteAllById")
    fun deleteByIds(ids: Iterable<ID>) {
        deleteByIds(ids, DeleteMode.AUTO)
    }

//    @AliasFor("deleteByIds")
    override fun deleteAllById(ids: Iterable<ID>) {
        deleteByIds(ids, DeleteMode.AUTO)
    }

    fun deleteByIds(ids: Iterable<ID>, mode: DeleteMode): Int

    override fun deleteAll(entities: Iterable<E>) {
        deleteAll(entities, DeleteMode.AUTO)
    }

    fun deleteAll(entities: Iterable<E>, mode: DeleteMode): Int

    override fun deleteAll()

    fun <V: View<E>> viewer(viewType: KClass<V>): Viewer<E, ID, V>

    @Deprecated("Replaced by KConfigurableQuery<E, R>.fetchPage, will be removed in 1.0")
    interface Pager {

        fun <T> execute(query: KConfigurableRootQuery<*, T>): Page<T>
    }

    interface Viewer<E: Any, ID, V: View<E>> {

        fun findNullable(id: ID): V?

        fun findByIds(ids: Iterable<ID>?): List<V>

        fun findMapByIds(ids: Iterable<ID>?): Map<ID, V>

        fun findAll(): List<V>

        fun findAll(block: (SortDsl<E>.() -> Unit)): List<V>

        fun findAll(sort: Sort): List<V>

        fun findAll(pageable: Pageable): Page<V>

        fun findAll(pageIndex: Int, pageSize: Int): Page<V>

        fun findAll(pageIndex: Int, pageSize: Int, block: (SortDsl<E>.() -> Unit)): Page<V>

        fun findAll(pageIndex: Int, pageSize: Int, sort: Sort): Page<V>
    }
}
