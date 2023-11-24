package org.babyfish.jimmer.spring.core.integration;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.babyfish.jimmer.jackson.ImmutableModule;
import org.babyfish.jimmer.spring.core.JimmerAdapter;
import org.babyfish.jimmer.spring.core.annotation.Db;
import org.babyfish.jimmer.spring.core.interceptor.XmCacheInterceptor;
import org.babyfish.jimmer.spring.core.interceptor.XmCachePutInterceptor;
import org.babyfish.jimmer.spring.core.interceptor.XmCacheRemoveInterceptor;
import org.noear.solon.Solon;
import org.noear.solon.SolonApp;
import org.noear.solon.Utils;
import org.noear.solon.core.*;
import org.noear.solon.core.event.EventBus;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.RenderManager;
import org.noear.solon.core.wrap.MethodWrap;
import org.noear.solon.data.annotation.Cache;
import org.noear.solon.data.annotation.CachePut;
import org.noear.solon.data.annotation.CacheRemove;
import org.noear.solon.data.cache.CacheService;
import org.noear.solon.data.cache.CacheServiceWrapConsumer;
import org.noear.solon.scheduling.annotation.EnableScheduling;
import org.noear.solon.serialization.StringSerializerRender;
import org.noear.solon.serialization.jackson.JacksonActionExecutor;
import org.noear.solon.serialization.jackson.JacksonSerializer;

import javax.sql.DataSource;

import static com.fasterxml.jackson.databind.MapperFeature.PROPAGATE_TRANSIENT_MARKER;
import static com.fasterxml.jackson.databind.MapperFeature.SORT_PROPERTIES_ALPHABETICALLY;

@Slf4j
@EnableScheduling
public class XmJimmerPluginImp implements Plugin {

    String JIMMER_PLUGIN_NAME = "xm-jimmer-solon-plugin";

    @Override
    public void start(AppContext context) throws Throwable {
        final SolonApp app = Solon.app();

        // solon 自带缓存不兼容 jimmer, 需要关闭，代码后面手动处理
        app.enableCaching(false);

        // solon 自带 Jackson 不兼容 jimmer，手动处理一下
        initJackson(app, context);


        // 设置 cacheService 序列化对象方式 jackson
//        Solon.context().getBeanAsync(ObjectMapper.class, bean -> {
//            // 设置缓存序列号模式
//            context.subWrapsOfType(CacheService.class, cache -> {
//                //设置自定义的序列化接口（借了内置的对象）
//                final CacheService raw = cache.raw();
//                if (raw instanceof RedissonCacheService) {
//                    final RedissonCacheService redissonCacheService = (RedissonCacheService) raw;
//                    redissonCacheService.serializer(new JacksonRedissionSerializer(bean));
//                }
//            });
//        });

        // 不在这里初始化， 是因为这个时机无法得到 jimmer 的配置config，只能下沉到 solon 的 bean加载完毕 context.lifecycle(-1
        context.subWrapsOfType(DataSource.class, bw -> {
            log.info("jimmer 获取到数据源...");
            JimmerAdapterManager.add(bw);
        });

        // 真正初始化 jimmer
        context.lifecycle(-1, () -> {
            log.info("jimmer 初始化开始 ...");
            // 此处构造 JimmerAdapter 对象放入 solon context 为 bean
            // 不同于 spring 版本的 jimmer 初始化流程，spring版本是完成一个 sqlClient，将sqlClient交给spring托管即可
            // solon 天生支持多数据源di注入，所以这里初始化一个 JimmerAdapter Bean用来管理多个 sqlClient（多数据源），实际上把JimmerAdapter交给solon托管
            // 之后可以监听 JimmerAdapter Bean的完成状态来完成注入
            JimmerAdapterManager.register(context);
        });

        // 处理 @Db 的类文件
        context.beanBuilderAdd(Db.class, (clz, wrap, anno) -> {
            log.info("jimmer 初始化被@Db注解的类 ...");
            builderAddDo(context, clz, wrap, anno.value());
        });

        // 处理 @Db 的成员属性
        context.beanInjectorAdd(Db.class, (varH, anno) -> {
            log.info("jimmer 初始化@Db注解的成员变量 ...");
            injectorAddDo(context, varH, anno.value());
        });

        Solon.context().getBeanAsync(CacheService.class, cacheService -> {
            log.info("{} 异步订阅 CacheService, 执行 jimmer 动作", JIMMER_PLUGIN_NAME);
            Solon.context().subWrapsOfType(CacheService.class, new CacheServiceWrapConsumer());
        });

        // solon 自带缓存不兼容 jimmer， 在这里重新处理
        log.info("jimmer 初始化缓存 ...");
        context.beanAroundAdd(CachePut.class, new XmCachePutInterceptor(), 110);
        context.beanAroundAdd(CacheRemove.class, new XmCacheRemoveInterceptor(), 110);
        context.beanAroundAdd(Cache.class, new XmCacheInterceptor(), 111);

//		log.info("jimmer 初始化完毕 !! ");

        log.info("{} 包加载完毕!", JIMMER_PLUGIN_NAME);
    }




    private static void getObjectMapper(ObjectMapper objectMapper) {
        objectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTypingAsProperty(objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT, "@type");
        objectMapper.registerModule(new JavaTimeModule());
        // 允许使用未带引号的字段名
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        // 允许使用单引号
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.setDefaultTyping(null);

        // 启用 transient 关键字
        objectMapper.configure(PROPAGATE_TRANSIENT_MARKER, true);
        // 启用排序（即使用 LinkedHashMap）
        objectMapper.configure(SORT_PROPERTIES_ALPHABETICALLY, true);
        // 是否识别不带引号的key
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        // 是否识别单引号的key
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        // 浮点数默认类型（dubbod 转 BigDecimal）
        objectMapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);

        // 反序列化时候遇到不匹配的属性并不抛出异常
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 序列化时候遇到空对象不抛出异常
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 反序列化的时候如果是无效子类型,不抛出异常
        objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        objectMapper.registerModule(new ImmutableModule());
    }

    private static void initJackson(SolonApp app, AppContext context) {
        // 给 body 塞入 arg 参数
        context.beanOnloaded(AppContext -> {
            final ChainManager chainManager = app.chainManager();
            chainManager.removeExecuteHandler(JacksonActionExecutor.class);
            final JacksonActionExecutor jacksonActionExecutor = new JacksonActionExecutor() {
                @Override
                protected Object changeBody(Context ctx, MethodWrap mWrap) throws Exception {
                    final Object o = super.changeBody(ctx, mWrap);
                    if (o instanceof ObjectNode) {
                        final ObjectNode changeBody = (ObjectNode) o;
                        ctx.paramMap().forEach((key, value) -> {
                            if (!changeBody.has(key)) {
                                changeBody.put(key, value);
                            }
                        });
                    }
                    return o;
                }
            };
            final ObjectMapper objectMapper = jacksonActionExecutor.config();
            getObjectMapper(objectMapper);

            // 框架默认 -99
            AppContext.lifecycle(-199, () -> {
                final StringSerializerRender render = new StringSerializerRender(false,
                        new JacksonSerializer(objectMapper));
                RenderManager.mapping("@json", render);
                RenderManager.mapping("@type_json", render);
            });

            // 支持 json 内容类型执行
            AppContext.wrapAndPut(JacksonActionExecutor.class, jacksonActionExecutor);
            EventBus.push(jacksonActionExecutor);
            AppContext.wrapAndPut(ObjectMapper.class, objectMapper);
            EventBus.push(objectMapper);

            chainManager.addExecuteHandler(jacksonActionExecutor);
        });

    }

    private void builderAddDo(AppContext ctx, Class<?> clz, BeanWrap wrap, String annoValue) {
        if (!clz.isInterface()) {
            return;
        }

        if (Utils.isEmpty(annoValue)) {
            // 监听 JimmerAdapter Bean的完成状态
            wrap.context().getWrapAsync(JimmerAdapter.class, (dsBw) -> {
                // 多数据源多次循环
                final JimmerAdapter adapter = dsBw.raw();
                final BeanWrap dsWrap = adapter.getDsWrap();
                // 此处使用默认的数据源
                if (dsWrap.typed()) {
                    create0(ctx, clz, dsWrap);
                }
            });
        } else {
            // 监听 JimmerAdapter Bean的完成状态
            wrap.context().getWrapAsync(JimmerAdapter.class, (dsBw) -> {
                // 多数据源多次循环
                final JimmerAdapter adapter = dsBw.raw();
                final BeanWrap dsWrap = adapter.getDsWrap();
                final String name = dsWrap.name();
                // 此处判断数据源名称与 @Db(name='数据源名') 是否相同
                if (!dsWrap.typed() && StrUtil.equalsIgnoreCase(name, annoValue)) {
                    create0(ctx, clz, dsWrap);
                }
            });
        }
    }

    private void injectorAddDo(AppContext ctx, VarHolder varH, String annoValue) {
        if (Utils.isEmpty(annoValue)) {
            // 监听 JimmerAdapter Bean的完成状态
            varH.context().getWrapAsync(JimmerAdapter.class, (dsBw) -> {
                // 多数据源多次循环
                final JimmerAdapter adapter = dsBw.raw();
                final BeanWrap dsWrap = adapter.getDsWrap();
                // 此处使用默认的数据源
                if (dsWrap.typed()) {
                    inject0(ctx, varH, dsWrap);
                }
            });
        } else {
            // 监听 JimmerAdapter Bean的完成状态
            varH.context().getWrapAsync(JimmerAdapter.class, (dsBw) -> {
                // 多数据源多次循环
                final JimmerAdapter adapter = dsBw.raw();
                final BeanWrap dsWrap = adapter.getDsWrap();
                final String name = dsWrap.name();
                // 此处判断数据源名称与 @Db(name='数据源名') 是否相同
                if (!dsWrap.typed() && StrUtil.equalsIgnoreCase(name, annoValue)) {
                    inject0(ctx, varH, dsWrap);
                }
            });
        }
    }

    private void create0(AppContext ctx, Class<?> clz, BeanWrap dsBw) {
        JimmerAdapter raw = JimmerAdapterManager.getClient(ctx, dsBw);
        // 进入容器，用于 @Inject 注入
        dsBw.context().wrapAndPut(clz, raw.getRepository(clz));
    }

    private void inject0(AppContext ctx, VarHolder varH, BeanWrap dsBw) {
        JimmerAdapter jimmerAdapter = JimmerAdapterManager.getClient(ctx, dsBw);

        if (jimmerAdapter != null) {
            jimmerAdapter.injectTo(varH, dsBw);
        }
    }

    @Override
    public void prestop() throws Throwable {
        Plugin.super.prestop();
    }

    @Override
    public void stop() throws Throwable {
        log.info("{} 插件关闭!", JIMMER_PLUGIN_NAME);
    }

}
