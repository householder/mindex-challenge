package com.mindex.challenge.dao;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompensationRepository extends MongoRepository<Compensation, String> {
    // find by employee ID and support passing in a sort for the result set
    @Query("{employeeId :?0}")
    List<Compensation> findByEmployeeId(String employeeId, Sort sort);

    // find by employee ID and compensation ID at the DAO layer rather than validating employee ID higher up
    @Query("{employeeId :?0, compensationId :?1}")
    Compensation findByCompensationId(String employeeId, String id);
}
