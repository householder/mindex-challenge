package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.CompensationRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.service.CompensationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class CompensationServiceImpl implements CompensationService {

    private static final Logger LOG = LoggerFactory.getLogger(CompensationServiceImpl.class);

    @Autowired
    private CompensationRepository compensationRepository;

    @Override
    public Compensation create(Compensation compensation) {
        LOG.debug("Creating compensation [{}]", compensation);

        compensation.setCompensationId(UUID.randomUUID().toString());
        compensationRepository.insert(compensation);

        return compensation;
    }

    @Override
    public List<Compensation> readAll(String employeeId) {
        LOG.debug("Reading compensation history for employee id [{}]", employeeId);

        Sort sort = Sort.by(Sort.Direction.DESC, "effectiveDate");

        List<Compensation> compensationHistory = compensationRepository.findByEmployeeId(employeeId, sort);

        if (compensationHistory == null) {
            throw new RuntimeException("Invalid employeeId: " + employeeId);
        }

        return compensationHistory;
    }

    @Override
    public Compensation read(String employeeId, String id) {
        LOG.debug("Reading compensation with id [{}] for employee id [{}]", id, employeeId);

        Compensation compensation = compensationRepository.findByCompensationId(id);

        if (compensation == null || !Objects.equals(compensation.getEmployeeId(), employeeId)) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        return compensation;
    }
}
