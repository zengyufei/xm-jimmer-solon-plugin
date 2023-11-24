package org.babyfish.jimmer.sql.example.runtime.filter;

import org.babyfish.jimmer.sql.example.model.common.TenantAwareProps;
import org.babyfish.jimmer.sql.example.runtime.TenantProvider;
import org.babyfish.jimmer.sql.filter.Filter;
import org.babyfish.jimmer.sql.filter.FilterArgs;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Component;

/*
 * see JSqlClient.Builder.addFilters
 *
 * This bean is only be used when cache is NOT used.
 */
@Component
public class TenantFilterForNonCacheMode implements Filter<TenantAwareProps> { // ❶


    @Override
    public void filter(FilterArgs<TenantAwareProps> args) {
        String tenant = Solon.context().getBean(TenantProvider.class).get();
        if (tenant != null) {
            args.where(args.getTable().tenant().eq(tenant));
        }
    }
}

/*----------------Documentation Links----------------
❶ https://babyfish-ct.github.io/jimmer/docs/query/global-filter/user-filter
---------------------------------------------------*/
