package com.mindex.challenge.controller;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.exceptions.BadRequestException;
import com.mindex.challenge.service.CompensationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
    Structure the URIs to treat compensation as a child resource of an employee because the current API requirements
    only support accessing the compensation for a given employee.
 */
@RestController
public class CompensationController {
    private static final Logger LOG = LoggerFactory.getLogger(CompensationController.class);

    @Autowired
    private CompensationService compensationService;

    @PostMapping("/employee/{employeeId}/compensation")
    @ResponseStatus(HttpStatus.CREATED)
    public Compensation create(@PathVariable String employeeId, @RequestBody Compensation compensation) {
        LOG.debug("Received compensation create request for employee id [{}]", employeeId);
        // prefer letting the user know that the value provided for a valid field will be ignored
        if (compensation.getEmployeeId() != null && !compensation.getEmployeeId().equals(employeeId))
            throw new BadRequestException("Invalid request; do not include `employeeId` or ensure it matches URI");
        if (compensation.getCompensationId() != null)
            throw new BadRequestException("Invalid request; do not include `compensationId`, it will be generated");
        compensation.setEmployeeId(employeeId);
        return compensationService.create(compensation);
    }

    // Bonus API that supports seeing a full history of compensation changes (most recent first) for a given employee
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
