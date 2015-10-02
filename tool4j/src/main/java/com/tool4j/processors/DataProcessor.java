package com.tool4j.processors;

import com.tool4j.Data;
import com.tool4j.Value;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

public class DataProcessor implements AnnotationProcessor<Data> {

    private Data data = null;
    private Method method;
    private Object object;

    public DataProcessor(Object object) {
        this.object = object;
        this.method = findDataMethod(object);
        if (method != null) {
            this.data = method.getAnnotation(Data.class);
        }
    }

    public Object getData(CommandLine parse) throws ParseException, InvocationTargetException, IllegalAccessException {
            if (method != null) {
                Class c = method.getReturnType();
                if (c != Collection.class && data.partitioned()){
                    throw new RuntimeException();
                }

                Object[] params = new Object[method.getParameterTypes().length];
                int i = 0;
                for (Annotation[] aClass : method.getParameterAnnotations()) {
                    Value optionValue = (Value) aClass[0];
                    String valueName = optionValue.value();
                    params[i] = parse.getParsedOptionValue(valueName);
                    i++;
                }
                return method.invoke(object, params);
            }
        return null;
    }

    private Method findDataMethod(Object tool) {
        Data data = null;
        int count = 0;
        Method result = null;
        for (Method method : tool.getClass().getDeclaredMethods()) {
            data = method.getAnnotation(Data.class);
            if (data != null) {
                count++;
                result = method;
            }
        }
        if (count > 1) {
            throw new RuntimeException("Just one data is allowed");
        }
        return result;
    }

    public Data raw() {
        return data;
    }
}
