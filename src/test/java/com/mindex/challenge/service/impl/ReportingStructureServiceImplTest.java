package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReportingStructureServiceImplTest {

    private String employeeUrl;
    private String reportingStructureUrl;

    private Employee employee1;
    private Employee employee2;
    private Employee employee3;

    @Autowired
    private EmployeeService employeeService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        employeeUrl = "http://localhost:" + port + "/employee";
        reportingStructureUrl = "http://localhost:" + port + "/reportingstructure/{employeeId}";
        employee1 = employeeService.read("16a596ae-edd3-4847-99fe-c4518e82c86f");
        employee2 = employeeService.read("b7839309-3348-463b-a7e3-5de1c168beb3");
        employee3 = employeeService.read("03aa1462-ffa9-4978-901b-7c001562cf6f");
    }

    @Test
    public void testNumberOfDirectReports() {
        // Verify employee 1 has 4 direct reports
        ReportingStructure reportingStructure1 = restTemplate
                .getForEntity(reportingStructureUrl, ReportingStructure.class, employee1.getEmployeeId()).getBody();
        assertNotNull(reportingStructure1);
        assertThat(reportingStructure1)
                .usingRecursiveComparison()
                .isEqualTo(new ReportingStructure(employee1,4));

        // Verify employee 1 has 0 direct reports
        ReportingStructure reportingStructure2 = restTemplate
                .getForEntity(reportingStructureUrl, ReportingStructure.class, employee2.getEmployeeId()).getBody();
        assertNotNull(reportingStructure2);
        assertThat(reportingStructure2)
                .usingRecursiveComparison()
                .isEqualTo(new ReportingStructure(employee2,0));

        // Verify employee 3 has 2 direct reports
        ReportingStructure reportingStructure3 = restTemplate
                .getForEntity(reportingStructureUrl, ReportingStructure.class, employee3.getEmployeeId()).getBody();
        assertNotNull(reportingStructure3);
        assertThat(reportingStructure3)
                .usingRecursiveComparison()
                .isEqualTo(new ReportingStructure(employee3,2));
    }
}
