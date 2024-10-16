package com.example.chatting.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
public class MySQLConfig {

    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username")
    private String username;
    @Value("${spring.datasource.password")
    private String password;
    @Value("${spring.datasource.driver-class-name")
    private String driverClassName;

    // 데이터 소스를 활용하여 JDBC를 기반으로 DB 트랜잭션을 관리한다.
    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    // 트랜잭션을 관리할 때 사용하는 유틸 클래스이다.
    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    @Bean(name = "createChatTransactionManager")
    public PlatformTransactionManager createChatTransactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }

    @Bean(name = "createUserTransactionManager")
    public PlatformTransactionManager createUserTransactionManager(DataSource dataSource) {
        DataSourceTransactionManager manager = new DataSourceTransactionManager(dataSource);
        return manager;
    }
}
