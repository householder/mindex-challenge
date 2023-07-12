package com.mindex.challenge.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.Id;

import java.util.Date;

public class Compensation {
    @Id private String compensationId;

    private String employeeId;

    private int salary;

    @JsonFormat(pattern="yyyy-MM-dd")
    private Date effectiveDate;

    public Compensation() {
    }

    public Compensation(String compensationId, String employeeId, int salary, Date effectiveDate) {
        this.compensationId = compensationId;
        this.employeeId = employeeId;
        this.salary = salary;
        this.effectiveDate = effectiveDate;
    }

    public String getCompensationId() { return compensationId; }

    public void setCompensationId(String compensationId) { this.compensationId = compensationId; }

    public String getEmployeeId() { return employeeId; }

    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public int getSalary() { return salary; }

    public void setSalary(int salary) { this.salary = salary; }

    public Date getEffectiveDate() { return effectiveDate; }

    public void setEffectiveDate(Date effectiveDate) { this.effectiveDate = effectiveDate; }
}
