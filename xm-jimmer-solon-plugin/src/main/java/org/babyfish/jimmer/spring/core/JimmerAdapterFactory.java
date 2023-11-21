package org.babyfish.jimmer.spring.core;

import org.noear.solon.core.AppContext;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.Props;

/**
 * 适配器工厂
 *
 * @author noear
 * @since 1.5
 */
public interface JimmerAdapterFactory {

	JimmerAdapter create(AppContext ctx, BeanWrap dsWrap);

	JimmerAdapter create(AppContext ctx, BeanWrap dsWrap, Props dsProps);

}
