package com.agent.test;

import javassist.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class FirstAgent implements ClassFileTransformer {
    public final String injectedClassName = "com.agent.test.FirstTest";
    public final String methodName = "hello";

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        className = className.replace("/", ".");
        if (className.equals(injectedClassName)) {
            CtClass ctclass = null;
            try {

                ClassPool classPool=ClassPool.getDefault();
                //防止web容器下取不到Class对象
                classPool.insertClassPath(new ClassClassPath(this.getClass()));
                ctclass = classPool.getCtClass(className);// 使用全称,用于取得字节码类<使用javassist>
/*                CtField f=CtField.make("public long time= 0;",ctclass);
                ctclass.addField(f);*/
                CtField f= new CtField(CtClass.longType,"time",ctclass);
                ctclass.addField(f,"0");
                CtMethod ctmethod = ctclass.getDeclaredMethod(methodName);// 得到这方法实例
                ctmethod.insertBefore(insertBeforeStr(ctmethod));
                ctmethod.insertAfter(insertAfterStr(ctmethod));
                return ctclass.toBytecode();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }
    private String insertBeforeStr(CtMethod method) throws CannotCompileException {
//        method.addLocalVariable("startTime",CtClass.longType);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("long startTime=0;");

        stringBuilder.append("startTime = System.currentTimeMillis();");
        stringBuilder.append("time=startTime;");
        stringBuilder.append("System.out.println(\"methodName:"+method.getName()+" startTime:\" + startTime);");
        return stringBuilder.toString();
    }

    private String insertAfterStr(CtMethod method) throws CannotCompileException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("long endTime=0;");
        stringBuilder.append("endTime = System.currentTimeMillis();");
        stringBuilder.append("System.out.println(\"methodName:"+method.getName()+" endTime:\" + endTime);");
        stringBuilder.append("System.out.println(\"methodName:"+method.getName()+"diff:\" + (endTime-time));");
        return stringBuilder.toString();
    }
}
