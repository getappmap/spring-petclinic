package org.springframework.samples.petclinic.security;

import org.springframework.samples.petclinic.user.User;
import org.springframework.samples.petclinic.user.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JpaUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public JpaUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username)
			.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

		Set<GrantedAuthority> authorities = user.getRoles()
			.stream()
			.map(role -> new SimpleGrantedAuthority(role.getName()))
			.collect(Collectors.toSet());

		return new org.springframework.security.core.userdetails.User(user.username(), user.password(),
				user.isEnabled(), true, true, true, authorities);
	}

}