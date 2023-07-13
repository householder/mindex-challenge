package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Employee;
import com.mindex.challenge.service.EmployeeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeServiceImplTest {

    private String employeeUrl;
    private String employeeIdUrl;

    @Autowired
    private EmployeeService employeeService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        employeeUrl = "http://localhost:" + port + "/employee";
        employeeIdUrl = "http://localhost:" + port + "/employee/{id}";
    }

    @Test
    public void testCreateReadUpdate() {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");

        // Create checks
        ResponseEntity<Employee> createResponse = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        Employee createdEmployee = createResponse.getBody();
        assertNotNull(createdEmployee);
        assertNotNull(createdEmployee.getEmployeeId());
        assertEmployeeEquivalence(testEmployee, createdEmployee);


        // Read checks
        Employee readEmployee = restTemplate.getForEntity(employeeIdUrl, Employee.class, createdEmployee.getEmployeeId()).getBody();
        assertNotNull(readEmployee);
        assertEquals(createdEmployee.getEmployeeId(), readEmployee.getEmployeeId());
        assertEmployeeEquivalence(createdEmployee, readEmployee);


        // Update checks
        readEmployee.setPosition("Development Manager");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Employee updatedEmployee =
                restTemplate.exchange(employeeIdUrl,
                        HttpMethod.PUT,
                        new HttpEntity<Employee>(readEmployee, headers),
                        Employee.class,
                        readEmployee.getEmployeeId()).getBody();

        assertEmployeeEquivalence(readEmployee, updatedEmployee);
    }

    @Test
    public void testCreateBadRequest() {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("First");
        testEmployee.setLastName("Last");

        // confirm 400 when we call create with employeeId provided
        testEmployee.setEmployeeId(UUID.randomUUID().toString());
        ResponseEntity<Employee> createResponse = restTemplate
                .postForEntity(employeeUrl, testEmployee, Employee.class);
        assertEquals(HttpStatus.BAD_REQUEST, createResponse.getStatusCode());
    }

    @Test
    public void testReadNotFound() {
        ResponseEntity<Employee> readEmployeeResponse = restTemplate.getForEntity(employeeIdUrl, Employee.class, "not a real id");
        assertEquals(HttpStatus.NOT_FOUND, readEmployeeResponse.getStatusCode());
    }

    @Test
    public void testUpdateNotFound() {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("First");
        testEmployee.setLastName("Last");

        // confirm 400 when we call update with employeeId for which no employee exists
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Employee> updateResponse =
                restTemplate.exchange(employeeIdUrl,
                        HttpMethod.PUT,
                        new HttpEntity<Employee>(testEmployee, headers),
                        Employee.class,
                        UUID.randomUUID().toString());
        assertEquals(HttpStatus.NOT_FOUND, updateResponse.getStatusCode());
    }

    @Test
    public void testUpdateBadRequest() {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("First");
        testEmployee.setLastName("Last");

        ResponseEntity<Employee> createResponse = restTemplate
                .postForEntity(employeeUrl, testEmployee, Employee.class);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        Employee employee = createResponse.getBody();
        assertNotNull(employee);

        // confirm 400 when we call update with employeeId provided that doesn't match URI
        String actualEmployeeId = employee.getEmployeeId();
        employee.setEmployeeId(UUID.randomUUID().toString());
        employee.setFirstName("New");
        employee.setDepartment("Department");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Employee> updateResponse =
                restTemplate.exchange(employeeIdUrl,
                        HttpMethod.PUT,
                        new HttpEntity<Employee>(employee, headers),
                        Employee.class,
                        actualEmployeeId);
        assertEquals(HttpStatus.BAD_REQUEST, updateResponse.getStatusCode());
    }

    private static void assertEmployeeEquivalence(Employee expected, Employee actual) {
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getDepartment(), actual.getDepartment());
        assertEquals(expected.getPosition(), actual.getPosition());
    }
}
