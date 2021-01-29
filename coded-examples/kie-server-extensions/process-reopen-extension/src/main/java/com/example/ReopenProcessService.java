package com.example;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.drools.core.command.runtime.process.StartProcessCommand;
import org.jbpm.kie.services.impl.admin.commands.TriggerNodeCommand;
import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.admin.ProcessInstanceAdminService;
import org.jbpm.services.api.admin.ProcessNode;
import org.jbpm.services.api.model.NodeInstanceDesc;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.jbpm.services.api.model.VariableDesc;
import org.kie.api.definition.process.Node;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.query.QueryContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReopenProcessService {

	private static final Logger logger = LoggerFactory.getLogger(ReopenProcessService.class);
	private RuntimeDataService runtimeService;
	private ProcessInstanceAdminService adminService;
	private ProcessService processService;

	public ReopenProcessService(RuntimeDataService runtimeService, ProcessInstanceAdminService adminService,
			ProcessService processService) {
		this.runtimeService = runtimeService;
		this.adminService = adminService;
		this.processService = processService;
	}

	public Long reopenProcess(Long instanceId, List<String> nodes)
			throws NamingException, NotSupportedException, SystemException, SecurityException, IllegalStateException,
			RollbackException, HeuristicMixedException, HeuristicRollbackException {
		logger.info("Reopening process {}", instanceId);

		// 1) retrieve variable history for the aborted/completed process instance
		Map<String, Object> params = getVariableHistory(instanceId);

		// 2) Retrieve the aborted/completed process details and start the new
		// process instance
		ProcessInstanceDesc processInstanceDesc = runtimeService.getProcessInstanceById(instanceId);
		logger.info("Found process instance {} ", processInstanceDesc);

		Long newPid = processService.startProcess(processInstanceDesc.getDeploymentId(),
				processInstanceDesc.getProcessId(), params); // this is not ideal, as it will actually start executing
																// all the nodes right after the start node until first
																// wait state

		logger.info("started a new process instance {}", newPid);

		// 3) Get the process nodes and trigger those which came through REST Payload

		Collection<ProcessNode> processNodes = adminService.getProcessNodes(newPid);

		Set<Long> activateMe = new HashSet<Long>();

		processNodes.forEach(pn -> {

			if (nodes.contains(pn.getNodeName())) {
				activateMe.add(pn.getNodeId());
				logger.info("Going to activate node {} with id {}", pn.getNodeName(), pn.getNodeId());

			}
		});

		activateMe.forEach(node -> {

			logger.info("Calling trigger for pid {} , nid {}", newPid, node);
			adminService.triggerNode(newPid, node);

		});

		return newPid;
	}

	private Map<String, Object> getVariableHistory(Long instanceId) {
		Collection<VariableDesc> vars = runtimeService.getVariablesCurrentState(instanceId);
		vars.forEach(v -> {

			logger.info("Found variable {}", v);
		});

		Map<String, Object> params = new HashMap<String, Object>();
		vars.forEach(v -> {
			params.put(v.getVariableId(), v.getNewValue());
		});
		return params;
	}

	/**
	 * This is currently not possible since abort adds EXIT entry in NodeInstanceLog
	 * 
	 * @param id
	 * @return
	 */
	public Map<Long, NodeInstanceDesc> getActiveNodes(Long id) {
		Map<Long, NodeInstanceDesc> activeNodes = new HashMap<Long, NodeInstanceDesc>();

		logger.info("calling getProcessInstanceFullHistory");
		Collection<NodeInstanceDesc> nodesHistory = runtimeService.getProcessInstanceFullHistory(id,
				new QueryContext());
		nodesHistory.forEach(n -> {
			logger.info("[getProcessInstanceFullHistory] we have found a node {}", n);
		});

		Map<Long, List<NodeInstanceDesc>> allNodesGroupped;

		allNodesGroupped = nodesHistory.stream().collect(Collectors.groupingBy(NodeInstanceDesc::getId));

		allNodesGroupped.forEach((k, v) -> {

			if (v.size() % 2 == 0) { // node has been entered and exited so we don't care about it

			} else { // if it was entered only, it means it was active at the time of aborting the
						// process
				activeNodes.put(k, v.get(0));
			}
		});

		activeNodes.forEach((k, v) -> {

			logger.info("Found active node with id {} and details {}", k, v);
		});

		return activeNodes;

	}

}
