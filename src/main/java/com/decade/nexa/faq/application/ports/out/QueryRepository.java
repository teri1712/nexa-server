package com.decade.nexa.faq.application.ports.out;

import com.decade.nexa.faq.domain.UserQuery;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QueryRepository extends CrudRepository<UserQuery, String> {
}
