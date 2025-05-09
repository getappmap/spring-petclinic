package org.springframework.samples.petclinic.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
public class LoginTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void testSuccessfulLoginAsAdmin() throws Exception {
		MockHttpServletRequestBuilder login = post("/login").contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.param("username", "admin")
			.param("password", "password")
			.with(csrf());

		mockMvc.perform(login)
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/"))
			.andExpect(authenticated().withUsername("admin").withRoles("CUSTOMER", "VET", "ADMIN"));
	}

	@Test
	public void testSuccessfulLoginAsUser() throws Exception {
		mockMvc.perform(formLogin().user("user").password("password"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/"))
			.andExpect(authenticated().withUsername("user").withRoles("USER"));
	}

	@Test
	public void testFailedLoginWithInvalidPassword() throws Exception {
		mockMvc.perform(formLogin().user("admin").password("wrongpassword"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/login?error"))
			.andExpect(unauthenticated());
	}

	@Test
	public void testFailedLoginWithInvalidUsername() throws Exception {
		mockMvc.perform(formLogin().user("nonexistent").password("password"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/login?error"))
			.andExpect(unauthenticated());
	}

	@Test
	public void testLogout() throws Exception {
		mockMvc.perform(formLogin().user("admin").password("password")).andExpect(authenticated()).andReturn();

		// Then logout
		mockMvc.perform(SecurityMockMvcRequestBuilders.logout())
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/?logout"))
			.andExpect(unauthenticated());
	}

}