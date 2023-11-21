package features;

import cn.hutool.json.JSONUtil;
import com.example.demo.App;
import com.example.demo.user.query.UserQuery;
import lombok.extern.slf4j.Slf4j;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Condition;
import org.noear.solon.data.cache.CacheService;
import org.noear.solon.test.HttpTester;
import org.noear.solon.test.SolonJUnit4ClassRunner;
import org.noear.solon.test.SolonTest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Condition(onClass = CacheService.class)
@RunWith(SolonJUnit4ClassRunner.class)
@SolonTest(App.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HelloTest extends HttpTester {

    @Test
    public void hello() throws IOException, InterruptedException {
        final CacheService cacheService = Solon.context().getBean(CacheService.class);
        final UserQuery userQuery = new UserQuery();
        userQuery.setUserId("1");
        userQuery.setUserName("2");
        userQuery.setCreateTime(LocalDateTime.now());
        userQuery.setBeginCreateTime(LocalDateTime.now());
        userQuery.setEndCreateTime(LocalDateTime.now());
        userQuery.setUpdateTime(LocalDateTime.now());
        userQuery.setBeginUpdateTime(LocalDateTime.now());
        userQuery.setEndUpdateTime(LocalDateTime.now());
        userQuery.setCreateId("3");
        userQuery.setCreateName("4");
        userQuery.setUpdateId("5");
        userQuery.setUpdateName("6");
        userQuery.setApprovalStatus("7");
        userQuery.setApproverId("8");
        userQuery.setApproverName("9");
        userQuery.setApprovalComment("10");
        userQuery.setApprovalTime(LocalDateTime.now());
        userQuery.setBeginApprovalTime(LocalDateTime.now());
        userQuery.setEndApprovalTime(LocalDateTime.now());
        userQuery.setIsImported(0);
        userQuery.setImportTime(LocalDateTime.now());
        userQuery.setBeginImportTime(LocalDateTime.now());
        userQuery.setEndImportTime(LocalDateTime.now());
        userQuery.setIsSystemDefault(0);
        userQuery.setStatus("11");

        cacheService.store("test", userQuery, 60);
        TimeUnit.SECONDS.sleep(5);
        final UserQuery afterUserQuery = cacheService.get("test", UserQuery.class);
        System.out.println(JSONUtil.toJsonPrettyStr(afterUserQuery));

        {
            final String result = path("/user/list?page=1&size=10")
                    .bodyJson("{}")
                    .post();

//            System.out.println(JSONUtil.toJsonPrettyStr(result));
        }
        TimeUnit.SECONDS.sleep(1);
        System.out.println("------------------------ 分割线 -----------------------");
        {
            final String result = path("/user/list?page=1&size=10")
                    .bodyJson("{}")
                    .post();
//            System.out.println(JSONUtil.toJsonPrettyStr(result));
        }
    }

    @Test
    public void hello2() throws IOException, InterruptedException {
        {
            final String result = path("/user/getById")
                    .bodyJson("{\"id\": \"user_10\"}")
                    .post();

//            System.out.println(JSONUtil.toJsonPrettyStr(result));
        }
        TimeUnit.SECONDS.sleep(1);
        System.out.println("------------------------ 分割线 -----------------------");
        {
            final String result = path("/user/getById")
                    .bodyJson("{\"id\": \"user_10\"}")
                    .post();
//            System.out.println(JSONUtil.toJsonPrettyStr(result));
        }
    }

    @Test
    public void hello3() throws IOException, InterruptedException {
        {
            final String result = path("/user/simpleList")
                    .get();

            System.out.println(JSONUtil.toJsonPrettyStr(result));
        }
    }
}
