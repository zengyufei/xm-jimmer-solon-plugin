package org.babyfish.jimmer.spring.repository;

import org.babyfish.jimmer.Input;
import org.babyfish.jimmer.View;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.meta.TypedProp;
import org.babyfish.jimmer.spring.core.PagingAndSortingRepository;
import org.babyfish.jimmer.spring.core.page.Page;
import org.babyfish.jimmer.spring.core.page.Pageable;
import org.babyfish.jimmer.spring.core.page.Sort;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.mutation.*;
import org.babyfish.jimmer.sql.ast.query.ConfigurableRootQuery;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

//@NoRepositoryBean // 不兼容 solon
public interface JRepository<E, ID> extends PagingAndSortingRepository<E, ID> {

    /*
     * For provider
     */

    JSqlClient sql();

    ImmutableType type();

    Class<E> entityType();

    Pager pager(Pageable pageable);

    Pager pager(int pageIndex, int pageSize);

    /*
     * For consumer
     */

    E findNullable(ID id);

    E findNullable(ID id, Fetcher<E> fetcher);

    @NotNull
    @Override
    default Optional<E> findById(ID id) {
        return Optional.ofNullable(findNullable(id));
    }

    default Optional<E> findById(ID id, Fetcher<E> fetcher) {
        return Optional.ofNullable(findNullable(id, fetcher));
    }

//    @AliasFor("findAllById")
    List<E> findByIds(Iterable<ID> ids);

//    @AliasFor("findByIds")
    @NotNull
    @Override
    default List<E> findAllById(@NotNull Iterable<ID> ids) {
        return findByIds(ids);
    }

    List<E> findByIds(Iterable<ID> ids, Fetcher<E> fetcher);

    Map<ID, E> findMapByIds(Iterable<ID> ids);

    Map<ID, E> findMapByIds(Iterable<ID> ids, Fetcher<E> fetcher);

    @NotNull
    @Override
    List<E> findAll();

    @SuppressWarnings("unchecked")
    List<E> findAll(TypedProp.Scalar<?, ?> ... sortedProps);

    @SuppressWarnings("unchecked")
    List<E> findAll(Fetcher<E> fetcher, TypedProp.Scalar<?, ?> ... sortedProps);

    @NotNull
    @Override
    List<E> findAll(@NotNull Sort sort);

    List<E> findAll(Fetcher<E> fetcher, Sort sort);

    Page<E> findAll(int pageIndex, int pageSize);

    Page<E> findAll(int pageIndex, int pageSize, Fetcher<E> fetcher);

    @SuppressWarnings("unchecked")
    Page<E> findAll(int pageIndex, int pageSize, TypedProp.Scalar<?, ?> ... sortedProps);

    @SuppressWarnings("unchecked")
    Page<E> findAll(int pageIndex, int pageSize, Fetcher<E> fetcher, TypedProp.Scalar<?, ?> ... sortedProps);

    Page<E> findAll(int pageIndex, int pageSize, Sort sort);

    Page<E> findAll(int pageIndex, int pageSize, Fetcher<E> fetcher, Sort sort);

    @NotNull
    @Override
    Page<E> findAll(@NotNull Pageable pageable);

    Page<E> findAll(Pageable pageable, Fetcher<E> fetcher);

    @Override
    default boolean existsById(ID id) {
        return findNullable(id) != null;
    }

    @Override
    long count();

    @NotNull
    default E insert(@NotNull Input<E> input) {
        return save(input.toEntity(), SaveMode.INSERT_ONLY).getModifiedEntity();
    }

    @NotNull
    default E insert(@NotNull E entity) {
        return save(entity, SaveMode.INSERT_ONLY).getModifiedEntity();
    }

    @NotNull
    default E update(@NotNull Input<E> input) {
        return save(input.toEntity(), SaveMode.UPDATE_ONLY).getModifiedEntity();
    }

    @NotNull
    default E update(@NotNull E entity) {
        return save(entity, SaveMode.UPDATE_ONLY).getModifiedEntity();
    }

    @NotNull
    default E save(@NotNull Input<E> input) {
        return save(input.toEntity(), SaveMode.UPSERT).getModifiedEntity();
    }

    @NotNull
    @Override
    default <S extends E> S save(@NotNull S entity) {
        return save(entity, SaveMode.UPSERT).getModifiedEntity();
    }

    @NotNull
    default SimpleSaveResult<E> save(
            @NotNull Input<E> input,
            SaveMode mode
    ) {
        return save(input.toEntity(), mode);
    }

    @NotNull
    <S extends E> SimpleSaveResult<S> save(@NotNull S entity, SaveMode mode);

    @NotNull
    SimpleEntitySaveCommand<E> saveCommand(@NotNull Input<E> input);

    @NotNull
    <S extends E> SimpleEntitySaveCommand<S> saveCommand(@NotNull S entity);

    @NotNull
    @Override
    default <S extends E> Iterable<S> saveAll(@NotNull Iterable<S> entities) {
        return saveAll(entities, SaveMode.UPSERT)
                .getSimpleResults()
                .stream()
                .map(SimpleSaveResult::getModifiedEntity)
                .collect(Collectors.toList());
    }

    @NotNull
    <S extends E> BatchSaveResult<S> saveAll(@NotNull Iterable<S> entities, SaveMode mode);

    @NotNull
    <S extends E> BatchEntitySaveCommand<S> saveAllCommand(@NotNull Iterable<S> entities);

    @Override
    default void delete(@NotNull E entity) {
        delete(entity, DeleteMode.AUTO);
    }

    int delete(@NotNull E entity, DeleteMode mode);

    @Override
    default void deleteAll(@NotNull Iterable<? extends E> entities) {
        deleteAll(entities, DeleteMode.AUTO);
    }

    int deleteAll(@NotNull Iterable<? extends E> entities, DeleteMode mode);

    @Override
    default void deleteById(@NotNull ID id) {
        deleteById(id, DeleteMode.AUTO);
    }

    int deleteById(@NotNull ID id, DeleteMode mode);

//    @AliasFor("deleteAllById")
    default void deleteByIds(Iterable<? extends ID> ids) {
        deleteByIds(ids, DeleteMode.AUTO);
    }

//    @AliasFor("deleteByIds")
    @Override
    default void deleteAllById(@NotNull Iterable<? extends ID> ids) {
        deleteByIds(ids, DeleteMode.AUTO);
    }

//    @AliasFor("deleteAllById")
    int deleteByIds(Iterable<? extends ID> ids, DeleteMode mode);

    @Override
    void deleteAll();

    <V extends View<E>> Viewer<E, ID, V> viewer(Class<V> viewType);

    interface Pager {

        <T> Page<T> execute(ConfigurableRootQuery<?, T> query);
    }

    interface Viewer<E, ID, V extends View<E>> {

        V findNullable(ID id);

        List<V> findByIds(Iterable<ID> ids);

        Map<ID, V> findMapByIds(Iterable<ID> ids);

        List<V> findAll();

        List<V> findAll(TypedProp.Scalar<?, ?> ... sortedProps);

        List<V> findAll(Sort sort);

        Page<V> findAll(Pageable pageable);

        Page<V> findAll(int pageIndex, int pageSize);

        Page<V> findAll(int pageIndex, int pageSize, TypedProp.Scalar<?, ?> ... sortedProps);

        Page<V> findAll(int pageIndex, int pageSize, Sort sort);
    }
}
