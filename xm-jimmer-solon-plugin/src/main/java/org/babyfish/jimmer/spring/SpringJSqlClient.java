//package org.babyfish.jimmer.spring;
//
//import org.babyfish.jimmer.spring.cfg.JimmerProperties;
//import org.babyfish.jimmer.spring.cfg.support.SpringConnectionManager;
//import org.babyfish.jimmer.spring.cfg.support.SpringTransientResolverProvider;
//import org.babyfish.jimmer.spring.cfg.support.SpringUserIdGeneratorProvider;
//import org.babyfish.jimmer.sql.DraftHandler;
//import org.babyfish.jimmer.sql.DraftInterceptor;
//import org.babyfish.jimmer.sql.JSqlClient;
//import org.babyfish.jimmer.sql.cache.CacheAbandonedCallback;
//import org.babyfish.jimmer.sql.cache.CacheFactory;
//import org.babyfish.jimmer.sql.cache.CacheOperator;
//import org.babyfish.jimmer.sql.di.AbstractJSqlClientWrapper;
//import org.babyfish.jimmer.sql.di.AopProxyProvider;
//import org.babyfish.jimmer.sql.di.TransientResolverProvider;
//import org.babyfish.jimmer.sql.di.UserIdGeneratorProvider;
//import org.babyfish.jimmer.sql.dialect.Dialect;
//import org.babyfish.jimmer.sql.event.TriggerType;
//import org.babyfish.jimmer.sql.event.Triggers;
//import org.babyfish.jimmer.sql.filter.Filter;
//import org.babyfish.jimmer.sql.kt.cfg.KCustomizer;
//import org.babyfish.jimmer.sql.kt.cfg.KCustomizerKt;
//import org.babyfish.jimmer.sql.kt.cfg.KInitializer;
//import org.babyfish.jimmer.sql.kt.cfg.KInitializerKt;
//import org.babyfish.jimmer.sql.kt.filter.KFilter;
//import org.babyfish.jimmer.sql.kt.filter.impl.JavaFiltersKt;
//import org.babyfish.jimmer.sql.meta.DatabaseNamingStrategy;
//import org.babyfish.jimmer.sql.runtime.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.aop.support.AopUtils;
//import org.springframework.beans.factory.NoSuchBeanDefinitionException;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationEventPublisher;
//
//import javax.sql.DataSource;
//import java.util.Collection;
//import java.util.stream.Collectors;
//
//public class SpringJSqlClient extends AbstractJSqlClientWrapper {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(SpringJSqlClient.class);
//
//    private final ApplicationContext ctx;
//
//    private final ApplicationEventPublisher publisher;
//
//    private final boolean isKotlin;
//
//    public SpringJSqlClient(ApplicationContext ctx, ApplicationEventPublisher publisher, boolean isKotlin) {
//        this.ctx = ctx;
//        this.publisher = publisher;
//        this.isKotlin = isKotlin;
//    }
//
//    @Override
//    protected JSqlClient.Builder createBuilder() {
//
//        JimmerProperties properties = getRequiredBean(JimmerProperties.class);
//        DataSource dataSource = getOptionalBean(DataSource.class);
//        ConnectionManager connectionManager = getOptionalBean(ConnectionManager.class);
//        UserIdGeneratorProvider userIdGeneratorProvider = getOptionalBean(UserIdGeneratorProvider.class);
//        TransientResolverProvider transientResolverProvider = getOptionalBean(TransientResolverProvider.class);
//        AopProxyProvider aopProxyProvider = getOptionalBean(AopProxyProvider.class);
//        EntityManager entityManager = getOptionalBean(EntityManager.class);
//        DatabaseNamingStrategy databaseNamingStrategy = getOptionalBean(DatabaseNamingStrategy.class);
//        Dialect dialect = getOptionalBean(Dialect.class);
//        Executor executor = getOptionalBean(Executor.class);
//        SqlFormatter sqlFormatter = getOptionalBean(SqlFormatter.class);
//        CacheFactory cacheFactory = getOptionalBean(CacheFactory.class);
//        CacheOperator cacheOperator = getOptionalBean(CacheOperator.class);
//        MicroServiceExchange exchange = getOptionalBean(MicroServiceExchange.class);
//        Collection<CacheAbandonedCallback> callbacks = ctx.getBeansOfType(CacheAbandonedCallback.class).values();
//        Collection<ScalarProvider<?, ?>> providers = getObjects(ScalarProvider.class);
//        Collection<DraftHandler<?, ?>> handlers = getObjects(DraftHandler.class);
//        Collection<DraftInterceptor<?>> interceptors = getObjects(DraftInterceptor.class);
//
//        JSqlClient.Builder builder = JSqlClient.newBuilder();
//        if (connectionManager != null) {
//            builder.setConnectionManager(connectionManager);
//        } else if (dataSource != null) {
//            builder.setConnectionManager(new SpringConnectionManager(dataSource));
//        }
//        if (userIdGeneratorProvider != null) {
//            builder.setUserIdGeneratorProvider(userIdGeneratorProvider);
//        } else {
//            builder.setUserIdGeneratorProvider(new SpringUserIdGeneratorProvider(ctx));
//        }
//        if (transientResolverProvider != null) {
//            builder.setTransientResolverProvider(transientResolverProvider);
//        } else {
//            builder.setTransientResolverProvider(new SpringTransientResolverProvider(ctx));
//        }
//        if (aopProxyProvider != null) {
//            builder.setAopProxyProvider(aopProxyProvider);
//        } else {
//            builder.setAopProxyProvider(AopUtils::getTargetClass);
//        }
//        if (entityManager != null) {
//            builder.setEntityManager(entityManager);
//        }
//        if (databaseNamingStrategy != null) {
//            builder.setDatabaseNamingStrategy(databaseNamingStrategy);
//        }
//
//        builder.setDialect(dialect != null ? dialect : properties.getDialect());
//        builder.setTriggerType(properties.getTriggerType());
//        builder.setDefaultDissociateActionCheckable(properties.isDefaultDissociationActionCheckable());
//        builder.setIdOnlyTargetCheckingLevel(properties.getIdOnlyTargetCheckingLevel());
//        builder.setDefaultEnumStrategy(properties.getDefaultEnumStrategy());
//        builder.setDefaultBatchSize(properties.getDefaultBatchSize());
//        builder.setDefaultListBatchSize(properties.getDefaultListBatchSize());
//        builder.setOffsetOptimizingThreshold(properties.getOffsetOptimizingThreshold());
//        builder.setForeignKeyEnabledByDefault(properties.isForeignKeyEnabledByDefault());
//        builder.setSaveCommandPessimisticLock(properties.isSaveCommandPessimisticLock());
//        builder.setExecutorContextPrefixes(properties.getExecutorContextPrefixes());
//        if (properties.isShowSql()) {
//            builder.setExecutor(Executor.log(executor));
//        } else {
//            builder.setExecutor(executor);
//        }
//        if (sqlFormatter != null) {
//            builder.setSqlFormatter(sqlFormatter);
//        } else if (properties.isPrettySql()) {
//            if (properties.isInlineSqlVariables()) {
//                builder.setSqlFormatter(SqlFormatter.INLINE_PRETTY);
//            } else {
//                builder.setSqlFormatter(SqlFormatter.PRETTY);
//            }
//        }
//        builder
//                .setDatabaseValidationMode(properties.getDatabaseValidation().getMode())
//                .setDatabaseValidationCatalog(properties.getDatabaseValidation().getCatalog())
//                .setDatabaseValidationSchema(properties.getDatabaseValidation().getSchema())
//                .setCacheFactory(cacheFactory)
//                .setCacheOperator(cacheOperator)
//                .addCacheAbandonedCallbacks(callbacks);
//
//        for (ScalarProvider<?, ?> provider : providers) {
//            builder.addScalarProvider(provider);
//        }
//
//        builder.addDraftHandlers(handlers);
//        builder.addDraftInterceptors(interceptors);
//        initializeByLanguage(builder);
//        builder.addInitializers(new SpringEventInitializer(publisher));
//
//        builder.setMicroServiceName(properties.getMicroServiceName());
//        if (!properties.getMicroServiceName().isEmpty()) {
//            builder.setMicroServiceExchange(exchange);
//        }
//
//        return builder;
//    }
//
//    private void initializeByLanguage(JSqlClient.Builder builder) {
//
//        Collection<Filter<?>> javaFilters = getObjects(Filter.class);
//        Collection<Customizer> javaCustomizers = getObjects(Customizer.class);
//        Collection<Initializer> javaInitializers = getObjects(Initializer.class);
//        Collection<KFilter<?>> kotlinFilters = getObjects(KFilter.class);
//        Collection<KCustomizer> kotlinCustomizers = getObjects(KCustomizer.class);
//        Collection<KInitializer> kotlinInitializers = getObjects(KInitializer.class);
//
//        if (isKotlin) {
//            if (!javaFilters.isEmpty()) {
//                LOGGER.warn(
//                        "Jimmer is working in kotlin mode, but some java filters " +
//                                "has been found in spring context, they will be ignored"
//                );
//            }
//            if (!javaCustomizers.isEmpty()) {
//                LOGGER.warn(
//                        "Jimmer is working in kotlin mode, but some java customizers " +
//                                "has been found in spring context, they will be ignored"
//                );
//            }
//            if (!javaInitializers.isEmpty()) {
//                LOGGER.warn(
//                        "Jimmer is working in kotlin mode, but some java initializers " +
//                                "has been found in spring context, they will be ignored"
//                );
//            }
//            builder.addFilters(
//                    kotlinFilters
//                            .stream()
//                            .map(JavaFiltersKt::toJavaFilter)
//                            .collect(Collectors.toList())
//            );
//            builder.addCustomizers(
//                    kotlinCustomizers
//                            .stream()
//                            .map(KCustomizerKt::toJavaCustomizer)
//                            .collect(Collectors.toList())
//            );
//            builder.addInitializers(
//                    kotlinInitializers
//                            .stream()
//                            .map(KInitializerKt::toJavaInitializer)
//                            .collect(Collectors.toList())
//            );
//        } else {
//            if (!kotlinFilters.isEmpty()) {
//                LOGGER.warn(
//                        "Jimmer is working in java mode, but some kotlin filters " +
//                                "has been found in spring context, they will be ignored"
//                );
//            }
//            if (!kotlinCustomizers.isEmpty()) {
//                LOGGER.warn(
//                        "Jimmer is working in java mode, but some kotlin customizers " +
//                                "has been found in spring context, they will be ignored"
//                );
//            }
//            if (!kotlinInitializers.isEmpty()) {
//                LOGGER.warn(
//                        "Jimmer is working in kotlin mode, but some kotlin initializers " +
//                                "has been found in spring context, they will be ignored"
//                );
//            }
//            builder.addFilters(javaFilters);
//            builder.addCustomizers(javaCustomizers);
//            builder.addInitializers(javaInitializers);
//        }
//    }
//
//    private <T> T getRequiredBean(Class<T> type) {
//        return ctx.getBean(type);
//    }
//
//    private <T> T getOptionalBean(Class<T> type) {
//        try {
//            return ctx.getBean(type);
//        } catch (NoSuchBeanDefinitionException ex) {
//            return null;
//        }
//    }
//
//    @SuppressWarnings("unchecked")
//    private  <E> Collection<E> getObjects(Class<?> elementType) {
//        return (Collection<E>) ctx.getBeansOfType(elementType).values();
//    }
//
//    private static class SpringEventInitializer implements Initializer {
//
//        private final ApplicationEventPublisher publisher;
//
//        private SpringEventInitializer(ApplicationEventPublisher publisher) {
//            this.publisher = publisher;
//        }
//
//        @Override
//        public void initialize(JSqlClient sqlClient) throws Exception {
//            Triggers[] triggersArr = ((JSqlClientImplementor)sqlClient).getTriggerType() == TriggerType.BOTH ?
//                    new Triggers[] { sqlClient.getTriggers(), sqlClient.getTriggers(true) } :
//                    new Triggers[] { sqlClient.getTriggers() };
//            for (Triggers triggers : triggersArr) {
//                triggers.addEntityListener(publisher::publishEvent);
//                triggers.addAssociationListener(publisher::publishEvent);
//            }
//        }
//    }
//}
