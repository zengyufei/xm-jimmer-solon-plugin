package org.babyfish.jimmer.sql.example.model;

import org.babyfish.jimmer.sql.EnumItem;
import org.babyfish.jimmer.sql.EnumType;

@EnumType(EnumType.Strategy.NAME)
public enum Gender {

    @EnumItem(name = "M") // ❶
    MALE,

    @EnumItem(name = "F") // ❷
    FEMALE
}

/*----------------Documentation Links----------------
❶ ❷ https://babyfish-ct.github.io/jimmer/docs/mapping/advanced/enum
---------------------------------------------------*/
