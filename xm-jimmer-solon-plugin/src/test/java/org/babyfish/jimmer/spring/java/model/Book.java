package org.babyfish.jimmer.spring.java.model;

import org.babyfish.jimmer.client.Doc;
import org.babyfish.jimmer.sql.Entity;
import org.babyfish.jimmer.sql.Id;
import org.babyfish.jimmer.sql.ManyToMany;
import org.babyfish.jimmer.sql.ManyToOne;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
public interface Book {

    @Id
    UUID id();

    String name();

    int edition();

    BigDecimal price();

    @Doc("The bookstore to which the current book belongs, null is allowd")
    @ManyToOne
    @Nullable
    BookStore store();

    @Doc("All authors involved in writing the work")
    @ManyToMany
    List<Author> authors();
}
