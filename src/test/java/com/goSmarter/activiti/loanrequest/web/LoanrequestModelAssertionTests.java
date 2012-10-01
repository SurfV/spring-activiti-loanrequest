/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.goSmarter.activiti.loanrequest.web;

import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.server.setup.MockMvcBuilders.standaloneSetup;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.junit.Before;
import org.junit.Test;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.server.MockMvc;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.goSmarter.activiti.loanrequest.domain.LoanRequest;

/**
 * Examples of expectations on the content of the model prepared by the
 * controller.
 * 
 * @author Rossen Stoyanchev
 */
public class LoanrequestModelAssertionTests {

	private MockMvc mockMvc;

	@Before
	public void setup() {

		LoanRequestController controller = new LoanRequestController();

		this.mockMvc = standaloneSetup(controller).defaultRequest(get("/"))
				.alwaysExpect(status().isOk()).build();
	}

	@Test
	public void testList() throws Exception {
		mockMvc.perform(get("/list")).andExpect(
				model().attribute("loanRequests", any(List.class)));
	}

	@Test
	public void testSpecificLoanRequest() throws Exception {
		mockMvc.perform(get("/show/1")).andExpect(
				model().attribute("loanRequest",
						hasProperty("customerName", equalTo("kermit"))));
	}

	@Test
	public void testInsert() throws Exception {
		mockMvc.perform(
				post("/create").param("id", "3")
						.param("customerName", "krishna").param("amount", "26"))
				.andExpect(status().isOk()).andExpect(redirectedUrl("/list"))
				.andExpect(model().attribute("loanRequests", any(List.class)));
	}

	@Test
	public void testUpdate() throws Exception {
		mockMvc.perform(
				post("/update/2").param("id", "2")
						.param("customerName", "krishna1")
						.param("amount", "27")).andExpect(status().isOk())
				.andExpect(redirectedUrl("/list?status=ok"))
				.andExpect(model().attribute("status", "ok"));
	}

	@Test
	public void testApprove() throws Exception {
		mockMvc.perform(
				post("/approve/2").param("id", "3")
						.param("customerName", "krishna").param("amount", "26"))
				.andExpect(status().isOk()).andExpect(redirectedUrl("/list"))
				.andExpect(model().attribute("loanRequests", any(List.class)));
	}

	@Test
	public void testClose() throws Exception {
		mockMvc.perform(post("/close").param("id", "3"))
				.andExpect(status().isOk()).andExpect(redirectedUrl("/list"))
				.andExpect(model().attribute("loanRequests", any(List.class)));
	}

/*	@Controller
	private static class SampleLoanrequestController {

		private final List<LoanRequest> values = new ArrayList<LoanRequest>();

		public SampleLoanrequestController() {
			LoanRequest loanRequest = new LoanRequest();
			loanRequest.setId(1);

			loanRequest.setAmount(25D);
			loanRequest.setCustomerName("kermit");
			loanRequest.setProcessId("1");
			values.add(loanRequest);

			loanRequest = new LoanRequest();
			loanRequest.setId(2);
			loanRequest.setAmount(25D);
			loanRequest.setCustomerName("fozzie");
			loanRequest.setProcessId("2");
			values.add(loanRequest);
		}

		@RequestMapping(value = "list")
		public String list(Model model) {
			model.addAttribute("loanRequests", this.values);
			return "view";
		}

		@RequestMapping(value = "show/{id}", produces = "text/html")
		public String show(@PathVariable("id") Integer id, Model model) {
			for (LoanRequest loanRequest : this.values) {
				if (loanRequest.getId() == id) {
					model.addAttribute("loanRequest", loanRequest);
					break;
				}
			}
			return "view";
		}

		@RequestMapping(value = "create", method = RequestMethod.POST)
		public String create(@Valid LoanRequest loanRequest,
				BindingResult result, Model model) {
			values.add(loanRequest);
			// Initiate the BPM modelling of the process
			return "redirect:/list";
		}

		@RequestMapping(value = "update/{id}", method = RequestMethod.POST)
		public String update(@Valid LoanRequest loanRequest,
				BindingResult result, Model model) {
			boolean isFound = false;
			for (LoanRequest loanRequest1 : this.values) {
				if (loanRequest.getId() == loanRequest1.getId()) {
					loanRequest.setCustomerName(loanRequest1.getCustomerName());
					loanRequest.setAmount(loanRequest1.getAmount());
					model.addAttribute("status", "ok");
					isFound = true;
					break;
				}
			}
			if (!isFound) {
				model.addAttribute("status", "notok");
			}
			// Initiate the BPM modelling of the process
			return "redirect:/list";
		}

		@RequestMapping(value = "approve/{id}", method = RequestMethod.POST)
		public String approve(@Valid LoanRequest loanRequest,
				BindingResult result, Model model) {
			boolean isFound = false;

			// check for the userrole if user role is not admin return false

			if (!isFound) {
				model.addAttribute("status", "notok");
			}
			return "redirect:/list";
		}
	}

	@RequestMapping(value = "close/{id}", method = RequestMethod.POST)
	public String close(@Valid LoanRequest loanRequest, BindingResult result,
			Model model) {
		boolean isFound = false;

		// check if the approval is done only than close it

		if (!isFound) {
			model.addAttribute("status", "notok");
		}
		return "redirect:/list";
	}
*/}
