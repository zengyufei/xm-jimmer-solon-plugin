package org.babyfish.jimmer.spring.client;

import lombok.extern.slf4j.Slf4j;
import org.babyfish.jimmer.client.generator.ts.TypeScriptGenerator;
import org.babyfish.jimmer.client.meta.Metadata;
import org.babyfish.jimmer.spring.cfg.JimmerProperties;
import org.babyfish.jimmer.spring.core.annotation.Db;
import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.Context;
import org.noear.solon.validation.annotation.Valid;

import java.io.IOException;

@Slf4j
@Valid
@Controller
@Mapping
public class TypeScriptController {

    @Db
    private Metadata metadata;

    @Db
    private JimmerProperties properties;


    @Get
//    @Mapping("${jimmer.db.client.ts.path}")
    @Mapping("/my-ts.zip")
    public void download(
            @Param(name = "apiName", required = false) String apiName,
            @Param(name = "indent", defaultValue = "0") int indent,
            @Param(name = "anonymous", required = false) Boolean anonymous
    ) throws IOException {
        final Context context = Context.current();
        context.headerAdd("Content-Type", "application/zip");
        JimmerProperties.Client.TypeScript ts = properties.getClient().getTs();
        new TypeScriptGenerator(
                apiName != null && !apiName.isEmpty() ? apiName : ts.getApiName(),
                indent != 0 ? indent : ts.getIndent(),
                anonymous != null ? anonymous : ts.isAnonymous(),
                properties.getClient().getTs().isMutable()
        ).generate(metadata, context.outputStream());
    }
}
