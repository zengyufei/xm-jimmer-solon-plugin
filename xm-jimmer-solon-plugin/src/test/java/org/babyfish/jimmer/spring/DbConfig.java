package org.babyfish.jimmer.spring;

import com.zaxxer.hikari.HikariDataSource;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.util.ResourceUtil;

import javax.sql.DataSource;
import java.sql.Connection;

@Configuration
public class DbConfig {

	@Bean(value = "db", typed = true) // typed 表示可类型注入 //即默认
	public DataSource db(@Inject("${datasource.db}") HikariDataSource ds) {
		return ds;
	}

}
