package com.bacop.jwt_dm_project.Testing;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import com.bacop.jwt_dm_project.ContinentMap;
import com.bacop.jwt_dm_project.Country;
import com.bacop.jwt_dm_project.CustomDuties;

import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuleTest {

  static final Logger LOG = LoggerFactory.getLogger(RuleTest.class);

  @Test
  public void test() {
    KieServices kieServices = KieServices.Factory.get();

    KieContainer kContainer = kieServices.getKieClasspathContainer();
    Results verifyResults = kContainer.verify();
    for (Message m : verifyResults.getMessages()) {
      LOG.info("{}", m);
    }

    LOG.info("Creating kieBase");
    KieBase kieBase = kContainer.getKieBase();

    LOG.info("There should be rules: ");
    for (KiePackage kp : kieBase.getKiePackages()) {
      for (Rule rule : kp.getRules()) {
        LOG.info("Rule Package " + kp + " Rule Name '" + rule.getName() + "'");
      }
    }

    LOG.info("Creating kieSession");
    KieSession session = kieBase.newKieSession();

    LOG.info("Now running data");

    {
      ContinentMap fact = new ContinentMap("Europe", "France");
      session.insert(fact);
    }
    {
      ContinentMap fact = new ContinentMap("Europe", "Denmark");
      session.insert(fact);
    }
    {
      ContinentMap fact = new ContinentMap("Europe", "Germany");
      session.insert(fact);
    }
    {
      ContinentMap fact = new ContinentMap("Europe", "Greece");
      session.insert(fact);
    }
    {
      ContinentMap fact = new ContinentMap("Europe", "Greece");
      session.insert(fact);
    }
    {
      ContinentMap fact = new ContinentMap("Asia", "Maldives");
      session.insert(fact);
    }
    {
      Country fact = new Country("France");
      session.insert(fact);
    }
    {
      Country fact = new Country("Maldives");
      session.insert(fact);
    }
    session.fireAllRules();

    LOG.info("Final checks");

    // assertEquals("Size of object in Working Memory is
    // 15",15,session.getObjects().size());

    // ---
    {
      LOG.info("---");
      LOG.info("Custom Duties should be applied to NON-EUROPEAN countries");
      boolean shouldBeFound = true;
      String continent = "EUROPE";
      String apply = "YES";
      Iterator<? extends Object> oIter = session.getObjects().iterator();
      while (oIter.hasNext()) {
        Object o = oIter.next();
        if (o instanceof CustomDuties) {
          CustomDuties cd = (CustomDuties) o;
          if (!cd.getContinent().equalsIgnoreCase(continent)) {
            LOG.info(cd.toString());
            assertEquals("Custom Duties for " + cd.getCountry() + " are " + apply, apply, cd.getApply());
          }
        }
      }
      assertEquals("Custom Duties found ", true, shouldBeFound);
    }

    // ---
    {
      LOG.info("---");
      LOG.info("Custom Duties should NOT be applied to EUROPEAN countries");
      boolean shouldBeFound = true;
      String continent = "EUROPE";
      String apply = "NO";
      Iterator<? extends Object> oIter = session.getObjects().iterator();
      while (oIter.hasNext()) {
        Object o = oIter.next();
        if (o instanceof CustomDuties) {
          CustomDuties cd = (CustomDuties) o;
          if (cd.getContinent().equalsIgnoreCase(continent)) {
            LOG.info(cd.toString());
            assertEquals("Custom Duties for " + cd.getCountry() + " are " + apply, apply, cd.getApply());
          }
        }
      }
      assertEquals("Custom Duties found ", true, shouldBeFound);
    }

  }
}
