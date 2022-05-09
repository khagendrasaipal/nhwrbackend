package org.saipal.workforce.config;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private JwtRequestFilter jwtRequestFilter;
	
	

	String[] excludeResource = { "/favicon.ico", "/sw.js", "/ng-login", "/error", "/util/**", "/check-login","/sign-in",
			"/relogin", "/en-pass/**", "/api/**", "/auth-login", "/auth", "/ng-auth/**", "/assets/**", "/backup/**",
			"/css/**", "/ext/**", "/fonts/**", "/icon/**", "/icons/**", "/images/**", "/img/**", "/jeasy/**",
			"/jeasy_blue/**", "/jeasyl/**", "/js/**", "/keystore/**", "/menuimage/**", "/ng-auth/**", "/qrcodes/**",
			"/ribbon/**", "/sjs/**", "/apphome/js/dhtmlx/**", "/wrtc-signal","/get-auth-token","/calender/**","/test/**","test/authorize","/test/dashboard","/test/web","/Emblem_of_Nepal_2020.svg","/getDashboard","/registry/downloadcsv" };

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
             
		http.cors().configurationSource(corsConfig).and().csrf().disable().authorizeRequests().antMatchers(excludeResource).permitAll().and().authorizeRequests()
				.anyRequest().authenticated().and().formLogin().loginPage("/login").permitAll().and().logout()
				.logoutRequestMatcher(new AntPathRequestMatcher("/**/logout")).logoutSuccessUrl("/logout-done");

		http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class).sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS);

	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {}
	
	CorsConfigurationSource corsConfig = new CorsConfigurationSource() {
		@Override
		public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
			CorsConfiguration configs = new CorsConfiguration().applyPermitDefaultValues();
			configs.addAllowedMethod(HttpMethod.PUT);
			configs.addAllowedMethod(HttpMethod.DELETE);
			return configs;
		}
    };
	
}