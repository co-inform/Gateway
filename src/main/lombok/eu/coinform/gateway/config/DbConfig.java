package eu.coinform.gateway.config;

import eu.coinform.gateway.db.PasswordAuthRepository;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(basePackages = "eu.coinform.gateway.db.entity")
public class DbConfig {

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DataSource getDataSource(
            @Value("${gateway_db.driverClassName}") String driverClassName,
            @Value("${gateway_db.url}") String url,
            @Value("${gateway_db.username}") String username,
            @Value("${GATEWAY_DB_PASSWORD}") String password,
            @Value("${gateway_db.testOnBorrow}") String testOnBorrow,
            @Value("${gateway_db.testWhileIdle}") String testWhileIdle,
            @Value("${gateway_db.timeBetweenEvictionRunsMillis}") String timeBetweenEvictionRunsMillis,
            @Value("${gateway_db.minEvictableIdleTimeMillis}") String minEvictableIdleTimeMillis,
            @Value("${gateway_db.validationQuery}") String validationQuery,
            @Value("${gateway_db.max-active}") String maxActive,
            @Value("${gateway_db.max-idle}") String maxIdle,
            @Value("${gateway_db.max-wait}") String maxWait) {

        return DataSourceBuilder.create()
                .driverClassName(driverClassName)
                .url(url)
                .username(username)
                .password(password)
                .build();
    }

    /*
    @Bean
    public Flyway getFlyway(DataSource dataSource) {
        Flyway flyway = Flyway.configure().dataSource(dataSource).load();
        flyway.migrate();
        return flyway;
    }

     */

}
