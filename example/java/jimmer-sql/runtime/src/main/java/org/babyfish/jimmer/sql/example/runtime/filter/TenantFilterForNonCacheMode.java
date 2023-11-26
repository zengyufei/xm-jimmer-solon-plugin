package org.babyfish.jimmer.sql.example.runtime.filter;

import org.babyfish.jimmer.sql.example.model.common.TenantAwareProps;
import org.babyfish.jimmer.sql.example.runtime.TenantProvider;
import org.babyfish.jimmer.sql.filter.Filter;
import org.babyfish.jimmer.sql.filter.FilterArgs;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

/*
 * see JSqlClient.Builder.addFilters
 *
 * This bean is only be used when cache is NOT used.
 */
@Component
public class TenantFilterForNonCacheMode implements Filter<TenantAwareProps> { // ❶

    @Inject
    private TenantProvider tenantProvider;

    @Override
    public void filter(FilterArgs<TenantAwareProps> args) {
        String tenant = tenantProvider.get();
        if (tenant != null) {
            args.where(args.getTable().tenant().eq(tenant));
        }
    }
}

/*----------------Documentation Links----------------
❶ https://babyfish-ct.github.io/jimmer/docs/query/global-filter/user-filter
---------------------------------------------------*/
