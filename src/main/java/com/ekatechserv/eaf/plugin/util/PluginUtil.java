package com.ekatechserv.eaf.plugin.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;

public class PluginUtil {

    /**
     * Utility to convert httpEntity to Map<String,String>
     *
     * @param entity httpEntity to convert
     * @return Map<String, String> object after conversion
     * @throws IOException
     */
    public static Map<String, String> convertEntityToMap(HttpEntity entity) throws IOException {
        String strTemp = entity != null ? EntityUtils.toString(entity) : StringUtils.EMPTY;
        if (StringUtils.isEmpty(strTemp)) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(
                strTemp,
                mapper.getTypeFactory().constructMapType(HashMap.class, String.class, String.class));
    }

    /**
     * Utility to convert Object into given class type T
     *
     * @param <T> Class Type to convert to
     * @param obj Object to covnert to
     * @param classType
     * @return converted object
     * @throws IOException
     */
    public static <T> T convertEntityToType(Object obj, Class<T> classType) throws IOException {
        String strTemp = (String) (obj instanceof String ? obj : EntityUtils.toString((HttpEntity) obj));
        if (StringUtils.isEmpty(strTemp)) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(
                strTemp,
                mapper.getTypeFactory().constructType(classType));
    }

    /**
     * Utility to convert Trace into String
     *
     * @param e Throwable object
     * @return converted String
     */
    public static String getTraceAsString(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.close();
        return sw.toString();
    }
}
