package com.localtest.s3imdbtitleuploader.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "com.localtest.s3imdbtitleuploader.data.repository" })
public class DataSourceConfiguration {

    @Bean(name = "dataSource")
    @ConfigurationProperties("spring.datasource")
    public DataSource highLightDataSource() {
        return DataSourceBuilder.create().build();
    }

}
