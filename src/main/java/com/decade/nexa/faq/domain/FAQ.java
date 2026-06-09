package com.decade.nexa.faq.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.List;

@Table(name = "faq")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class FAQ {
    @Id
    private Long clusterId;

    @Setter
    private String question;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "TEXT[]")
    private List<String> queries;

    private LocalDate createdAt;

}
