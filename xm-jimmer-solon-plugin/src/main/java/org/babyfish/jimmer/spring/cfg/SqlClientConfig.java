//package org.babyfish.jimmer.spring.cfg;
//
//import org.babyfish.jimmer.spring.SpringJSqlClient;
//import org.babyfish.jimmer.sql.JSqlClient;
//import org.babyfish.jimmer.sql.cache.CacheAbandonedCallback;
//import org.babyfish.jimmer.sql.kt.KSqlClient;
//import org.babyfish.jimmer.sql.kt.KSqlClientKt;
//import org.noear.solon.annotation.*;
//
//import java.util.List;
//
//@Configuration
//@Import({TransactionCacheOperatorFlusherConfig.class, MicroServiceExchangeConfig.class})
//public class SqlClientConfig {
//
//    @Bean(name = "sqlClient")
//    @ConditionalOnMissingBean({JSqlClient.class, KSqlClient.class})
//    @ConditionalOnProperty(name = "jimmer.language", havingValue = "java", matchIfMissing = true)
//    public JSqlClient javaSqlClient(ApplicationContext ctx, ApplicationEventPublisher publisher) {
//        return new SpringJSqlClient(ctx, publisher, false);
//    }
//
//    @Bean(name = "sqlClient")
//    @ConditionalOnMissingBean({JSqlClient.class, KSqlClient.class})
//    @ConditionalOnProperty(name = "jimmer.language", havingValue = "kotlin")
//    public KSqlClient kotlinSqlClient(ApplicationContext ctx, ApplicationEventPublisher publisher) {
//        return KSqlClientKt.toKSqlClient(
//                new SpringJSqlClient(ctx, publisher, true)
//        );
//    }
//
//    @Bean
//    public SqlClientInitializer sqlClientInitializer(
//            @Inject List<JSqlClient> javaSqlClients,
//            @Inject List<KSqlClient> kotlinSqlClients
//    ) {
//        return new SqlClientInitializer(javaSqlClients, kotlinSqlClients);
//    }
//
//    @ConditionalOnMissingBean(CacheAbandonedCallback.class)
//    @Bean
//    public CacheAbandonedCallback cacheAbandonedCallback() {
//        return CacheAbandonedCallback.log();
//    }
//}
