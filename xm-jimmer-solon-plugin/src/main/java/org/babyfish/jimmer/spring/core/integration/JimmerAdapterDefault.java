package org.babyfish.jimmer.spring.core.integration;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.babyfish.jimmer.spring.cache.impl.TransactionCacheOperatorFlusher;
import org.babyfish.jimmer.spring.cfg.JimmerProperties;
import org.babyfish.jimmer.spring.client.SolonCodeBasedExceptionAdvice;
import org.babyfish.jimmer.spring.core.JimmerAdapter;
import org.babyfish.jimmer.spring.core.Repository;
import org.babyfish.jimmer.spring.core.SolonJSqlClient;
import org.babyfish.jimmer.spring.repository.JRepository;
import org.babyfish.jimmer.spring.repository.support.JimmerRepositoryFactory;
import org.babyfish.jimmer.sql.EnumType;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.cache.CacheOperator;
import org.babyfish.jimmer.sql.cache.TransactionCacheOperator;
import org.babyfish.jimmer.sql.event.TriggerType;
import org.babyfish.jimmer.sql.kt.KSqlClient;
import org.babyfish.jimmer.sql.kt.KSqlClientKt;
import org.babyfish.jimmer.sql.runtime.DatabaseValidationMode;
import org.babyfish.jimmer.sql.runtime.IdOnlyTargetCheckingLevel;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.noear.solon.Solon;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.Props;
import org.noear.solon.core.VarHolder;
import org.noear.solon.core.util.GenericUtil;
import org.noear.solon.core.wrap.ClassWrap;

import javax.sql.DataSource;
import java.util.*;

/**
 * jimmer 适配器默认实现
 *
 * @author zengyufei
 * @since 0.1
 */
@Slf4j
public class JimmerAdapterDefault implements JimmerAdapter {

    protected final BeanWrap dsWrap;

    protected final Props dsProps;

    protected final JimmerRepositoryFactory jimmerRepositoryFactory;

    Map<Class<?>, Object> mapperCached = new HashMap<>();

    private Object sqlClient;
    private JimmerProperties jimmerProperties;

    /**
     * 构建Sql工厂适配器，使用默认的 typeAliases 和 mappers 配置
     */
    protected JimmerAdapterDefault(AppContext ctx, BeanWrap dsWrap) {
        this(ctx, dsWrap, Solon.cfg().getProp("jimmer"));
    }

    /**
     * 构建Sql工厂适配器，使用属性配置
     */
    protected JimmerAdapterDefault(AppContext ctx, BeanWrap dsWrap, Props dsProps) {
        /*
         * 因为jimmerRepositoryFactory类的原因，不的不在此处初始化 sqlClient
         * */
        this.dsWrap = dsWrap;
        if (dsProps == null) {
            this.dsProps = new Props();
        } else {
            this.dsProps = dsProps;
        }

        final JimmerProperties jimmerProperties = initJimmerProperties(ctx, dsWrap);

        if (dsWrap.typed()) {
            // 统一异常处理
            final JimmerProperties.ErrorTranslator errorTranslator = jimmerProperties.getErrorTranslator();
            if (!errorTranslator.isDisabled()) {
                Solon.app().routerInterceptor(0, new SolonCodeBasedExceptionAdvice(errorTranslator));
            }
        }


        final String language = dsProps.get("language", "java");
        SolonJSqlClient solonSqlClient = new SolonJSqlClient(ctx, jimmerProperties, dsProps, !StrUtil.equalsIgnoreCase(language, "java"));

        if (StrUtil.equalsIgnoreCase(language, "java")) {
            Set<TransactionCacheOperator> transactionCacheOperators = new LinkedHashSet<>();
            CacheOperator op = ((JSqlClientImplementor) solonSqlClient).getCacheOperator();
            if (op instanceof TransactionCacheOperator) {
                transactionCacheOperators.add((TransactionCacheOperator) op);
            }
            if (!transactionCacheOperators.isEmpty()) {
                new TransactionCacheOperatorFlusher(new ArrayList<>(transactionCacheOperators));
            }
            this.sqlClient = solonSqlClient;
            jimmerRepositoryFactory = new JimmerRepositoryFactory(solonSqlClient);
        } else {
            final KSqlClient kSqlClient = KSqlClientKt.toKSqlClient(solonSqlClient);
            Set<TransactionCacheOperator> transactionCacheOperators = new LinkedHashSet<>();
            CacheOperator op = ((JSqlClientImplementor) kSqlClient).getCacheOperator();
            if (op instanceof TransactionCacheOperator) {
                transactionCacheOperators.add((TransactionCacheOperator) op);
            }
            if (!transactionCacheOperators.isEmpty()) {
                new TransactionCacheOperatorFlusher(new ArrayList<>(transactionCacheOperators));
            }
            this.sqlClient = kSqlClient;
            jimmerRepositoryFactory = new JimmerRepositoryFactory(kSqlClient);
        }
    }


    private JimmerProperties initJimmerProperties(AppContext context, BeanWrap bw) {
        final String named = bw.name();
        final Props dsProps = Solon.cfg().getProp("jimmer." + named);
        final Map<String, JimmerProperties.Client> clientMap = new HashMap<>();
        final Map<String, String> clientStrMap = dsProps.getMap("clients");
        for (Map.Entry<String, String> entry : clientStrMap.entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue();
            final JimmerProperties.Client client;
            try {
                client = ClassWrap.get(Class.forName(value)).newBy(new Props());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            clientMap.put(key, client);
        }

        DatabaseValidationMode finalDatabaseValidationMode = null;
        final String databaseValidationModeStr = dsProps.get("databaseValidationMode");
        if (StrUtil.isNotBlank(databaseValidationModeStr)) {
            finalDatabaseValidationMode = EnumUtil.fromString(DatabaseValidationMode.class,
                    databaseValidationModeStr.toUpperCase());
        }

        TriggerType finalTriggerType = null;
        final String triggerTypeStr = dsProps.get("triggerType");
        if (StrUtil.isNotBlank(triggerTypeStr)) {
            finalTriggerType = EnumUtil.fromString(TriggerType.class,
                    triggerTypeStr.toUpperCase());
        }

        IdOnlyTargetCheckingLevel finalIdOnlyTargetCheckingLevel = null;
        final String idOnlyTargetCheckingLevelStr = dsProps.get("idOnlyTargetCheckingLevel");
        if (StrUtil.isNotBlank(idOnlyTargetCheckingLevelStr)) {
            finalIdOnlyTargetCheckingLevel = EnumUtil.fromString(IdOnlyTargetCheckingLevel.class,
                    idOnlyTargetCheckingLevelStr.toUpperCase());
        }

        EnumType.Strategy finalEnumTypeStrategy = null;
        final String defaultEnumStrategyStr = dsProps.get("defaultEnumStrategy");
        if (StrUtil.isNotBlank(defaultEnumStrategyStr)) {
            finalEnumTypeStrategy = EnumUtil.fromString(EnumType.Strategy.class,
                    defaultEnumStrategyStr.toUpperCase());
        }

        JimmerProperties.DatabaseValidation databaseValidation = null;
        final String validation = dsProps.get("databaseValidation");
        if (StrUtil.isNotBlank(validation)) {
            databaseValidation = dsProps.getBean("databaseValidation", JimmerProperties.DatabaseValidation.class);
        }
        JimmerProperties jimmerProperties = new JimmerProperties(
                dsProps.get("language"),
                dsProps.get("dialect"),
                dsProps.getBool("showSql", false),
                dsProps.getBool("prettySql", false),
                dsProps.getBool("inlineSqlVariables", false),
                finalDatabaseValidationMode,
                databaseValidation,
                finalTriggerType,
                Convert.toBool(dsProps.get("defaultDissociationActionCheckable")),
                finalIdOnlyTargetCheckingLevel,
                Convert.toInt(dsProps.get("transactionCacheOperatorFixedDelay")),
                finalEnumTypeStrategy,
                Convert.toInt(dsProps.get("defaultBatchSize")),
                Convert.toInt(dsProps.get("defaultListBatchSize")),
                Convert.toInt(dsProps.get("offsetOptimizingThreshold")),
                Convert.toBool(dsProps.get("isForeignKeyEnabledByDefault")),
                dsProps.getBool("saveCommandPessimisticLock", false),
                dsProps.getList("executorContextPrefixes"),
                dsProps.get("microServiceName"),
                dsProps.getBean("errorTranslator", JimmerProperties.ErrorTranslator.class),
                dsProps.getBean("client", JimmerProperties.Client.class),
                clientMap
        );
        return jimmerProperties;
    }

    @Override
    public BeanWrap getDsWrap() {
        return dsWrap;
    }

    protected DataSource getDataSource() {
        return dsWrap.raw();
    }

    @Override
    public Object sqlClient() {
        return this.sqlClient;
    }


    @Override
    public <T> T getRepository(Class<T> repositoryClz) {
        Object repository = mapperCached.get(repositoryClz);

        if (repository == null) {
            synchronized (repositoryClz) {
                repository = mapperCached.get(repositoryClz);
                if (repository == null) {
                    Class<?>[] typeArguments = GenericUtil.resolveTypeArguments(repositoryClz, JRepository.class);
                    if (typeArguments == null) {
                        throw new IllegalArgumentException("The class \"" + this.getClass() + "\" "
                                + "does not explicitly specify the type arguments of \"" + JRepository.class.getName()
                                + "\" so that the entityType must be specified");
                    }
                    repository = jimmerRepositoryFactory.getTargetRepository(repositoryClz, typeArguments[0]);
                    mapperCached.put(repositoryClz, repository);
                }
            }
        }

        return (T) repository;
    }

    @Override
    public void injectTo(VarHolder varH, BeanWrap dsBw) {
        final Class<?> varHolderType = varH.getType();
        if (JSqlClient.class.isAssignableFrom(varHolderType)) {
            final Object sqlClient = this.sqlClient();
            if (sqlClient != null) {
                varH.setValue(sqlClient);
                if (dsBw.typed()) {
                    dsBw.context().wrapAndPut(JSqlClient.class, sqlClient);
                }
            }
        }
        if (Repository.class.isAssignableFrom(varHolderType)) {
            final Object repository = this.getRepository(varHolderType);
            varH.setValue(repository);
            // 进入容器，用于 @Inject 注入
            if (dsBw.typed()) {
                dsBw.context().wrapAndPut(varHolderType, repository);
            }
        }
        if (JimmerProperties.class.isAssignableFrom(varHolderType)) {
            final JimmerProperties jimmerProperties = this.getJimmerProperties();
            if (jimmerProperties != null) {
                varH.setValue(jimmerProperties);
                if (dsBw.typed()) {
                    dsBw.context().wrapAndPut(JimmerProperties.class, jimmerProperties);
                }
            }
        }
    }

    @Override
    public JimmerProperties getJimmerProperties() {
        return jimmerProperties;
    }
}
