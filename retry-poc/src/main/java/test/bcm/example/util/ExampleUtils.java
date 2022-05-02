package test.bcm.example.util;

import org.springframework.util.StringUtils;

public class ExampleUtils {

    /*
        Rest proxy key comes with double quotes like "key-1" on the String itself...
     */
    public static String fixRestProxyKey(String key) {
        return StringUtils.isEmpty(key) ? "null" : key.replace("\"", "");
    }

}
