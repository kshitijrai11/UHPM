package com.ultrahpm.productservice.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    public DataSource primaryDataSource() {
        // Fallback properties for local execution without config server
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:postgresql://localhost:5432/ultrahpm_product");
        ds.setUsername("ultrahpm_user");
        ds.setPassword("ultrahpm_password");
        return ds;
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.replica")
    public DataSource replicaDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:postgresql://localhost:5433/ultrahpm_product");
        ds.setUsername("ultrahpm_user");
        ds.setPassword("ultrahpm_password");
        return ds;
    }

    @Primary
    @Bean
    public DataSource routingDataSource(
            @Qualifier("primaryDataSource") DataSource primaryDataSource,
            @Qualifier("replicaDataSource") DataSource replicaDataSource) {

        AbstractRoutingDataSource routingDataSource = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                boolean isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
                log.debug("Routing DB query. ReadOnly: {}", isReadOnly);
                return isReadOnly ? "REPLICA" : "PRIMARY";
            }
        };

        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put("PRIMARY", primaryDataSource);
        dataSourceMap.put("REPLICA", replicaDataSource);

        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.setDefaultTargetDataSource(primaryDataSource);

        return routingDataSource;
    }
}
