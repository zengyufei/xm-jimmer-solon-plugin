package test;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.babyfish.jimmer.sql.example.App;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.noear.solon.annotation.Condition;
import org.noear.solon.data.cache.CacheService;
import org.noear.solon.test.HttpTester;
import org.noear.solon.test.SolonJUnit4ClassRunner;
import org.noear.solon.test.SolonTest;

import java.io.IOException;

@Slf4j
@Condition(onClass = CacheService.class)
@RunWith(SolonJUnit4ClassRunner.class)
@SolonTest(App.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HelloTest extends HttpTester {


    @Test
    public void hello1() throws IOException {
        final Response resp = path("/author/simpleList").exec("get");
        Assert.assertEquals(resp.code(), 200);
        System.out.println(JSONUtil.toJsonPrettyStr(resp.body().string()));
    }

    @Test
    public void hello2() throws IOException {
        final Response resp = path("/author/list")
                .bodyJson("{\"gender\": 0}")
//                    .bodyJson("{\"firstName\": \"Dan\"}")
                .exec("post");
        Assert.assertEquals(resp.code(), 200);
        System.out.println(JSONUtil.toJsonPrettyStr(resp.body().string()));
    }

    @Test
    public void hello3() throws IOException {
        final Response resp = path("/author/1").exec("get");
        Assert.assertEquals(resp.code(), 200);
        System.out.println(JSONUtil.toJsonPrettyStr(resp.body().string()));
    }

    @Test
    public void hello4() throws IOException {
        int id = RandomUtil.randomInt(5000, 10000);
        {
            final Response resp = path("/author/" + id).exec("get");
            Assert.assertEquals(resp.code(), 200);
            System.out.println(resp.body().string());
        }
        {
            final Response resp = path("/author")
                    .bodyJson("{\"id\": " + id + ", \"gender\": 0, \"firstName\": \"张\", \"lastName\": \"三\"}")
                    .exec("put");
            Assert.assertEquals(resp.code(), 200);
            System.out.println(JSONUtil.toJsonPrettyStr(resp.body().string()));
        }
        {
            final Response resp = path("/author/" + id).exec("get");
            Assert.assertEquals(resp.code(), 200);
            System.out.println(JSONUtil.toJsonPrettyStr(resp.body().string()));
        }
    }

    @Test
    public void hello5() throws IOException {
        int id = 0;
        {
            final Response resp = path("/author/list")
                    .bodyJson("{\"firstName\": \"张\", \"lastName\": \"三\"}")
                    .exec("post");
            Assert.assertEquals(resp.code(), 200);
            final String result = resp.body().string();
            System.out.println(JSONUtil.toJsonPrettyStr(result));

            final JSONArray contents = JSONUtil.parseArray(result);
            final JSONObject jsonObject = contents.jsonIter().iterator().next();
            id = jsonObject.getInt("id");
        }
        {
            final Response resp = path("/author/" + id).exec("delete");
            Assert.assertEquals(resp.code(), 200);
            System.out.println(resp.body().string());
        }
        {
            final Response resp = path("/author/list")
                    .bodyJson("{\"firstName\": \"张\", \"lastName\": \"三\"}")
                    .exec("post");
            Assert.assertEquals(resp.code(), 200);
            final String result = resp.body().string();
            System.out.println(result);
        }
    }
}
