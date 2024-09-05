package com.zapcom.booking.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

public class HeaderBasedAuthenticationFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest request,
			jakarta.servlet.http.HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String username = request.getHeader("username");
		String rolesHeader = request.getHeader("roles");
		System.err.println(rolesHeader);

		if (username != null && rolesHeader != null) {
			String cleanedRoles = rolesHeader.replaceAll("[\\[\\]\\s]", "");
			List<GrantedAuthority> authorities = Arrays.stream(cleanedRoles.split(","))
					.map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())).collect(Collectors.toList());
			System.err.println(authorities);
			// Create an Authentication object and set it in the SecurityContext
			Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}

		filterChain.doFilter(request, response);
	}

}
