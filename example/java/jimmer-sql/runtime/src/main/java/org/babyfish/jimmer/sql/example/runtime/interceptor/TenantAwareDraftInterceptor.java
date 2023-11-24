package org.babyfish.jimmer.sql.example.runtime.interceptor;

import org.babyfish.jimmer.ImmutableObjects;
import org.babyfish.jimmer.sql.DraftInterceptor;
import org.babyfish.jimmer.sql.example.model.common.TenantAwareDraft;
import org.babyfish.jimmer.sql.example.model.common.TenantAwareProps;
import org.babyfish.jimmer.sql.example.runtime.TenantProvider;
import org.jetbrains.annotations.NotNull;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

@Component
public class TenantAwareDraftInterceptor implements DraftInterceptor<TenantAwareDraft> { //

    @Inject("${demo.default-tenant}")
    private String defaultTenant;


    @Override
    public void beforeSave(@NotNull TenantAwareDraft draft, boolean isNew) { // ❷
        if (!ImmutableObjects.isLoaded(draft, TenantAwareProps.TENANT)) { // ❸
            String tenant = Solon.context().getBean(TenantProvider.class).get();
            if (tenant == null) {
                tenant = defaultTenant;
            }
            draft.setTenant(tenant);
        }
    }
}

/*----------------Documentation Links----------------
❶ https://babyfish-ct.github.io/jimmer/docs/mutation/draft-interceptor
❷ https://babyfish-ct.github.io/jimmer/docs/object/draft

❸ https://babyfish-ct.github.io/jimmer/docs/object/tool#isloaded
  https://babyfish-ct.github.io/jimmer/docs/object/dynamic
---------------------------------------------------*/
