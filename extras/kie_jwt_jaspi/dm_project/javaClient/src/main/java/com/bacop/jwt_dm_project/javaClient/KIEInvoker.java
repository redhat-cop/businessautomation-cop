package com.bacop.jwt_dm_project.javaClient;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.bacop.jwt_dm_project.ContinentMap;
import com.bacop.jwt_dm_project.Country;

import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieServiceResponse.ResponseType;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.RuleServicesClient;

public class KIEInvoker {

    public void invoke(String url, String username, String password, String ksession, String containerId) {

        String fancyPrefix = "..::|| ";

        KieServicesConfiguration config = KieServicesFactory.newRestConfiguration(url, username, password);

        config.setMarshallingFormat(MarshallingFormat.JSON);

        Set<Class<?>> allClasses = new HashSet<Class<?>>();
        allClasses.add(ContinentMap.class);
        allClasses.add(Country.class);
        config.addExtraClasses(allClasses);

        KieServicesClient client = KieServicesFactory.newKieServicesClient(config);

        RuleServicesClient ruleClient = client.getServicesClient(RuleServicesClient.class);
        List<Command<?>> commands = new ArrayList<Command<?>>();

        KieCommands kieCommander = KieServices.Factory.get().getCommands();

        {
            ContinentMap fact = new ContinentMap("Europe", "United Kingdom");
            commands.add(new InsertObjectCommand(fact));
        }
        {
            ContinentMap fact = new ContinentMap("Europe", "France");
            commands.add(new InsertObjectCommand(fact));
        }
        {
            ContinentMap fact = new ContinentMap("Europe", "Italy");
            commands.add(new InsertObjectCommand(fact));
        }
        {
            ContinentMap fact = new ContinentMap("Europe", "Germany");
            commands.add(new InsertObjectCommand(fact));
        }
        {
            ContinentMap fact = new ContinentMap("Europe", "Greece");
            commands.add(new InsertObjectCommand(fact));
        }
        {
            ContinentMap fact = new ContinentMap("Asia", "India");
            commands.add(new InsertObjectCommand(fact));
        }
        {
            ContinentMap fact = new ContinentMap("Asia", "Japan");
            commands.add(new InsertObjectCommand(fact));
        }
        {
            ContinentMap fact = new ContinentMap("Asia", "Thailand");
            commands.add(new InsertObjectCommand(fact));
        }
        {
            ContinentMap fact = new ContinentMap("Asia", "Indonesia");
            commands.add(new InsertObjectCommand(fact));
        }
        {
            ContinentMap fact = new ContinentMap("Asia", "Maldives");
            commands.add(new InsertObjectCommand(fact));
        }
        {
            Country fact = new Country("France");
            commands.add(new InsertObjectCommand(fact));
        }
        {
            Country fact = new Country("Maldives");
            commands.add(new InsertObjectCommand(fact));
        }

        commands.add(new FireAllRulesCommand("fireAll"));

        BatchExecutionCommand batchCommand = kieCommander.newBatchExecution(commands, ksession);
        {
            try {
                Marshaller marshallerJAXB = MarshallerFactory.getMarshaller(allClasses, MarshallingFormat.JAXB,
                        this.getClass().getClassLoader());
                String bbXML = marshallerJAXB.marshall(batchCommand);
                FileWriter fw = new FileWriter(new File(("batchCommand.xml")));
                fw.write(bbXML);
                fw.close();
                System.out.println(fancyPrefix + "batchCommand has been saved as XML to batchCommand.xml");
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Marshaller marshallerJSON = MarshallerFactory.getMarshaller(allClasses, MarshallingFormat.JSON,
                        getClass().getClassLoader());
                String bbJSON = marshallerJSON.marshall(batchCommand);
                FileWriter fw = new FileWriter(new File(("batchCommand.json")));
                fw.write(bbJSON);
                fw.close();
                System.out.println(fancyPrefix + "batchCommand has been saved as JSON to batchCommand.json");
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Marshaller marshallerXSTREAM = MarshallerFactory.getMarshaller(allClasses, MarshallingFormat.XSTREAM,
                        this.getClass().getClassLoader());
                String bbXSTREAM = marshallerXSTREAM.marshall(batchCommand);
                FileWriter fw = new FileWriter(new File(("batchCommand.xstream")));
                fw.write(bbXSTREAM);
                fw.close();
                System.out.println(fancyPrefix + "batchCommand has been saved as XSTREAM to batchCommand.xstream");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ServiceResponse<ExecutionResults> response = ruleClient.executeCommandsWithResults(containerId, batchCommand);

        if (response.getType().compareTo(ResponseType.SUCCESS) == 0) {
            System.out.println("..::||");
            System.out.println("..::|| SUCCESS : " + containerId + " has been invoked");
            System.out.println("..::||");
        }

        System.out.println("..::|| response.getMsg(): " + response.getMsg() + " ||::..");
        System.out.println("..::|| response.getResult(): " + response.getResult() + " ||::..");
    }
}
