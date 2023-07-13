package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.service.CompensationService;
import com.mindex.challenge.service.EmployeeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CompensationServiceImplTest {

    private String baseUrl;

    private Employee employee1;

    private Employee employee2;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private CompensationService compensationService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        baseUrl = "http://localhost:" + port;
        employee1 = employeeService.read("16a596ae-edd3-4847-99fe-c4518e82c86f");
        employee2 = employeeService.read("b7839309-3348-463b-a7e3-5de1c168beb3");
    }

    @Test
    public void testCompensationCreateAndRead() {

        Compensation testCompensation1 = new Compensation();
        testCompensation1.setSalary(1111);
        testCompensation1.setEffectiveDate(new GregorianCalendar(2023,Calendar.JULY,12).getTime());
        Compensation testCompensation2 = new Compensation();
        testCompensation2.setSalary(3333);
        testCompensation2.setEffectiveDate(new GregorianCalendar(2023,Calendar.JUNE,11).getTime());
        Compensation testCompensation3 = new Compensation();
        testCompensation3.setSalary(2222);
        testCompensation3.setEffectiveDate(new GregorianCalendar(2023,Calendar.JULY,10).getTime());

        // Create compensation objects
        ResponseEntity<Compensation> createResponse = restTemplate
                .postForEntity(url(employee1.getEmployeeId(), null), testCompensation1, Compensation.class);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        Compensation compensation1 = createResponse.getBody();
        assertNotNull(compensation1);
        Compensation compensation2 = restTemplate
                .postForEntity(url(employee2.getEmployeeId(), null), testCompensation2, Compensation.class).getBody();
        assertNotNull(compensation1);
        Compensation compensation3 = restTemplate
                .postForEntity(url(employee2.getEmployeeId(), null), testCompensation3, Compensation.class).getBody();
        assertNotNull(compensation1);

        // Read compensation history for employee 2
        HttpHeaders headers = new HttpHeaders();
        headers.set("accept", "application/json");
        ResponseEntity<List<Compensation>> response = restTemplate.exchange(
                url(employee2.getEmployeeId(), null),
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                new ParameterizedTypeReference<List<Compensation>>() {
                });
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<Compensation> compensationHistory = response.getBody();
        assertNotNull(compensationHistory);
        assertEquals(2, compensationHistory.size());
        assertThat(compensationHistory.get(0))
                .usingRecursiveComparison()
                .isEqualTo(compensation3);
        assertThat(compensationHistory.get(1))
                .usingRecursiveComparison()
                .isEqualTo(compensation2);

        // Read compensation history for employee 1 by ID
        Compensation compensation = restTemplate.getForEntity(url(employee1.getEmployeeId(), compensation1.getCompensationId()), Compensation.class).getBody();
        assertNotNull(compensation);
        assertThat(compensation)
                .usingRecursiveComparison()
                .isEqualTo(compensation1);
    }

    private String url(String employeeId, String compensationId) {
        String url = baseUrl + "/employee";
        if (employeeId != null) url += "/" + employeeId;
        url += "/compensation";
        if (compensationId != null) url += "/" + compensationId;
        return url;
    }
}
