package org.babyfish.jimmer.spring.core;

import org.babyfish.jimmer.spring.cfg.JimmerProperties;
import org.babyfish.jimmer.spring.core.support.SolonConnectionManager;
import org.babyfish.jimmer.spring.core.support.SolonTransientResolverProvider;
import org.babyfish.jimmer.spring.core.support.SolonUserIdGeneratorProvider;
import org.babyfish.jimmer.sql.DraftHandler;
import org.babyfish.jimmer.sql.DraftInterceptor;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.cache.CacheAbandonedCallback;
import org.babyfish.jimmer.sql.cache.CacheFactory;
import org.babyfish.jimmer.sql.cache.CacheOperator;
import org.babyfish.jimmer.sql.di.AbstractJSqlClientWrapper;
import org.babyfish.jimmer.sql.di.AopProxyProvider;
import org.babyfish.jimmer.sql.di.TransientResolverProvider;
import org.babyfish.jimmer.sql.di.UserIdGeneratorProvider;
import org.babyfish.jimmer.sql.dialect.Dialect;
import org.babyfish.jimmer.sql.event.TriggerType;
import org.babyfish.jimmer.sql.event.Triggers;
import org.babyfish.jimmer.sql.filter.Filter;
import org.babyfish.jimmer.sql.kt.cfg.KCustomizer;
import org.babyfish.jimmer.sql.kt.cfg.KCustomizerKt;
import org.babyfish.jimmer.sql.kt.cfg.KInitializer;
import org.babyfish.jimmer.sql.kt.cfg.KInitializerKt;
import org.babyfish.jimmer.sql.kt.filter.KFilter;
import org.babyfish.jimmer.sql.kt.filter.impl.JavaFiltersKt;
import org.babyfish.jimmer.sql.meta.DatabaseNamingStrategy;
import org.babyfish.jimmer.sql.runtime.*;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Props;
import org.noear.solon.core.event.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.stream.Collectors;

public class SolonJSqlClient extends AbstractJSqlClientWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolonJSqlClient.class);

    private final AppContext ctx;
    private final Props dsProps;
    private final JimmerProperties jimmerProperties;

    private final boolean isKotlin;

    public SolonJSqlClient(AppContext ctx, JimmerProperties jimmerProperties, Props dsProps, boolean isKotlin) {
        this.ctx = ctx;
        this.jimmerProperties = jimmerProperties;
        this.dsProps = dsProps;
        this.isKotlin = isKotlin;
    }

    @Override
    protected Builder createBuilder() {
        DataSource dataSource = getOptionalBean(DataSource.class);
        ConnectionManager connectionManager = getOptionalBean(ConnectionManager.class);
        UserIdGeneratorProvider userIdGeneratorProvider = getOptionalBean(UserIdGeneratorProvider.class);
        TransientResolverProvider transientResolverProvider = getOptionalBean(TransientResolverProvider.class);
        AopProxyProvider aopProxyProvider = getOptionalBean(AopProxyProvider.class);
        EntityManager entityManager = getOptionalBean(EntityManager.class);
        DatabaseNamingStrategy databaseNamingStrategy = getOptionalBean(DatabaseNamingStrategy.class);
        Dialect dialect = getOptionalBean(Dialect.class);
        Executor executor = getOptionalBean(Executor.class);
        SqlFormatter sqlFormatter = getOptionalBean(SqlFormatter.class);
        CacheFactory cacheFactory = getOptionalBean(CacheFactory.class);
        CacheOperator cacheOperator = getOptionalBean(CacheOperator.class);
        MicroServiceExchange exchange = getOptionalBean(MicroServiceExchange.class);
        Collection<CacheAbandonedCallback> callbacks = ctx.getBeansOfType(CacheAbandonedCallback.class);
        Collection<ScalarProvider<?, ?>> providers = getObjects(ScalarProvider.class);
        Collection<DraftHandler<?, ?>> handlers = getObjects(DraftHandler.class);
        Collection<DraftInterceptor<?>> interceptors = getObjects(DraftInterceptor.class);

        Builder builder = JSqlClient.newBuilder();
        if (connectionManager != null) {
            builder.setConnectionManager(connectionManager);
        } else if (dataSource != null) {
            builder.setConnectionManager(new SolonConnectionManager(dataSource));
        }
        if (userIdGeneratorProvider != null) {
            builder.setUserIdGeneratorProvider(userIdGeneratorProvider);
        } else {
//            builder.setUserIdGeneratorProvider(new SpringUserIdGeneratorProvider(ctx)); // 不兼容 solon
            builder.setUserIdGeneratorProvider(new SolonUserIdGeneratorProvider(ctx));
        }
        if (transientResolverProvider != null) {
            builder.setTransientResolverProvider(transientResolverProvider);
        } else {
//            builder.setTransientResolverProvider(new SpringTransientResolverProvider(ctx)); // 不兼容 solon
            builder.setTransientResolverProvider(new SolonTransientResolverProvider(ctx));
        }
//        if (aopProxyProvider != null) {
        builder.setAopProxyProvider(aopProxyProvider);
//        } else {
//            builder.setAopProxyProvider(AopUtils::getTargetClass);
//        }
        if (entityManager != null) {
            builder.setEntityManager(entityManager);
        }
        if (databaseNamingStrategy != null) {
            builder.setDatabaseNamingStrategy(databaseNamingStrategy);
        }

        builder.setDialect(dialect != null ? dialect : jimmerProperties.getDialect());
        builder.setTriggerType(jimmerProperties.getTriggerType());
        builder.setDefaultDissociateActionCheckable(jimmerProperties.isDefaultDissociationActionCheckable());
        builder.setIdOnlyTargetCheckingLevel(jimmerProperties.getIdOnlyTargetCheckingLevel());
        builder.setDefaultEnumStrategy(jimmerProperties.getDefaultEnumStrategy());
        builder.setDefaultBatchSize(jimmerProperties.getDefaultBatchSize());
        builder.setDefaultListBatchSize(jimmerProperties.getDefaultListBatchSize());
        builder.setOffsetOptimizingThreshold(jimmerProperties.getOffsetOptimizingThreshold());
        builder.setForeignKeyEnabledByDefault(jimmerProperties.isForeignKeyEnabledByDefault());
        builder.setSaveCommandPessimisticLock(jimmerProperties.isSaveCommandPessimisticLock());
        builder.setExecutorContextPrefixes(jimmerProperties.getExecutorContextPrefixes());
        if (jimmerProperties.isShowSql()) {
            builder.setExecutor(Executor.log(executor));
        } else {
            builder.setExecutor(executor);
        }
        if (sqlFormatter != null) {
            builder.setSqlFormatter(sqlFormatter);
        } else if (jimmerProperties.isPrettySql()) {
            if (jimmerProperties.isInlineSqlVariables()) {
                builder.setSqlFormatter(SqlFormatter.INLINE_PRETTY);
            } else {
                builder.setSqlFormatter(SqlFormatter.PRETTY);
            }
        }
        builder
                .setDatabaseValidationMode(jimmerProperties.getDatabaseValidation().getMode())
                .setDatabaseValidationCatalog(jimmerProperties.getDatabaseValidation().getCatalog())
                .setDatabaseValidationSchema(jimmerProperties.getDatabaseValidation().getSchema())
                .setCacheFactory(cacheFactory)
                .setCacheOperator(cacheOperator)
                .addCacheAbandonedCallbacks(callbacks);

        for (ScalarProvider<?, ?> provider : providers) {
            builder.addScalarProvider(provider);
        }

        builder.addDraftHandlers(handlers);
        builder.addDraftInterceptors(interceptors);
        initializeByLanguage(builder);
        builder.addInitializers(new SpringEventInitializer());

        builder.setMicroServiceName(jimmerProperties.getMicroServiceName());
        if (!jimmerProperties.getMicroServiceName().isEmpty()) {
            builder.setMicroServiceExchange(exchange);
        }

        return builder;
    }

    private void initializeByLanguage(Builder builder) {

        Collection<Filter<?>> javaFilters = getObjects(Filter.class);
        Collection<Customizer> javaCustomizers = getObjects(Customizer.class);
        Collection<Initializer> javaInitializers = getObjects(Initializer.class);
        Collection<KFilter<?>> kotlinFilters = getObjects(KFilter.class);
        Collection<KCustomizer> kotlinCustomizers = getObjects(KCustomizer.class);
        Collection<KInitializer> kotlinInitializers = getObjects(KInitializer.class);

        if (isKotlin) {
            if (!javaFilters.isEmpty()) {
                LOGGER.warn(
                        "Jimmer is working in kotlin mode, but some java filters " +
                                "has been found in spring context, they will be ignored"
                );
            }
            if (!javaCustomizers.isEmpty()) {
                LOGGER.warn(
                        "Jimmer is working in kotlin mode, but some java customizers " +
                                "has been found in spring context, they will be ignored"
                );
            }
            if (!javaInitializers.isEmpty()) {
                LOGGER.warn(
                        "Jimmer is working in kotlin mode, but some java initializers " +
                                "has been found in spring context, they will be ignored"
                );
            }
            builder.addFilters(
                    kotlinFilters
                            .stream()
                            .map(JavaFiltersKt::toJavaFilter)
                            .collect(Collectors.toList())
            );
            builder.addCustomizers(
                    kotlinCustomizers
                            .stream()
                            .map(KCustomizerKt::toJavaCustomizer)
                            .collect(Collectors.toList())
            );
            builder.addInitializers(
                    kotlinInitializers
                            .stream()
                            .map(KInitializerKt::toJavaInitializer)
                            .collect(Collectors.toList())
            );
        } else {
            if (!kotlinFilters.isEmpty()) {
                LOGGER.warn(
                        "Jimmer is working in java mode, but some kotlin filters " +
                                "has been found in spring context, they will be ignored"
                );
            }
            if (!kotlinCustomizers.isEmpty()) {
                LOGGER.warn(
                        "Jimmer is working in java mode, but some kotlin customizers " +
                                "has been found in spring context, they will be ignored"
                );
            }
            if (!kotlinInitializers.isEmpty()) {
                LOGGER.warn(
                        "Jimmer is working in kotlin mode, but some kotlin initializers " +
                                "has been found in spring context, they will be ignored"
                );
            }
            builder.addFilters(javaFilters);
            builder.addCustomizers(javaCustomizers);
            builder.addInitializers(javaInitializers);
        }
    }


    private <T> T getOptionalBean(Class<T> type) {
        return ctx.getBean(type);
    }

    @SuppressWarnings("unchecked")
    private <E> Collection<E> getObjects(Class<?> elementType) {
        return (Collection<E>) ctx.getBeansOfType(elementType);
    }

    private static class SpringEventInitializer implements Initializer {

        @Override
        public void initialize(JSqlClient sqlClient) throws Exception {
            Triggers[] triggersArr = ((JSqlClientImplementor) sqlClient).getTriggerType() == TriggerType.BOTH ?
                    new Triggers[]{sqlClient.getTriggers(), sqlClient.getTriggers(true)} :
                    new Triggers[]{sqlClient.getTriggers()};
            for (Triggers triggers : triggersArr) {
                triggers.addEntityListener(EventBus::push);
                triggers.addAssociationListener(EventBus::push);
            }
        }
    }

    public JimmerProperties getProperties() {
        return jimmerProperties;
    }
}
