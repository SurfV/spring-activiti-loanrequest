package com.goSmarter.activiti.loanrequest.domain;


public class LoanRequest {

    private String processId;

	private String customerName;

    private Double amount;

	private Integer id;

    public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Integer getId() {
		// TODO Auto-generated method stub
		return this.id;
	}

	public void persist() {
		// TODO Auto-generated method stub
		
	}

	public static LoanRequest findLoanRequest(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	public static float countLoanRequests() {
		// TODO Auto-generated method stub
		return 0;
	}

	public static Object findLoanRequestEntries(int firstResult, int sizeNo) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Object findAllLoanRequests() {
		// TODO Auto-generated method stub
		return null;
	}

	public void merge() {
		// TODO Auto-generated method stub
		
	}

	public void remove() {
		// TODO Auto-generated method stub
		
	}

	public void setId(Integer  id) {
		// TODO Auto-generated method stub
		this.id = id;
	}
}
