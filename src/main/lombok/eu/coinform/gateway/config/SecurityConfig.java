package eu.coinform.gateway.config;

import eu.coinform.gateway.db.entity.RoleEnum;
import eu.coinform.gateway.db.UserDbAuthenticationFilter;
import eu.coinform.gateway.db.UserDbAuthenticationProvider;
import eu.coinform.gateway.db.UserDbManager;
import eu.coinform.gateway.jwt.JwtAuthenticationFailureHandler;
import eu.coinform.gateway.jwt.JwtAuthenticationFilter;
import eu.coinform.gateway.jwt.JwtAuthenticationProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private UserDbManager userDbManager;
    private final String JWT_SECRET;

    public SecurityConfig(
            UserDbManager userDbManager,
            @Value("${JWT_KEY}") String JWT_SECRET) {
        this.userDbManager = userDbManager;
        this.JWT_SECRET = JWT_SECRET;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder builder) {
        builder
                .authenticationProvider(new UserDbAuthenticationProvider(userDbManager))
                .authenticationProvider(new JwtAuthenticationProvider(userDbManager,JWT_SECRET));
    }

    //todo: make the /module and /external endpoints demand the module authority
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER).and()
                .addFilterBefore(userDbAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests()
                    .antMatchers(HttpMethod.POST, "/login", "/exit","/twitter/evaluate", "/twitter/evaluate/*", "/change-password", "/user/*").hasAnyAuthority(RoleEnum.USER.name(), RoleEnum.ADMIN.name(), RoleEnum.MODULE.name())
                    .antMatchers(HttpMethod.GET, "/login", "/exit","/twitter/evaluate", "/twitter/evaluate/*", "/change-password", "/user/*").hasAnyAuthority(RoleEnum.USER.name(), RoleEnum.ADMIN.name(), RoleEnum.MODULE.name())
                    .antMatchers(HttpMethod.POST, "/module/*", "/external/*").hasAnyAuthority(RoleEnum.MODULE.name())
                .anyRequest().permitAll();
    }

    private UserDbAuthenticationFilter userDbAuthenticationFilter() throws Exception {
        return new UserDbAuthenticationFilter(authenticationManager(), userDbManager);
    }

    private JwtAuthenticationFilter jwtAuthenticationFilter() throws Exception{
        return new JwtAuthenticationFilter(authenticationManager(), new JwtAuthenticationFailureHandler());
    }
}


