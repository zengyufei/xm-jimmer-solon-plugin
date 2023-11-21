package org.babyfish.jimmer.spring.core.integration;

import org.babyfish.jimmer.spring.core.JimmerAdapter;
import org.babyfish.jimmer.spring.core.JimmerAdapterFactory;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.Props;

/**
 * Jimmer 适配器工厂默认实现
 *
 * @author noear
 * @since 1.5
 */
public class JimmerAdapterFactoryDefault implements JimmerAdapterFactory {

	@Override
	public JimmerAdapter create(AppContext ctx, BeanWrap dsWrap) {
		return new JimmerAdapterDefault(ctx, dsWrap);
	}

	@Override
	public JimmerAdapter create(AppContext ctx, BeanWrap dsWrap, Props dsProps) {
		return new JimmerAdapterDefault(ctx, dsWrap, dsProps);
	}

}
