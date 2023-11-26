package com.example.demo;

import org.babyfish.jimmer.sql.EnableDtoGeneration;
import org.noear.solon.Solon;
import org.noear.solon.annotation.SolonMain;

@SolonMain
@EnableDtoGeneration
public class App {
    public static void main(String[] args) {

		Solon.start(App.class, args, app -> {
			app.get("/", ctx -> {
				 ctx.forward("/doc.html");
			});
		});
    }
}
