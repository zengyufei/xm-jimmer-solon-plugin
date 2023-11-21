package com.example.demo;

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
		try(final Connection connection = ds.getConnection()){
			connection.prepareStatement(ResourceUtil.getResourceAsString("init.sql")).execute();
			connection.prepareStatement(ResourceUtil.getResourceAsString("data.sql")).execute();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return ds;
	}

}
