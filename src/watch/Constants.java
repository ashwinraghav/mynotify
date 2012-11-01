package watch;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Constants {
	public static final Map<String, Object> exchangeMap;
    static {
    	Map<String, Object> tempMap = new HashMap<String, Object>();
    	tempMap.put("type", "fanout");
    	tempMap.put("autoDelete", true);
    	tempMap.put("durable", false);
    	tempMap.put("internal", false);
    	tempMap.put("arguments", null);
        exchangeMap = Collections.unmodifiableMap(tempMap);
    }
    public static final String host = "elmer.cs.virginia.edu";
	
}
