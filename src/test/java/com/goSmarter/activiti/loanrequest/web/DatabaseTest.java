package com.goSmarter.activiti.loanrequest.web;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ibatis.SqlMapClientTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.goSmarter.activiti.loanrequest.domain.LoanRequest;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/test-datasource-config.xml"})
public class DatabaseTest {

	@Autowired
	SqlMapClientTemplate ibatisTemplate;

	@Test
	public void testInMemoryDatabase() {
		Integer value = (Integer) ibatisTemplate
				.queryForObject("GoSmarter.loanRequestCount");

		assertEquals(0, value.intValue());

		List<LoanRequest> list = ibatisTemplate.queryForList("GoSmarter.loanRequestList");

		assertEquals(0, list.size());
	}
}
