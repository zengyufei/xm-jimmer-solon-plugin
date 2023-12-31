package org.babyfish.jimmer.spring.core;

import org.babyfish.jimmer.spring.cfg.JimmerProperties;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.VarHolder;

/**
 * 适配器
 * <p>
 * 1.提供 mapperScan 能力 2.生成 factory 的能力
 *
 * @author noear
 * @since 1.5
 */
public interface JimmerAdapter {

	BeanWrap getDsWrap();

	Object sqlClient();

    JimmerProperties getJimmerProperties();

	/**
	 * 获取印映代理
	 */
	<T> T getRepository(Class<T> repositoryClz);

	/**
	 * 注入到
	 */
	void injectTo(VarHolder varH, BeanWrap dsBw);

}
