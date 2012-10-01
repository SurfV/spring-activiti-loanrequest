/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.goSmarter.activiti.loanrequest.web;

import static com.goSmarter.activiti.loanrequest.web.SecurityRequestPostProcessors.user;
import static com.goSmarter.activiti.loanrequest.web.SecurityRequestPostProcessors.userDeatilsService;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;

import java.util.List;

import javax.servlet.http.HttpSession;

import junit.framework.Assert;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.identity.User;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.server.MockMvc;
import org.springframework.test.web.server.MvcResult;
import org.springframework.test.web.server.ResultMatcher;
import org.springframework.test.web.server.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = WebContextLoader.class, value = {
		"classpath:META-INF/spring/applicationContext-activiti.xml",
		"classpath:META-INF/spring/applicationContext-security.xml",
		"classpath:META-INF/spring/applicationContext.xml",
		"classpath:META-INF/spring/test-datasource-config.xml"})
public class LoanRequestControllerTest {

	private static String SEC_CONTEXT_ATTR = HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

	@Autowired
	private FilterChainProxy springSecurityFilterChain;

	@Autowired
	private WebApplicationContext wac;

	@Autowired
	SpringProcessEngineConfiguration processEngineConfiguration;
	
	private MockMvc mockMvc;

	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webApplicationContextSetup(this.wac)
				.addFilters(this.springSecurityFilterChain).build();
		
        ProcessEngine processEngine = processEngineConfiguration.buildProcessEngine();

        IdentityService identityService = processEngine.getIdentityService();
        User user = identityService.newUser("fozzie");
        user.setPassword("fozzie");
        identityService.saveUser(user);
        user = identityService.newUser("kermit");
        user.setPassword("kermit");
		identityService.saveUser(user);

		identityService.saveGroup(identityService.newGroup("accountancy"));
		identityService.saveGroup(identityService.newGroup("management"));

		identityService.createMembership("fozzie", "accountancy");
		identityService.createMembership("kermit", "management");
	}

	@After
	public void tearDown() throws Exception {
        ProcessEngine processEngine = processEngineConfiguration.buildProcessEngine();

        IdentityService identityService = processEngine.getIdentityService();	
		identityService.deleteUser("fozzie");
		identityService.deleteUser("kermit");
		identityService.deleteGroup("accountancy");
		identityService.deleteGroup("management");
	}

	@Test
	public void requiresAuthentication() throws Exception {
		mockMvc.perform(get("/loanrequests")).andExpect(
				redirectedUrl("http://localhost/login"));
	}

	@Test
	public void accessGranted() throws Exception {
		this.mockMvc
				.perform(
						get("/loanrequests/list").with(
								userDeatilsService("fozzie")))
				.andExpect(status().isOk()).andExpect(forwardedUrl("view"));
	}

	@Test
	public void accessDenied() throws Exception {
		this.mockMvc.perform(
				get("/loanrequests/list").with(user("kermit").roles("DENIED")))
				.andExpect(status().isForbidden());
	}

	@Test
	public void userAuthenticates() throws Exception {
		final String username = "fozzie";
		final String password = "fozzie";
		mockMvc.perform(
				post("/resources/j_spring_security_check").param("j_username",
						username).param("j_password", password))
				.andExpect(redirectedUrl("/"))
				.andExpect(new ResultMatcher() {
					public void match(MvcResult mvcResult) throws Exception {
						HttpSession session = mvcResult.getRequest()
								.getSession();
						SecurityContext securityContext = (SecurityContext) session
								.getAttribute(SEC_CONTEXT_ATTR);
						Assert.assertEquals(securityContext.getAuthentication()
								.getName(), username);
					}
				});
	}

	@Test
	public void userAuthenticateFails() throws Exception {
		final String username = "user";
		mockMvc.perform(
				post("/resources/j_spring_security_check").param("j_username",
						username).param("j_password", "invalid"))
				.andExpect(redirectedUrl("/login?login_error=t"))
				.andExpect(new ResultMatcher() {
					public void match(MvcResult mvcResult) throws Exception {
						HttpSession session = mvcResult.getRequest()
								.getSession();
						SecurityContext securityContext = (SecurityContext) session
								.getAttribute(SEC_CONTEXT_ATTR);
						Assert.assertNull(securityContext);
					}
				});
	}

	@Test
	public void testList() throws Exception {
		mockMvc.perform(
				get("/loanrequests/list").with(userDeatilsService("fozzie")))
				.andExpect(status().isOk())
				.andExpect(model().attribute("loanRequests", any(List.class)));
	}

	@Test
	public void testSpecificLoanRequest() throws Exception {
		mockMvc.perform(
				get("/loanrequests/show/1").with(userDeatilsService("fozzie")))
				.andExpect(status().isOk())
				.andExpect(
						model().attribute("loanRequest",
								hasProperty("customerName", equalTo("kermit"))));
	}

	@Test
	public void testInsert() throws Exception {
		mockMvc.perform(
				post("/loanrequests/create").param("id", "3")
						.param("customerName", "krishna").param("amount", "26")
						.with(userDeatilsService("fozzie")))
				.andExpect(status().isOk()).andExpect(redirectedUrl("/list"))
				.andExpect(model().attribute("loanRequests", any(List.class)));
	}

	@Test
	public void testUpdate() throws Exception {
		mockMvc.perform(
				post("/loanrequests/update/2").param("id", "2")
						.param("customerName", "krishna1")
						.param("amount", "27").with(userDeatilsService("fozzie"))).andExpect(status().isOk())
				.andExpect(redirectedUrl("/list?status=ok"))
				.andExpect(model().attribute("status", "ok"));
	}

	@Test
	public void testApprove() throws Exception {
		mockMvc.perform(
				post("/loanrequestsapproval/approve/2").param("id", "3")
						.param("customerName", "krishna").param("amount", "26").with(userDeatilsService("kermit")))
				.andExpect(status().isOk())
				.andExpect(model().attribute("loanRequests", any(List.class)));
	}

	@Test
	public void testApproveForbidden() throws Exception {
		mockMvc.perform(
				post("/loanrequestsapproval/approve/2").with(userDeatilsService("fozzie")))
				.andExpect(status().isForbidden());
	}

	@Test
	public void testClose() throws Exception {
		mockMvc.perform(post("/loanrequestsapproval/close/2").with(userDeatilsService("kermit")))
				.andExpect(status().isOk()).andExpect(redirectedUrl("/list?status=notok"))
				.andExpect(model().attribute("loanRequests", any(List.class)));
	}

	@Test
	public void testCloseForbidden() throws Exception {
		mockMvc.perform(
				post("/loanrequestsapproval/close/2").with(userDeatilsService("fozzie")))
				.andExpect(status().isForbidden());
	}
}
