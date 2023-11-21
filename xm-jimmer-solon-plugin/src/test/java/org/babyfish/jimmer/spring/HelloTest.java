package org.babyfish.jimmer.spring;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.noear.solon.test.HttpTester;
import org.noear.solon.test.SolonJUnit4ClassRunner;
import org.noear.solon.test.SolonTest;

import java.io.IOException;

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

}
