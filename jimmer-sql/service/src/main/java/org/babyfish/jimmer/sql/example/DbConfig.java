package org.babyfish.jimmer.sql.example;

import com.zaxxer.hikari.HikariDataSource;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;

@Configuration
public class DbConfig {

	@Bean(value = "db", typed = true) // typed 表示可类型注入 //即默认
	public DataSource db(@Inject("${datasource.db}") HikariDataSource ds) throws Exception {
		initH2(ds);
		return ds;
	}

	private void initH2(HikariDataSource dataSource) throws Exception {
		try (Connection con = dataSource.getConnection()) {
			InputStream inputStream = DbConfig.class
					.getClassLoader()
					.getResourceAsStream("h2-database.sql");
			if (inputStream == null) {
				throw new RuntimeException("no `h2-database.sql`");
			}
			try (Reader reader = new InputStreamReader(inputStream)) {
				char[] buf = new char[1024];
				StringBuilder builder = new StringBuilder();
				while (true) {
					int len = reader.read(buf);
					if (len == -1) {
						break;
					}
					builder.append(buf, 0, len);
				}
				con.createStatement().execute(builder.toString());
			}
		}
	}
}
