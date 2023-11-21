package org.babyfish.jimmer.sql.example.repository;

import com.xunmo.jimmer.page.Sort;
import com.xunmo.jimmer.repository.JRepository;
import org.babyfish.jimmer.sql.example.model.Author;
import org.babyfish.jimmer.sql.example.model.Gender;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface AuthorRepository extends JRepository<Author, Long> {

    List<Author> findByFirstNameAndLastNameAndGender(
            Sort sort,
            @Nullable String firstName,
            @Nullable String lastName,
            @Nullable Gender gender,
            Fetcher<Author> fetcher
    );
}
