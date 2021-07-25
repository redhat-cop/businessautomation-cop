package com.bacop.jwt_dm_project.droolsLogger;

import java.util.Collection;
import java.util.Iterator;

import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.AgendaGroupPoppedEvent;
import org.kie.api.event.rule.AgendaGroupPushedEvent;
import org.kie.api.event.rule.BeforeMatchFiredEvent;
import org.kie.api.event.rule.MatchCancelledEvent;
import org.kie.api.event.rule.MatchCreatedEvent;
import org.kie.api.event.rule.RuleFlowGroupActivatedEvent;
import org.kie.api.event.rule.RuleFlowGroupDeactivatedEvent;
import org.kie.api.runtime.rule.FactHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuleLogger implements AgendaEventListener {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private static final String LOG_HEADER = "SIMPLE_RULES_LOG";

  /*
	 * AgendaEventListener methods
   */
  public void matchCancelled(MatchCancelledEvent event) {
    writeToLogDebug("MATCH_CANCELLED " + event.getMatch().getRule().getName());
  }

  public void matchCreated(MatchCreatedEvent event) {
    writeToLogDebug("MATCH_CREATED " + event.getMatch().getRule().getName());
  }

  public void afterMatchFired(AfterMatchFiredEvent event) {
    writeToLogDebug("AFTER_MATCH_FIRED " + event.getMatch().getRule().getName());
    Collection<FactHandle> factList = event.getKieRuntime().getFactHandles();
    for (Iterator<FactHandle> iterator = factList.iterator(); iterator.hasNext();) {
      FactHandle fh = iterator.next();
      Object o = event.getKieRuntime().getObject(fh);
      writeToLogInfo("After_Fire_Object found : " + o.getClass().getName());
    }
  }

  public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
    writeToLogDebug("AFTER_RULE_FLOW_ACTIVATED " + event.getRuleFlowGroup().getName());
  }

  public void afterRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
    writeToLogDebug("AFTER_RULE_FLOW_DEACTIVATED " + event.getRuleFlowGroup().getName());
  }

  public void agendaGroupPopped(AgendaGroupPoppedEvent event) {
    writeToLogDebug("AGENDA_POPPED_OUT " + event.getAgendaGroup().getName());
  }

  public void agendaGroupPushed(AgendaGroupPushedEvent event) {
    writeToLogDebug("AGENDA_PUSHED_IN " + event.getAgendaGroup().getName());
  }

  public void beforeMatchFired(BeforeMatchFiredEvent event) {
    writeToLogInfo("FIRING_RULE " + event.getMatch().getRule().getName());
    Collection<FactHandle> factList = event.getKieRuntime().getFactHandles();
    for (Iterator<FactHandle> iterator = factList.iterator(); iterator.hasNext();) {
      FactHandle fh = iterator.next();
      Object o = event.getKieRuntime().getObject(fh);
      writeToLogInfo("Before_Fire_Object found : " + o.getClass().getName());
    }
  }

  public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
    writeToLogDebug("BEFORE_RULE_FLOW_ACTIVATED " + event.getRuleFlowGroup().getName());
  }

  public void beforeRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
    writeToLogDebug("BEFORE_RULE_FLOW_DEACTIVATED " + event.getRuleFlowGroup().getName());
  }

  private void writeToLogInfo(String message) {
    logger.info(LOG_HEADER + " " + message);
  }

  private void writeToLogDebug(String message) {
    logger.debug(LOG_HEADER + " " + message);
  }

}
