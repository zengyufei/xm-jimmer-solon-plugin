package features;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.demo.App;
import lombok.extern.slf4j.Slf4j;
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
    public void hello3() throws IOException, InterruptedException {
        {
            final String result = path("/user/simpleList")
                    .bodyJson("{}")
                    .post();

            System.out.println(JSONUtil.toJsonPrettyStr(result));
        }
    }

    @Test
    public void hello4() throws IOException, InterruptedException {
        {
            final String result = path("/user/list")
                    .bodyJson("{\"userName\": \"User 0\"}")
                    .post();

            System.out.println(result);
        }
    }

    @Test
    public void hello5() throws IOException, InterruptedException {
        {
            final String result = path("/user/get")
                    .bodyJson("{\"userId\": \"user_11\"}")
                    .post();

            System.out.println(result);
        }
    }

    @Test
    public void hello6() throws IOException, InterruptedException {
        {

            final String result = path("/user/list")
                    .bodyJson("{\"userName\": \"2222222\"}")
                    .post();


            System.out.println(result);
        }
        {
            final String result = path("/user/add")
                    .bodyJson("{\"userName\": \"2222222\"}")
                    .post();

            System.out.println(result);
        }
        {

            final String result = path("/user/list")
                    .bodyJson("{\"userName\": \"2222222\"}")
                    .post();


            System.out.println(result);
        }
    }

    @Test
    public void hello7() throws IOException, InterruptedException {
        String id = "";
        {
            final String result = path("/user/list")
                    .bodyJson("{\"userName\": \"2222222\"}")
                    .post();


            System.out.println(result);
            final JSONArray contents = JSONUtil.parseObj(result).getJSONArray("content");
            final JSONObject jsonObject = contents.jsonIter().iterator().next();
            id = jsonObject.getStr("userId");
        }
        {
            final String result = path("/user/del")
                    .bodyJson("{\"userId\": \""+id+"\"}")
                    .post();

            System.out.println(result);
        }
        {

            final String result = path("/user/list")
                    .bodyJson("{\"userName\": \"2222222\"}")
                    .post();


            System.out.println(result);
        }
    }
}
