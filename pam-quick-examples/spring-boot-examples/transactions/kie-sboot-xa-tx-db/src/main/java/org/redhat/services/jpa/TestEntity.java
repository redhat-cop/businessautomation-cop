package org.redhat.services.jpa;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;

/**
 * Annotation City..
 */
@Entity
@Table(name = "TEST_ENTITY")
@Slf4j
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private long processInstanceId;
    private String description;

}