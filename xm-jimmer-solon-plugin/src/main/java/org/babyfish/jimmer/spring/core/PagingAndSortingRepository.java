package org.babyfish.jimmer.spring.core;

import org.babyfish.jimmer.spring.core.page.Page;
import org.babyfish.jimmer.spring.core.page.Pageable;
import org.babyfish.jimmer.spring.core.page.Sort;

public interface PagingAndSortingRepository<T, ID> extends CrudRepository<T, ID> {

	Iterable<T> findAll(Sort sort);

	Page<T> findAll(Pageable pageable);

}
