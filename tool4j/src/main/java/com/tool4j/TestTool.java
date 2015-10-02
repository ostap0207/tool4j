package com.tool4j;


import org.apache.commons.cli.ParseException;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;


@Tool(name = "testTool", value ={@Option("providerId"), @Option("externalArtistId")})
public class TestTool {

    AtomicInteger totalProcessed = new AtomicInteger();

    public static void main(String[] args) throws ParseException, IllegalAccessException, InvocationTargetException {
        ToolRunner tool = new ToolRunner();
        tool.run(new TestTool(), new String[]{"--providerId", "1", "--externalArtistId", "2"});
    }

    @Data
    public Collection<Integer> init(@Value("providerId") String providerId,
                                    @Value("externalArtistId") String artistId) {
        System.out.println(providerId);
        System.out.println(artistId);
        return Arrays.asList(1, 2, 3, 4);
    }

    @Execution
    public void run(@Value("providerId") String providerId, @Data Collection<Integer> data) {
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
