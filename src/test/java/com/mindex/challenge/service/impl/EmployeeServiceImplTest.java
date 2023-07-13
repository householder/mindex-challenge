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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeServiceImplTest {

    private String employeeUrl;
    private String employeeIdUrl;

    private final HttpHeaders headers = new HttpHeaders();

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        employeeUrl = "http://localhost:" + port + "/employee";
        employeeIdUrl = "http://localhost:" + port + "/employee/{id}";
        headers.setContentType(MediaType.APPLICATION_JSON);
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
    public void testCreateUpdateWithDirectReports() {
        Employee testEmployee1 = new Employee();
        testEmployee1.setFirstName("Employee");
        testEmployee1.setLastName("One");
        ResponseEntity<Employee> createResponse1 = restTemplate
                .postForEntity(employeeUrl, testEmployee1, Employee.class);
        assertEquals(HttpStatus.CREATED, createResponse1.getStatusCode());
        Employee employee1 = createResponse1.getBody();
        assertNotNull(employee1);

        Employee testEmployee2 = new Employee();
        testEmployee2.setFirstName("Employee");
        testEmployee2.setLastName("Two");
        ResponseEntity<Employee> createResponse2 = restTemplate
                .postForEntity(employeeUrl, testEmployee2, Employee.class);
        assertEquals(HttpStatus.CREATED, createResponse2.getStatusCode());
        Employee employee2 = createResponse2.getBody();
        assertNotNull(employee2);

        Employee testEmployee3 = new Employee();
        testEmployee3.setFirstName("Employee");
        testEmployee3.setLastName("Three");

        // check we get a 404 if any direct report employee is missing
        List<String> directReports = new ArrayList<>();
        directReports.add(UUID.randomUUID().toString());
        testEmployee3.setDirectReports(directReports);
        ResponseEntity<Employee> createResponse3 = restTemplate
                .postForEntity(employeeUrl, testEmployee3, Employee.class);
        assertEquals(HttpStatus.NOT_FOUND, createResponse3.getStatusCode());

        // check we get a 201 on create if all direct report employees are found
        directReports.clear();
        directReports.add(employee1.getEmployeeId());
        testEmployee3.setDirectReports(directReports);
        ResponseEntity<Employee> createResponse4 = restTemplate
                .postForEntity(employeeUrl, testEmployee3, Employee.class);
        assertEquals(HttpStatus.CREATED, createResponse4.getStatusCode());
        Employee employee3 = createResponse4.getBody();
        assertNotNull(employee3);
        assertEmployeeEquivalence(testEmployee3, employee3);

        // check we get a 404 on update if changing direct reports to one that is missing
        directReports.clear();
        directReports.add(employee1.getEmployeeId());
        directReports.add(UUID.randomUUID().toString());
        employee3.setDirectReports(directReports);
        ResponseEntity<Employee> updateResponse1 =
                restTemplate.exchange(employeeIdUrl,
                        HttpMethod.PUT,
                        new HttpEntity<Employee>(employee3, headers),
                        Employee.class,
                        employee3.getEmployeeId());
        assertEquals(HttpStatus.NOT_FOUND, updateResponse1.getStatusCode());

        // check we get a 200 on update if changing direct reports to ones that are all found
        directReports.clear();
        directReports.add(employee1.getEmployeeId());
        directReports.add(employee2.getEmployeeId());
        employee3.setDirectReports(directReports);
        ResponseEntity<Employee> updateResponse2 =
                restTemplate.exchange(employeeIdUrl,
                        HttpMethod.PUT,
                        new HttpEntity<Employee>(employee3, headers),
                        Employee.class,
                        employee3.getEmployeeId());
        assertEquals(HttpStatus.OK, updateResponse2.getStatusCode());
        Employee updatedEmployee3 = updateResponse2.getBody();
        assertNotNull(updatedEmployee3);
        assertEmployeeEquivalence(employee3, updatedEmployee3);
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
