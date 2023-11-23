package org.babyfish.jimmer.sql.example;

import lombok.extern.slf4j.Slf4j;
import org.noear.solon.Solon;
import org.noear.solon.data.cache.CacheService;

@Slf4j
public class App {

    public static void main(String[] args) {

		Solon.start(App.class, args, app -> {
			// 启用 WebSocket 服务
			app.enableWebSocket(true);

			// 转发日志到 Slf4j 接口
//			LogUtil.globalSet(new LogUtilToSlf4j()); // v1.10.11 后支持

			app.get("/", ctx -> {
				// ctx.forward("/railway-bureau-test/index.html");
				// ctx.redirect("/dict/view/tree");
				// ctx.redirect("/employee_info/view/index");
				ctx.render("主页");
			});

			// 异步订阅方式，根据bean type获取Bean（已存在或产生时，会通知回调；否则，一直不回调）
			Solon.context().getBeanAsync(CacheService.class, bean -> {
				// bean 获取后，可以做些后续处理。。。
				log.info("app 异步订阅 CacheService, 执行初始化缓存动作");
			});

		});
    }
}
