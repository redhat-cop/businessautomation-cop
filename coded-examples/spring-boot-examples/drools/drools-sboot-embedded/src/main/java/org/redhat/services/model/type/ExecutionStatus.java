package org.redhat.services.model.type;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

/**
 * Back the ExecutionLog.executionTarget property
 * 
 * @author ksbk859
 *
 */
public enum ExecutionStatus {

	// @formatter:off
	SUCCESS,
	FAILURE,
	IN_PROGRESS,
	ERROR;
	// @formatter:on

	private static Map<String, ExecutionStatus> MAP = Stream.of(ExecutionStatus.values()).collect(Collectors.toMap(rc -> rc.name(), Function.identity()));

	public String getStatus() {
		return this.name();
	}

    public static ExecutionStatus fromString( String scenario ) {
		if (StringUtils.isEmpty(scenario))
			return null;
		return Optional.ofNullable(MAP.get(scenario.toUpperCase())).orElse(null);
	}

}
