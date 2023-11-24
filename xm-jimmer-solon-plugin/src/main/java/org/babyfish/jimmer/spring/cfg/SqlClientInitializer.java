package org.babyfish.jimmer.spring.cfg;

import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.kt.KSqlClient;
import org.babyfish.jimmer.sql.kt.impl.KSqlClientImplementor;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;

import java.util.List;

public class SqlClientInitializer {

    public SqlClientInitializer(List<JSqlClient> javaSqlClients, List<KSqlClient> kotlinSqlClients) {
        for (JSqlClient sqlClient : javaSqlClients) {
            ((JSqlClientImplementor) sqlClient).initialize();
        }
        for (KSqlClient sqlClient : kotlinSqlClients) {
            ((KSqlClientImplementor) sqlClient).initialize();
        }
    }


}
