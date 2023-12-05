package org.babyfish.jimmer.spring;

import cn.hutool.core.io.IoUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.noear.solon.test.HttpTester;
import org.noear.solon.test.HttpUtils;
import org.noear.solon.test.SolonJUnit4ClassRunner;
import org.noear.solon.test.SolonTest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
@RunWith(SolonJUnit4ClassRunner.class)
@SolonTest(App.class)
public class HelloTest extends HttpTester {

    @Test
    public void hello() throws IOException {
            final String result = path(8080, "/error/test")
                    .get();
            System.out.println(JSONUtil.toJsonPrettyStr(result));
    }


    @Test
    public void hello2() throws IOException {
            final String result = path(8080, "/defaultBooks")
                    .get();
            System.out.println(JSONUtil.toJsonPrettyStr(result));
    }

    @Test
    public void hello1() throws IOException {
        final HttpUtils utils = path(8080, "/my-ts.zip");
        final Response response = utils.exec("get");
        IoUtil.copy(response.body().byteStream(), Files.newOutputStream(new File("d:/test.zip").toPath()));
    }
}
