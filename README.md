## tool4j
tool4j is an easy to use annotation based mini framework that helps to write command line tools.


### Usage

```java
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
      //our task
    }

}
```

### Annotations
- @Tool: use on top of the main class
- @Option: use to specify which command line argument your tool can accept
- @Data: use for a method which generates initial data for a tool
- @Execution: use to mark the actuall execution method
- @Progress (optional): use to return current progress
- @Value: use to inject command line arguments into the methods

Contributions are welcome
