package com.goSmarter.activiti.loanrequest.usertask;

import java.util.List;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("classpath:META-INF/spring/applicationContext-activiti.xml")
public class LoanProcessTest extends SpringActivitiTestCase {

	public void setUp() throws Exception {
		identityService.saveUser(identityService.newUser("fozzie"));
		identityService.saveUser(identityService.newUser("kermit"));

		identityService.saveGroup(identityService.newGroup("accountancy"));
		identityService.saveGroup(identityService.newGroup("management"));

		identityService.createMembership("fozzie", "accountancy");
		identityService.createMembership("kermit", "management");
	}

	public void tearDown() throws Exception {
		identityService.deleteUser("fozzie");
		identityService.deleteUser("kermit");
		identityService.deleteGroup("accountancy");
		identityService.deleteGroup("management");
	}

	@Deployment(resources = { "org/activiti/spring/test/usertask/LoanProcess.bpmn20.xml" })
	public void testProcess() {

		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("loanProcess");

		List<Task> tasks = taskService.createTaskQuery().taskCandidateUser("fozzie").list();
		assertEquals(1, tasks.size());
		Task task = tasks.get(0);
		assertEquals("Submit loan request", task.getName());

		taskService.claim(task.getId(), "fozzie");
		tasks = taskService.createTaskQuery().taskAssignee("fozzie").list();

		assertEquals(1, tasks.size());
		taskService.complete(task.getId());

		tasks = taskService.createTaskQuery().taskCandidateUser("fozzie").list();
		assertEquals(0, tasks.size());
		tasks = taskService.createTaskQuery().taskCandidateUser("kermit").list();
		assertEquals(1, tasks.size());
		assertEquals("Verify loan request", tasks.get(0).getName());
		taskService.complete(tasks.get(0).getId());

		assertProcessEnded(processInstance.getId());
	}

}
