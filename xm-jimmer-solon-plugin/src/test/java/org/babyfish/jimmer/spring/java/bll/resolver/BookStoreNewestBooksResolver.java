package org.babyfish.jimmer.spring.java.bll.resolver;

import org.babyfish.jimmer.spring.core.annotation.Db;
import org.babyfish.jimmer.spring.java.dal.BookStoreRepository;
import org.babyfish.jimmer.sql.TransientResolver;
import org.babyfish.jimmer.sql.ast.tuple.Tuple2;
import org.noear.solon.annotation.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class BookStoreNewestBooksResolver implements TransientResolver<UUID, List<UUID>> {

    @Db
    private BookStoreRepository bookStoreRepository;

    @Override
    public Map<UUID, List<UUID>> resolve(Collection<UUID> ids) {
        return bookStoreRepository.findIdAndNewestBookIds(ids)
                .stream()
                .collect(
                        Collectors.groupingBy(
                                Tuple2<UUID, UUID>::get_1,
                                Collectors.mapping(
                                        Tuple2<UUID, UUID>::get_2,
                                        Collectors.toList()
                                )
                        )
                );
    }
}
