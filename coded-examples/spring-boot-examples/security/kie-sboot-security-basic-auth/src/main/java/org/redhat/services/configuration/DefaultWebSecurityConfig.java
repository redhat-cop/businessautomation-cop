package org.redhat.services.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration("kieServerSecurity")
@EnableWebSecurity
public class DefaultWebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //@formatter:off

//        http // Authroise any authenticated request
//            .cors().and().csrf().disable()
//            .authorizeRequests()
//                .anyRequest().authenticated()
//            .and().httpBasic()
//            .and().headers().frameOptions().disable();

        http // Authroise any authenticated request
            .cors().and().csrf().disable()
            .authorizeRequests()
               // .antMatchers("/**").hasRole("random")
                //.antMatchers("/**").hasAnyRole("kie-server", "random")
                .antMatchers("/**").access("hasRole('kie-server') and hasRole('random')")
                .anyRequest().authenticated()
            .and().httpBasic()
            .and().headers().frameOptions().disable();

//        http // Authorise request only from specific group i.e. Swagger is unauthenticated
//        .cors().and().csrf().disable()
//        .authorizeRequests().anyRequest().authenticated()
//            .regexMatchers(".*swagger.json", ".*swagger-ui.js", ".*/css/.*css", ".*/lib/.*js", ".*/images/.*png").permitAll()
//            .antMatchers("/**/server/readycheck", "/rest/api-docs/*").permitAll()
//            .antMatchers("/rest/server/**").hasRole("random")
//        .and().httpBasic()
//        .and().headers().frameOptions().disable();

        // TODO : Explore openIdConnect & oauth logins

        // @formatter:on
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser("user").password("user").roles("kie-server");
        auth.inMemoryAuthentication().withUser("test_user").password("password").roles("random");
        auth.inMemoryAuthentication().withUser("wbadmin").password("wbadmin").roles("admin");
        auth.inMemoryAuthentication().withUser("kieserver").password("kieserver1!").roles("kie-server");
        auth.inMemoryAuthentication().withUser("kris").password("password1!").roles("kie-server", "admin", "rest-all");
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}
