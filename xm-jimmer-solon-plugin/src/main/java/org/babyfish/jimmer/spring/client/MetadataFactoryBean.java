package org.babyfish.jimmer.spring.client;

import kotlin.reflect.KFunction;
import kotlin.reflect.KType;
import org.babyfish.jimmer.client.meta.Metadata;
import org.babyfish.jimmer.client.meta.Operation;
import org.babyfish.jimmer.sql.ast.tuple.Tuple2;
import org.jetbrains.annotations.Nullable;
import org.noear.solon.annotation.*;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.DownloadedFile;
import org.noear.solon.core.handle.MethodType;
import org.noear.solon.core.handle.UploadedFile;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MetadataFactoryBean {

    String DEFAULT_NONE = "\n\t\t\n\t\t\n\uE000\uE001\uE002\n\t\t\t\t\n";

    private static final Set<String> IGNORED_CLASS_NAMES;

    private final AppContext ctx;


    public MetadataFactoryBean(AppContext ctx) {
        this.ctx = ctx;
    }

    public Metadata getMetadata() {
        List<Class<?>> serviceTypes = new ArrayList<>();
        final List<BeanWrap> beanWraps = ctx.beanFind(bw -> bw.clz().isAnnotationPresent(Controller.class));

        for (BeanWrap bean : beanWraps) {
            serviceTypes.add(bean.clz());
        }
        return org.babyfish.jimmer.client.meta.Metadata
                .newBuilder()
                .addServiceTypes(serviceTypes)
                .setOperationParser(
                        new Metadata.OperationParser() {
                            @Override
                            public Tuple2<String, Operation.HttpMethod> http(AnnotatedElement annotatedElement) {
                                if (annotatedElement instanceof Method) {
                                    Mapping mapping = annotatedElement.getAnnotation(Mapping.class);
                                    Get getMapping = annotatedElement.getAnnotation(Get.class);
                                    if (getMapping != null) {
                                        return new Tuple2<>(mapping.value(), Operation.HttpMethod.GET);
                                    }
                                    Post postMapping = annotatedElement.getAnnotation(Post.class);
                                    if (postMapping != null) {
                                        return new Tuple2<>(mapping.value(), Operation.HttpMethod.POST);
                                    }
                                    Put putMapping = annotatedElement.getAnnotation(Put.class);
                                    if (putMapping != null) {
                                        return new Tuple2<>(mapping.value(), Operation.HttpMethod.PUT);
                                    }
                                    Delete deleteMapping = annotatedElement.getAnnotation(Delete.class);
                                    if (deleteMapping != null) {
                                        return new Tuple2<>(mapping.value(), Operation.HttpMethod.DELETE);
                                    }
                                    Patch patchMapping = annotatedElement.getAnnotation(Patch.class);
                                    if (patchMapping != null) {
                                        return new Tuple2<>(mapping.value(), Operation.HttpMethod.PATCH);
                                    }
                                }
                                Mapping requestMapping = annotatedElement.getAnnotation(Mapping.class);
                                if (requestMapping != null) {
                                    final MethodType[] method = requestMapping.method();
                                    final MethodType methodType = method[0];
                                    final String named = methodType.name();
                                    return new Tuple2<>(requestMapping.value(),
                                            Operation.HttpMethod.POST
                                    );
                                }
                                return null;
                            }

                            @Override
                            public String[] getParameterNames(Method method) {
                                Parameter[] parameters = method.getParameters();
                                String[] parameterNames = new String[parameters.length];
                                for (int i = 0; i < parameters.length; i++) {
                                    Parameter param = parameters[i];
                                    if (!param.isNamePresent()) {
                                        return null;
                                    }
                                    parameterNames[i] = param.getName();
                                }
                                return parameterNames;
                            }

                            @Override
                            public KType kotlinType(KFunction<?> function) {
                                return function.getReturnType();
                            }

                            @Override
                            public AnnotatedType javaType(Method method) {
                                return method.getAnnotatedReturnType();
                            }
                        }
                )
                .setParameterParser(
                        new org.babyfish.jimmer.client.meta.Metadata.ParameterParser() {
                            @Nullable
                            @Override
                            public Tuple2<String, Boolean> requestParamNameAndNullable(Parameter javaParameter) {
                                Param requestParam = javaParameter.getAnnotation(Param.class);
                                if (requestParam == null) {
                                    return null;
                                }
                                return new Tuple2<>(
                                        notEmpty(requestParam.value(), requestParam.name()),
                                        !requestParam.required() || !requestParam.defaultValue().equals(DEFAULT_NONE)
                                );
                            }

                            @Nullable
                            @Override
                            public String pathVariableName(Parameter javaParameter) {
                                Path pathVariable = javaParameter.getAnnotation(Path.class);
                                if (pathVariable == null) {
                                    return null;
                                }
                                return notEmpty(pathVariable.value(), pathVariable.name());
                            }

                            @Override
                            public boolean isRequestBody(Parameter javaParameter) {
                                return javaParameter.isAnnotationPresent(Body.class);
                            }

                            @Override
                            public boolean shouldBeIgnored(Parameter javaParameter) {
                                return IGNORED_CLASS_NAMES.contains(javaParameter.getType().getName());
                            }
                        }
                )
                .build();
    }

    private static String text(String[] a, String[] b) {
        for (String value : a) {
            if (!value.isEmpty()) {
                return value;
            }
        }
        for (String path : b) {
            if (!path.isEmpty()) {
                return path;
            }
        }
        return "";
    }

    private static String notEmpty(String a, String b) {
        if (!a.isEmpty()) {
            return a;
        }
        if (!b.isEmpty()) {
            return b;
        }
        return "";
    }

    static {
        Set<String> set = new HashSet<>();
        set.add(Context.class.getName());
        set.add(UploadedFile.class.getName());
        set.add(DownloadedFile.class.getName());
        set.add(Principal.class.getName());
        IGNORED_CLASS_NAMES = set;
    }
}
