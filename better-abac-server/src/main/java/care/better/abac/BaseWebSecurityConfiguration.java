package care.better.abac;

import care.better.abac.plugin.listener.PartyRelationAsyncServiceRestController;
import care.better.abac.rest.ExternalSystemResource;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * @author Bostjan Lah
 */
@Configuration
@EnableWebSecurity
public class BaseWebSecurityConfiguration extends WebSecurityConfigurerAdapter {
    // make sure we can use base page!
    @Override
    public void init(WebSecurity web) {
        web.ignoring()
                .antMatchers("/*")
                .mvcMatchers(HttpMethod.GET, ExternalSystemResource.BASE_PATH + "/*")
                .mvcMatchers(HttpMethod.POST, ExternalSystemResource.BASE_PATH + "/*/validate")
                .mvcMatchers(HttpMethod.POST, PartyRelationAsyncServiceRestController.STATIC_PATH + "/**");
    }
}