//package org.babyfish.jimmer.spring.client;
//
//import org.babyfish.jimmer.client.generator.java.feign.JavaFeignGenerator;
//import org.babyfish.jimmer.client.meta.Metadata;
//import org.babyfish.jimmer.spring.cfg.JimmerProperties;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
//
//@Controller
//public class JavaFeignController {
//
//    private final Metadata metadata;
//
//    private final JimmerProperties properties;
//
//    public JavaFeignController(Metadata metadata, JimmerProperties properties) {
//        this.metadata = metadata;
//        this.properties = properties;
//    }
//
//    @GetMapping("${jimmer.client.java-feign.path}")
//    public ResponseEntity<StreamingResponseBody> download(
//            @RequestParam(name = "apiName", required = false) String apiName,
//            @RequestParam(name = "indent", defaultValue = "0") int indent,
//            @RequestParam(name = "basePackage", required = false) String basePackage,
//            @Value("${spring.application.name:}") String applicationName
//    ) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Content-Type", "application/zip");
//        StreamingResponseBody body = out -> {
//            JimmerProperties.Client.JavaFeign javaFeign = properties.getClient().getJavaFeign();
//            new JavaFeignGenerator(
//                    apiName != null && !apiName.isEmpty() ?
//                            apiName :
//                            !javaFeign.getApiName().isEmpty() ?
//                                    javaFeign.getApiName() :
//                                    !applicationName.isEmpty() ? applicationName : "api",
//                    indent != 0 ? indent : javaFeign.getIndent(),
//                    basePackage != null && !basePackage.isEmpty() ?
//                            basePackage :
//                            javaFeign.getBasePackage()
//            ).generate(metadata, out);
//        };
//        return ResponseEntity.ok().headers(headers).body(body);
//    }
//}
