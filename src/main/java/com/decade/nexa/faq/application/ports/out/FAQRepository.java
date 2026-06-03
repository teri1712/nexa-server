package com.decade.nexa.faq.application.ports.out;

import com.decade.nexa.faq.domain.FAQ;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FAQRepository extends CrudRepository<FAQ, String> {
    List<FAQ> findByCreatedAt(LocalDate createdAt);
}
