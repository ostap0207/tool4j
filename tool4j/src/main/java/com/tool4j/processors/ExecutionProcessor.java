package com.tool4j.processors;

import com.tool4j.Data;
import com.tool4j.Execution;
import com.tool4j.Progress;
import com.tool4j.Value;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ExecutionProcessor implements AnnotationProcessor<Execution>{


    private final Method method;
    private Object tool;

    public ExecutionProcessor(Object object) {
        this.tool = object;
        this.method = findExecution(object);
    }

    private Method findExecution(Object tool) {
        for (Method method : tool.getClass().getDeclaredMethods()) {
            Execution execution = method.getAnnotation(Execution.class);
            if (execution != null) {
                return method;
            }
        }
        return null;
    }

    @Override
    public Execution raw() {
        return null;
    }

    public void run(CommandLine parse, Object data, boolean partitioned) throws ParseException, InvocationTargetException, IllegalAccessException {

        ExecutorService executor = Executors.newFixedThreadPool(Integer.parseInt(parse.getOptionValue("threadsNumber", "1")));

        if (partitioned) {
            Collection<Object> results = (Collection) data;
            for (Object o : results) {
                final Object[] params = new Object[method.getParameterTypes().length];
                int i = 0;
                for (Annotation[] aClass : method.getParameterAnnotations()) {
                    if (aClass[0] instanceof Value) {
                        Value optionValue = (Value) aClass[0];
                        String valueName = optionValue.value();
                        params[i] = parse.getParsedOptionValue(valueName);
                    } else if (aClass[0] instanceof Data) {
                        params[i] = o;
                    }
                    i++;
                }
                executor.execute(new Runnable() {

                    public void run() {
                        try {
                            method.invoke(tool, params);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

        } else {
            final Object[] params = new Object[method.getParameterTypes().length];
            int i = 0;
            for (Annotation[] aClass : method.getParameterAnnotations()) {
                if (aClass[0] instanceof Value) {
                    Value optionValue = (Value) aClass[0];
                    String valueName = optionValue.value();
                    params[i] = parse.getParsedOptionValue(valueName);
                } else if (aClass[0] instanceof Data) {
                    params[i] = data;
                }
                i++;
            }
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        method.invoke(tool, params);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        final Method progress = findProgress(tool);
        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                System.out.println(progress.invoke(tool).toString());
                executor.awaitTermination(5000, MILLISECONDS);
            } catch (InterruptedException e) {
                System.out.println("Error while waiting for completion:" + e.getLocalizedMessage());
            }
        }
    }

    private Method findProgress(Object tool) {
        for (Method method : tool.getClass().getDeclaredMethods()) {
            Progress execution = method.getAnnotation(Progress.class);
            if (execution != null) {
                return method;
            }
        }
        return null;
    }
}
