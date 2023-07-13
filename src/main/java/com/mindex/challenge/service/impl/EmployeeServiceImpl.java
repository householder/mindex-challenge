package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.exceptions.ResourceNotFoundException;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public Employee create(Employee employee) {
        LOG.debug("Creating employee [{}]", employee);
        // This can also be done via annotation but this is fine
        employee.setEmployeeId(UUID.randomUUID().toString());
        // Important validation so that the reporting structure isn't broken, leading to unwelcome behavior for that API
        validateDirectReports(employee);
        employeeRepository.insert(employee);
        return employee;
    }

    @Override
    public Employee read(String id) {
        LOG.debug("Reading employee with id [{}]", id);
        Employee employee = employeeRepository.findByEmployeeId(id);
        if (employee == null) throw new ResourceNotFoundException(id);
        return employee;
    }

    @Override
    public Employee update(Employee employee) {
        LOG.debug("Updating employee [{}]", employee);
        // Important validation so that the reporting structure isn't broken, leading to unwelcome behavior for that API
        validateDirectReports(employee);
        if (!employeeRepository.existsById(employee.getEmployeeId()))
            throw new ResourceNotFoundException(employee.getEmployeeId());
        return employeeRepository.save(employee);
    }

    // Programmatic recursion to get the number of direct reports, but this could lead to many trips to the DB.
    // Research MongoDB support for recursive queries and consider using them -or- consider caching the result of this
    // function for a given employee if performance is too poor
    @Override
    public int numberOfDirectReports(Employee employee) {
        List<String> directReports = employee.getDirectReports();
        if (directReports == null || directReports.isEmpty()) return 0;
        else return directReports.stream()
                .mapToInt(id -> 1 + numberOfDirectReports(read(id)))
                .sum();
    }

    // Require that direct report employee IDs are valid (i.e., the employees already exist)
    private void validateDirectReports(Employee employee) {
        List<String> directReports = employee.getDirectReports();
        if (directReports != null) directReports.forEach( employeeId -> {
            if (!employeeRepository.existsById(employeeId)) throw new ResourceNotFoundException(employeeId);
        });
    }
}
