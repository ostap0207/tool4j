package com.tool4j.processors;

import com.tool4j.Option;
import com.tool4j.Tool;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

public class ToolProcessor implements AnnotationProcessor<Tool> {

    private Object object;
    private Tool tool;

    public ToolProcessor(Object object) {
        this.object = object;
        tool = object.getClass().getAnnotation(Tool.class);
    }

    public Options options() {
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

    public String name() {
        return tool.name();
    }

    public Tool raw() {
        return tool;
    }
}
