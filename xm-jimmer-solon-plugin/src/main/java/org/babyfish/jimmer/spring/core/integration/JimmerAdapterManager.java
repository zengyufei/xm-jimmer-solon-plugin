package org.babyfish.jimmer.spring.core.integration;

import org.babyfish.jimmer.spring.core.JimmerAdapter;
import org.babyfish.jimmer.spring.core.JimmerAdapterFactory;
import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.BeanWrap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 适配管理器
 *
 * @author noear
 * @since 1.1
 */
public class JimmerAdapterManager {

    private final static ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private static List<BeanWrap> dsWraps = new ArrayList<>();

    private static JimmerAdapterFactory adapterFactory = new JimmerAdapterFactoryDefault();

    /**
     * 缓存适配器
     */
    private static Map<String, JimmerAdapter> dbMap = new ConcurrentHashMap<>();

    public static JimmerAdapter getOnly(String name) {
        return dbMap.get(name);
    }

    /**
     * 获取适配器
     */
    public synchronized static JimmerAdapter getClient(AppContext ctx, BeanWrap bw) {
        final String named = bw.name();

        Lock lock;
        JimmerAdapter adapter;
        (lock = readWriteLock.readLock()).lock();
        try {
            adapter = dbMap.get(named);
        } finally {
            lock.unlock();
        }

        if (adapter == null) {
            (lock = readWriteLock.writeLock()).lock();
            try {
                adapter = dbMap.get(named);
                if (adapter == null) {
                    adapter = buildAdapter(ctx, bw);

                    dbMap.put(named, adapter);

                    if (bw.typed()) {
                        dbMap.put("", adapter);
                    }
                    Solon.context().wrapAndPut(JimmerAdapter.class, adapter);
                }
            } finally {
                lock.unlock();
            }
        }

        return adapter;
    }

    /**
     * 注册数据源，并生成适配器
     *
     * @param bw 数据源的BW
     */
    public static void add(BeanWrap bw) {
        dsWraps.add(bw);
    }

    /**
     * 注册数据源，并生成适配器
     *
     * @param bw 数据源的BW
     */
    public static void register(AppContext ctx, BeanWrap bw) {
        getClient(ctx, bw);
    }

    /**
     * 注册数据源，并生成适配器
     */
    public static void register(AppContext ctx) {
        for (BeanWrap dsWrap : dsWraps) {
            getClient(ctx, dsWrap);
        }
    }

    /**
     * 构建适配器
     */
    private static synchronized JimmerAdapter buildAdapter(AppContext ctx, BeanWrap bw) {
        JimmerAdapter adapter;
        if (Utils.isEmpty(bw.name())) {
            adapter = adapterFactory.create(ctx, bw);
        } else {
            adapter = adapterFactory.create(ctx, bw, Solon.cfg().getProp("jimmer." + bw.name()));
        }

        return adapter;
    }

}
