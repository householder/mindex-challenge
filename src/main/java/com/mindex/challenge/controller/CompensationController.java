package com.mindex.challenge.controller;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.service.CompensationService;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CompensationController {
    private static final Logger LOG = LoggerFactory.getLogger(CompensationController.class);

    @Autowired
    private CompensationService compensationService;

    @PostMapping("/employee/{employeeId}/compensation")
    public Compensation create(@PathVariable String employeeId, @RequestBody Compensation compensation) {
        LOG.debug("Received compensation create request for employee id [{}]", employeeId);
        compensation.setEmployeeId(employeeId);
        return compensationService.create(compensation);
    }

    @GetMapping("/employee/{employeeId}/compensation")
    public List<Compensation> readAll(@PathVariable String employeeId) {
        LOG.debug("Received read request for compensation history for employee id [{}]", employeeId);

        return compensationService.readAll(employeeId);
    }

    @GetMapping("/employee/{employeeId}/compensation/{id}")
    public Compensation read(@PathVariable String employeeId, @PathVariable String id) {
        LOG.debug("Received read request for compensation for employee id [{}] and compensation id [{}]", employeeId, id);

        return compensationService.read(employeeId, id);
    }
}
