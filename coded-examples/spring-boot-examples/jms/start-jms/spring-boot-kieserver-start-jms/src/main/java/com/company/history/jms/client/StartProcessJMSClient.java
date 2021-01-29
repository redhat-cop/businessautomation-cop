package com.company.history.jms.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//import static org.kie.soup.commons.xstream.XStreamUtils.createTrustingXStream;

import javax.jms.TextMessage;

import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.redhat.services.model.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.company.Constants;

@Component
public class StartProcessJMSClient {

	Logger logger = LoggerFactory.getLogger(StartProcessJMSClient.class);
	@Autowired
	private JmsTemplate jmsTemplate;

	private static final Marshaller marshaller = MarshallerFactory.getMarshaller(
			new HashSet<Class<?>>(Arrays.asList(Person.class)), MarshallingFormat.JSON,
			StartProcessJMSClient.class.getClassLoader());

	@Value("${kieserver.process.queue}")
	private String queue;

	@Scheduled(fixedRate = 5000)
	public void startProcess() {

		Person person = new Person("Anton Giertli");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("person", person);

		logger.info("Sending message to Start a process");
		jmsTemplate.send(queue, messageCreator -> {
			TextMessage message = messageCreator.createTextMessage(marshaller.marshall(params));
			message.setStringProperty(Constants.CONTAINER_ID, Constants.CONTAINER_ID_VALUE);
			message.setStringProperty(Constants.PROCESS_ID, Constants.PROCESS_ID_VALUE);

			logger.info("Sending JMS Message {}", message);
			return message;
		});
	}

}
