package com.company.history.jms.server;

import java.util.Enumeration;
import java.util.HashMap;

import javax.jms.JMSException;
import javax.jms.Message;

import org.jbpm.services.api.ProcessService;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.company.Constants;
import com.company.history.jms.client.StartProcessJMSClient;

@Component
@Transactional
public class CustomKieServerMDB {

	private static final Logger logger = LoggerFactory.getLogger(CustomKieServerMDB.class);
	private static final Marshaller marshaller = MarshallerFactory.getMarshaller(MarshallingFormat.JSON,
			StartProcessJMSClient.class.getClassLoader());

	@Autowired
	private ProcessService pService;

	@JmsListener(destination = "${kieserver.process.queue}")
	public void receiveMessage(Message message) throws JMSException {

		String json = message.getBody(String.class);
		HashMap<String, Object> params = marshaller.unmarshall(json, HashMap.class);
		logger.info("Request to start a process received with following payload {}", params);
		
		Enumeration props = message.getPropertyNames();

		while (props.hasMoreElements()) {
			String propName = (String) props.nextElement();
			logger.info("Property name {}, property value {}", propName, message.getStringProperty(propName));
		}

		pService.startProcess(message.getStringProperty(Constants.CONTAINER_ID),
				message.getStringProperty(Constants.PROCESS_ID), params);

	}
}
