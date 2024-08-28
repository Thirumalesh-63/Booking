package com.zapcom.booking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class Config {



    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
        .csrf().disable() 
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                .requestMatchers("/bookingmanagement/admin/**").hasRole("ADMIN")  // Match /admin/** for ADMIN
                .requestMatchers("/**").hasAnyRole("ADMIN", "CUSTOMER") 
                .anyRequest().authenticated()                                // All other requests must be authenticated
            )
            .addFilterBefore(new HeaderBasedAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}