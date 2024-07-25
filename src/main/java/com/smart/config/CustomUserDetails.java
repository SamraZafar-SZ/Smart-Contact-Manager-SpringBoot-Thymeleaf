package com.smart.config;
import org.springframework.security.core.*;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.smart.entities.User;

public class CustomUserDetails implements UserDetails {

	private User user;
	
	public CustomUserDetails(User user) {
		super();
		this.user = user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// TODO Auto-generated method stub
		SimpleGrantedAuthority smp=new SimpleGrantedAuthority(user.getRole());
		return List.of(smp);
	}

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return user.getPassword();
	}

	@Override 
	public String getUsername() {
		// TODO Auto-generated method stub
		return user.getEmail();
	}

}
