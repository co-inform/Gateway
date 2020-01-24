package eu.coinform.gateway.config;

import eu.coinform.gateway.db.RoleEnum;
import eu.coinform.gateway.db.UserDbAuthenticationProvider;
import eu.coinform.gateway.db.UserDbManager;
import eu.coinform.gateway.jwt.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDbManager userDbManager;
    @Value("${JWT_KEY}")
    private String JWT_SECRET;

    @Override
    protected void configure(AuthenticationManagerBuilder builder) {
        builder.authenticationProvider(new UserDbAuthenticationProvider(userDbManager));
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .antMatcher("/twitter/evaluate")
                    .authorizeRequests()
                    .antMatchers("/twitter/evaluate").hasRole(RoleEnum.USER.toString())
                    .and()
                .antMatcher("/login")
                    .authorizeRequests()
                    .antMatchers("/login").hasRole(RoleEnum.USER.toString())
                    .and()
                    .addFilter(usernamePasswordAuthenticationFilter())
                    .addFilter(jwtAuthenticationFilter());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private UsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter() {
        UsernamePasswordAuthenticationFilter authenticationFilter = new UsernamePasswordAuthenticationFilter();
        authenticationFilter.setUsernameParameter("username");
        authenticationFilter.setPasswordParameter("password");
        return authenticationFilter;
    }

    private JwtAuthenticationFilter jwtAuthenticationFilter() throws Exception{
        return new JwtAuthenticationFilter(authenticationManager(), JWT_SECRET);
    }
}


