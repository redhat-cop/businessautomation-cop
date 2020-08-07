package org.redhat.services.model.entity;

import org.redhat.services.model.type.ExecutionStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "EXECUTION_LOG")
//@NamedNativeQueries({@NamedNativeQuery(name = "ExecutionLog.findByCountryCode", query = EXECUTION_LOG_BY_COUNTRY_CODE, resultClass = ExecutionLog.class),
//        @NamedNativeQuery(name = "ExecutionLog.findMaxExecutionDatePerCountryScenario", query = EXECUTION_LOG_MAX_EXECUTION_DATE, resultClass = ExecutionLog.class)})
public class ExecutionLog implements Serializable {

    private static final long serialVersionUID = -2771996276955706645L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "exelog_trig_seq_gen")
    @SequenceGenerator(name = "exelog_trig_seq_gen", sequenceName = "exelog_trig_seq_gen", allocationSize = 1)
    private Long id;

    /**
     * UUID to lookup execution stats
     */
    @Column(name = "EXECUTION_REFERENCE")
    private String executionReference;

    @Column(name = "REQUEST_START", columnDefinition = "TIMESTAMP")
    private LocalDateTime executionStart;

    @Column(name = "REQUEST_STOP", columnDefinition = "TIMESTAMP")
    private LocalDateTime executionStop;

    @Column(name = "RULE_EXECUTION_DURATION", columnDefinition = "TIMESTAMP")
    private LocalDateTime rulesExecutionDuration;

    @Column(name = "PAYLOAD")
    private String payload;

    @Column(name = "EXECUTION_STATUS")
    @Enumerated(EnumType.STRING)
    private ExecutionStatus status;

    @Column(name = "TOTAL_RULES_FIRED")
    private Integer totalRulesFired;

    @Column(name = "RULES_FIRED")
    private String rulesFired;


    // @formatter:off
	@Override
	public String toString() {
		return "ExecutionLog ["
				+ " id=" + id 
				+ ", executionStart=" + executionStart 
				+ ", executionStop=" + executionStop
//				+ ", executionTarget=" + executionTarget
//				+ ", executionTrigger=" + executionTrigger
//				+ ", executionCountryCode=" + executionCountryCode
				+ ", status=" + status 
//				+ ", invocationStatus=" + invocationStatus
				+ "]";
	// @formatter:on
    }

}
