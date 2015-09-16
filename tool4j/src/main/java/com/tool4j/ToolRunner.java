package com.tool4j;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ToolRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(Tool.class);
    CommandLineParser parser = new BasicParser();

    public void run(final Object tool,String[] args) throws ParseException, InvocationTargetException, IllegalAccessException {
        Options options = createOptions(tool.getClass().getAnnotation(Tool.class));
        Data data = findData(tool);
        if (data.partitioned()) {
            options.addOption(OptionBuilder
                    .withArgName("number of threads")
                    .withLongOpt("threadsNumber")
                    .withType(Integer.class)
                    .hasArg(true)
                    .isRequired(true)
                    .create());
        }
        CommandLine parse = parse(tool, options, args);
        Object result = initMethods(tool, parse);

        ExecutorService executor = Executors.newFixedThreadPool(Integer.parseInt(parse.getOptionValue("threadsNumber", "1")));
        final Method method = findExecution(tool);


            if (data.partitioned()) {
                Collection<Object> results = (Collection)result;
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
                        params[i] = result;
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
                LOGGER.info(progress.invoke(tool).toString());
                executor.awaitTermination(5000, MILLISECONDS);
            } catch (InterruptedException e) {
                LOGGER.error("Error while waiting for completion:", e);
            }
        }

    }

    private CommandLine parse(Object tool, org.apache.commons.cli.Options apacheOptions, String[] args) {
        try {
            return parser.parse(apacheOptions, args);
        } catch (ParseException ex) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(tool.getClass().getSimpleName(), apacheOptions, true);
            System.exit(-1);
            return null;
        }
    }

    private Data findData(Object tool) {
        Data data = null;
        int count = 0;
        for (Method method : tool.getClass().getDeclaredMethods()) {
            data = method.getAnnotation(Data.class);
            if (data != null) {
                count++;
            }
        }
        if (count > 1) {
            throw new RuntimeException("Just one data alowed");
        }
        return data;
    }

    private Object initMethods(Object tool, CommandLine parse) throws ParseException, InvocationTargetException, IllegalAccessException {
        for (Method method : tool.getClass().getDeclaredMethods()) {
            Data data = method.getAnnotation(Data.class);
            if (data != null) {
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
                return method.invoke(tool, params);
            }
        }
        return null;
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


    private Method findProgress(Object tool) {
        for (Method method : tool.getClass().getDeclaredMethods()) {
            Progress execution = method.getAnnotation(Progress.class);
            if (execution != null) {
                return method;
            }
        }
        return null;
    }

    private Options createOptions(Tool tool) {
        org.apache.commons.cli.Options apacheOptions = new org.apache.commons.cli.Options();
        if (tool != null) {
            Option[] optionArray = tool.value();
            if (optionArray.length > 0) {
                for (Option option : optionArray) {
                    apacheOptions.addOption(OptionBuilder
                            .withArgName(option.value())
                            .withLongOpt(option.value())
                            .withType(option.type())
                            .hasArg(option.hasArg())
                            .isRequired(option.isRequired())
                            .create());
                }

            }
        }
        return apacheOptions;
    }

}
