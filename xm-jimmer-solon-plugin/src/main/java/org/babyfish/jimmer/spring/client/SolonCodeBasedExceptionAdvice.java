package org.babyfish.jimmer.spring.client;

import org.babyfish.jimmer.error.CodeBasedException;
import org.babyfish.jimmer.impl.util.StringUtil;
import org.babyfish.jimmer.spring.cfg.JimmerProperties;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Handler;
import org.noear.solon.core.route.RouterInterceptor;
import org.noear.solon.core.route.RouterInterceptorChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SolonCodeBasedExceptionAdvice implements RouterInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolonCodeBasedExceptionAdvice.class);

    protected final JimmerProperties.ErrorTranslator errorTranslator;

    public SolonCodeBasedExceptionAdvice(JimmerProperties.ErrorTranslator errorTranslator) {
        this.errorTranslator = errorTranslator;
        if (errorTranslator.isDebugInfoSupported()) {
            notice();
        }
    }

	@Override
	public void doIntercept(Context ctx, Handler mainHandler, RouterInterceptorChain chain) throws Throwable {
        try {
			chain.doIntercept(ctx, mainHandler);
		}
		catch (CodeBasedException ex) {
            ctx.status(errorTranslator.getHttpStatus());
			ctx.render(resultMap(ex));
		}

    }

    protected void notice() {
        String builder = "\n" + "------------------------------------------------\n" +
                "|                                              |\n" +
                "|`jimmer.error-translator.debug-info-supported`|\n" +
                "|has been turned on, this is dangerous, please |\n" +
                "|make sure the current environment is          |\n" +
                "|NOT PRODUCTION!                               |\n" +
                "|                                              |\n" +
                "------------------------------------------------\n";
        LOGGER.info(builder);
    }

    protected Map<String, Object> resultMap(CodeBasedException ex) {
        Map<String, Object> resultMap = new LinkedHashMap<>();
        resultMap.put(
                "family",
                StringUtil.snake(ex.getCode().getDeclaringClass().getSimpleName(), StringUtil.SnakeCase.UPPER)
        );
        resultMap.put(
                "code",
                ex.getCode().name()
        );
        resultMap.putAll(ex.getFields());
        if (errorTranslator.isDebugInfoSupported()) {
            resultMap.put("debugInfo", debugInfoMap(ex));
        }
        return resultMap;
    }

    protected Map<String, Object> debugInfoMap(Throwable ex) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("message", ex.getMessage());
        StackTraceElement[] elements = ex.getStackTrace();
        int size = Math.min(elements.length, errorTranslator.getDebugInfoMaxStackTraceCount());
        List<String> stackFrames = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            stackFrames.add(elements[i].toString());
        }
        map.put("stackFrames", stackFrames);
        if (ex.getCause() != null) {
            map.put("causeBy", debugInfoMap(ex.getCause()));
        }
        return map;
    }
}
