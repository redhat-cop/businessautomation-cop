package org.redhat.services.configuration;

import org.redhat.services.filter.AnonymousAuthFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration("kieServerSecurity")
@EnableWebSecurity(debug = true)
@EnableGlobalMethodSecurity(prePostEnabled = true, jsr250Enabled = true, securedEnabled = true)
public class KieUnsecureWebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //@formatter:off

                http
                    .anonymous().authenticationFilter(new AnonymousAuthFilter()).and() // Override anonymousUser
                    .cors().and().csrf().disable() // Disable CORs & CSRF filters
                    .authorizeRequests()
                    .antMatchers("/*").permitAll()
                    .and().headers().frameOptions().disable(); // Disable X-Frame header for h2-console

        // @formatter:on
    }

}