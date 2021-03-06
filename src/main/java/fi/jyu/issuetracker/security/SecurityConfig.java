package fi.jyu.issuetracker.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Spring Security configuration. Introduces needed beans. Enables password encryption, CORS configuration and sets API endpoints
 * which are secured and which are not.
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	
	@Autowired
	CustomUserDetailsService customUserDetailsService;

	@Autowired
	private JwtAuthenticationEntryPoint unauthorizedHandler;

	@Bean
	public JwtAuthenticationFilter jwtAuthenticationFilterBean() {
		return new JwtAuthenticationFilter();
	}
	
	@Override
	public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
		authenticationManagerBuilder.userDetailsService(customUserDetailsService);
	}

	@Bean(BeanIds.AUTHENTICATION_MANAGER)
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	/**
	 * Enable CORS
	 * @return CORS configuration
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowCredentials(true);
		configuration.setAllowedOrigins(Arrays.asList(CorsConfiguration.ALL));
		configuration.setAllowedMethods(Arrays.asList(CorsConfiguration.ALL));
		configuration.setAllowedHeaders(Arrays.asList(CorsConfiguration.ALL));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	/**
	 * Use Bcrypt to encrypt passwords by default.
	 * @return encoder
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
	  return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationProvider daoAuthenticationProvider() {
	  DaoAuthenticationProvider provider = 
		new DaoAuthenticationProvider();
	  provider.setPasswordEncoder(passwordEncoder());
	  provider.setUserDetailsService(this.customUserDetailsService);
	  return provider;
	}

	/**
	 * Allow entry to /api/login and /api/register entrypoints without authentication. Authenticate
	 * all other entrypoints.
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.cors()
		.and().csrf().disable().exceptionHandling().authenticationEntryPoint(unauthorizedHandler)
		.and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
		.and().authorizeRequests()
				.antMatchers("/api/login").permitAll()
				.antMatchers("/api/register").permitAll()
				.antMatchers("/actuator/health").permitAll()
				.anyRequest().authenticated();
		
		http.addFilterBefore(jwtAuthenticationFilterBean(), UsernamePasswordAuthenticationFilter.class);
	}
}

