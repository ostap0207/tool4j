package com.tool4j;

import com.tool4j.processors.DataProcessor;
import com.tool4j.processors.ExecutionProcessor;
import com.tool4j.processors.ToolProcessor;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * Class that runs all the tools and manages data exchange
 */
public class ToolRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(Tool.class);
    CommandLineParser parser = new BasicParser();

    /**
     * Start the tool execution
     * @param tool create and initialized tool object
     * @param args command line input params
     * @throws ParseException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public void run(final Object tool, String[] args) throws ParseException, InvocationTargetException, IllegalAccessException {
        ToolProcessor toolProcessor = new ToolProcessor(tool);
        DataProcessor dataProcessor = new DataProcessor(tool);
        ExecutionProcessor execution = new ExecutionProcessor(tool);

        CommandLine parse = parse(tool, args, toolProcessor, dataProcessor);

        String name = toolProcessor.name();
        Object data = dataProcessor.getData(parse);

        LOGGER.info("{} has started", name);
        execution.run(parse, data, dataProcessor.raw().partitioned());
        LOGGER.info("{} has finished", name);

    }

    private CommandLine parse(Object tool, String[] args, ToolProcessor toolProcessor, DataProcessor dataProcessor) {
        Options options = toolProcessor.options();
        if (dataProcessor.raw().partitioned()) {
            options.addOption(OptionBuilder
                    .withArgName("number of threads")
                    .withLongOpt("threadsNumber")
                    .withType(Integer.class)
                    .hasArg(true)
                    .isRequired(true)
                    .create());
        }


        try {
            return parser.parse(options, args);
        } catch (ParseException ex) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(tool.getClass().getSimpleName(), options, true);
            System.exit(-1);
            return null;
        }
    }

}
