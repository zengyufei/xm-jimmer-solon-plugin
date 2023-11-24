package org.babyfish.jimmer.sql.example.runtime.interceptor;

import org.babyfish.jimmer.ImmutableObjects;
import org.babyfish.jimmer.sql.DraftInterceptor;
import org.babyfish.jimmer.sql.example.model.common.BaseEntityDraft;
import org.babyfish.jimmer.sql.example.model.common.BaseEntityProps;
import org.noear.solon.annotation.Component;

import java.time.LocalDateTime;

@Component
public class BaseEntityDraftInterceptor implements DraftInterceptor<BaseEntityDraft> { // ❶

    /*
     * In this simple example, `BaseEntity` has only two fields: `createdTime` and `modifiedTime`.
     *
     * In actual projects, you can add more fields, such as `creator` and `modifier`,
     * and you can use the information of the permission system to set them as the current user.
     *
     * Since `DraftInterceptor` itself is a spring object, you can use any business information
     * for draft filling. This is why jimmer uses Spring-managed `DraftInterceptor` instead of
     * simply using ORM to support default value.
     */

    @Override
    public void beforeSave(BaseEntityDraft draft, boolean isNew) { // ❷
        if (!ImmutableObjects.isLoaded(draft, BaseEntityProps.MODIFIED_TIME)) { // ❸
            draft.setModifiedTime(LocalDateTime.now());
        }
        if (isNew && !ImmutableObjects.isLoaded(draft, BaseEntityProps.CREATED_TIME)) { // ❹
            draft.setCreatedTime(LocalDateTime.now());
        }
    }
}

/*----------------Documentation Links----------------
❶ https://babyfish-ct.github.io/jimmer/docs/mutation/draft-interceptor
❷ https://babyfish-ct.github.io/jimmer/docs/object/draft

❸ ❹ https://babyfish-ct.github.io/jimmer/docs/object/tool#isloaded
     https://babyfish-ct.github.io/jimmer/docs/object/dynamic
---------------------------------------------------*/
