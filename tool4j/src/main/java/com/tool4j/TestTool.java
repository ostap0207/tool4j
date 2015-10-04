package com.tool4j;


import org.apache.commons.cli.ParseException;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;


@Tool(name = "testTool", value ={@Option("name"), @Option("card")})
public class TestTool {

    AtomicInteger totalProcessed = new AtomicInteger();

    public static void main(String[] args) throws ParseException, IllegalAccessException, InvocationTargetException {
        ToolRunner tool = new ToolRunner();
        tool.run(new TestTool(), new String[]{"--name", "name", "--card", "2123 1242 1241 1241"});
    }

    @Data
    public Collection<Integer> init(@Value("name") String name,
                                    @Value("card") String card) {
        System.out.println(name);
        System.out.println(card);
        return Arrays.asList(1, 2, 3, 4);
    }

    @Execution
    public void run(@Value("name") String name, @Data Collection<Integer> data) {
        try {
            System.out.println("Started task " + data);
            Thread.sleep(5000 * data.iterator().next());
            System.out.println("Finished task " + data);
            totalProcessed.addAndGet(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
