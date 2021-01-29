package com.mycompany.app.models;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AuthLevel {
	public static final Map<String, Integer> AUTH_LEVEL = createMap();

    private static Map<String, Integer> createMap() {
        Map<String, Integer> authLevel = new HashMap<String, Integer>();
        authLevel.put("manager",2);
        authLevel.put("supervisor",1);
        authLevel.put("user",0);
        return Collections.unmodifiableMap(authLevel);
    }
	
	
}
