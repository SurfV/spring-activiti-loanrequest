package com.goSmarter.activiti.loanrequest.web;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ibatis.SqlMapClientTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.goSmarter.activiti.loanrequest.domain.LoanRequest;

@Controller
public class LoanRequestController {

	@Autowired
	SqlMapClientTemplate ibatisTemplate;

	@Autowired
	SpringProcessEngineConfiguration processEngineConfiguration;

	private final List<LoanRequest> values = new ArrayList<LoanRequest>();

	private static final String submitterRole = "accountancy";
    private static final String approverRole = "management";
    private static final String submitTaskName = "Submit loan request";
    private static final String approveTaskName = "Verify loan request";

    private static Log logger = LogFactory.getLog(LoanRequestController.class);

    public LoanRequestController() {
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

	@RequestMapping(value = "loanrequests/list")
	public String list(Model model) {
		assertNotNull(ibatisTemplate);

		model.addAttribute("loanRequests", this.values);
		return "view";
	}

	@RequestMapping(value = "loanrequests/show/{id}", produces = "text/html")
	public String show(@PathVariable("id") Integer id, Model model) {
		for (LoanRequest loanRequest : this.values) {
			if (loanRequest.getId() == id) {
				model.addAttribute("loanRequest", loanRequest);
				break;
			}
		}
		return "view";
	}

	@RequestMapping(value = "loanrequests/create", method = RequestMethod.POST)
	public String create(@Valid LoanRequest loanRequest, BindingResult result,
			Model model, HttpServletRequest httpServletRequest) {

		// Initiate the BPM modelling of the process
		String processId = startProcess(loanRequest, httpServletRequest);
		loanRequest.setProcessId(processId);
		ibatisTemplate.insert("GoSmater.loanRequestInsert", loanRequest);
		return "redirect:/list";
	}

	@RequestMapping(value = "loanrequests/update/{id}", method = RequestMethod.POST)
	public String update(@Valid LoanRequest loanRequest, BindingResult result,
			Model model) {
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

	@RequestMapping(value = "loanrequestsapproval/approve/{id}", method = RequestMethod.POST)
	public String approve(@Valid LoanRequest loanRequest, BindingResult result,
			Model model, HttpServletRequest httpServletRequest) {
		boolean isFound = false;

		approveProcess(loanRequest, httpServletRequest);
		// check for the userrole if user role is not admin return false

		if (!isFound) {
			model.addAttribute("status", "notok");
		}
		return "redirect:/list";
	}

	@RequestMapping(value = "loanrequestsapproval/close/{id}", method = RequestMethod.POST)
	public String close(@Valid LoanRequest loanRequest, BindingResult result,
			Model model) {
		boolean isFound = false;

		// check if the approval is done only than close it
		claimAndComplete(loanRequest.getProcessId());
		if (!isFound) {
			model.addAttribute("status", "notok");
		}
		return "redirect:/list";
	}

	private String startProcess(LoanRequest loanRequest,
			HttpServletRequest request) {
		if (request.isUserInRole(submitterRole)) {
			logger.debug("in the startProcess ");
			// Get Activiti services
			// Create Activiti process engine
			ProcessInstance processInstance = getRuntimeService()
					.startProcessInstanceByKey("loanProcess");
			logger.debug("startProcess processInstance Id="
					+ processInstance.getId());

			claimAndComplete(processInstance.getId());
		}
		return "";
	}

	private ProcessEngine processEngine = null;
	private RuntimeService getRuntimeService() {
		if(processEngine == null){
			processEngineConfiguration.buildProcessEngine();
		}
		return processEngine.getRuntimeService();
	}

	private TaskService getTaskService() {
		if(processEngine == null){
			processEngineConfiguration.buildProcessEngine();
		}
		return processEngine.getTaskService();
	}

	private void approveProcess(LoanRequest loanRequest,
			HttpServletRequest request) {
		// TODO Auto-generated method stub

		logger.debug("in the approveProcess ");
		if (!loanRequest.getProcessId().isEmpty()
				&& request.isUserInRole(approverRole)) {
			claimAndComplete(loanRequest.getProcessId());
		}
	}

	private void claimAndComplete(String processInstanceId) {
		User user = (User) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal();

		List<Task> tasks = getTaskService().createTaskQuery()
				.taskCandidateUser(user.getUsername()).list();
		logger.debug("startProcess task size=" + tasks.size());
		Task task = getTask(tasks, processInstanceId);
		if (task != null) {
			logger.debug("got the task=" + tasks.size());
			getTaskService().claim(task.getId(), user.getUsername());
			getTaskService().complete(task.getId());
		}
	}

	private Task getTask(List<Task> tasks, String processInstanceId) {
		for (Task task : tasks) {
			if (task.getProcessInstanceId().equals(processInstanceId)) {
				return task;
			}
		}
		return null;
	}
}
