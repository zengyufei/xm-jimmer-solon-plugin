package org.babyfish.jimmer.spring.datasource;

import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DataSources {

    private DataSources() {}

    public static DataSource create(TxCallback callback) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setJdbcUrl("jdbc:h2:~/jimmer_spring_test_db;database_to_upper=true");
        return callback == null ? dataSource : new DataSourceProxy(dataSource, callback);
    }
}
