package org.babyfish.jimmer.spring.cfg;

import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.kt.KSqlClient;
import org.babyfish.jimmer.sql.kt.impl.KSqlClientImplementor;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.noear.solon.core.event.AppLoadEndEvent;
import org.noear.solon.core.event.EventListener;

import java.util.List;

public class SqlClientInitializer implements EventListener<AppLoadEndEvent> {

    private final List<JSqlClient> javaSqlClients;

    private final List<KSqlClient> kotlinSqlClients;

    public SqlClientInitializer(List<JSqlClient> javaSqlClients, List<KSqlClient> kotlinSqlClients) {
        this.javaSqlClients = javaSqlClients;
        this.kotlinSqlClients = kotlinSqlClients;
    }

	@Override
	public void onEvent(AppLoadEndEvent event) throws Throwable {
        for (JSqlClient sqlClient : javaSqlClients) {
            ((JSqlClientImplementor) sqlClient).initialize();
        }
        for (KSqlClient sqlClient : kotlinSqlClients) {
            ((KSqlClientImplementor) sqlClient).initialize();
        }
    }
}
