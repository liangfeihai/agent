package com.agent.test;

import java.lang.instrument.Instrumentation;

/**
 * Hello world!
 */
public class App {
    public static void premain(String agentOps, Instrumentation inst) {
        System.out.println("=========premain方法执行========");
        // 添加Transformer
        inst.addTransformer(new DemoAgent());
        System.out.println("=========premain方法退出========");
    }
}
