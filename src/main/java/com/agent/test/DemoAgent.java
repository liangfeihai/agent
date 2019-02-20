package com.agent.test;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class DemoAgent implements ClassFileTransformer {
    public final String injectedClassName = "com.agent.test.FirstTest,cn.utrust.fintech.p2passet.rest.PartnerAssetController" +
            ",cn.utrust.fintech.p2passet.service.impl.PartnerAssetWeCashServiceImpl";
    public final String methodName = "hello,applyCredit,applyCredit";

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        className = className.replace("/", ".");
        if (injectedClassName.contains(className)) {
            CtClass ctClass = null;
            try {
                ClassPool classPool=ClassPool.getDefault();
                ctClass = classPool.getCtClass(className);// 使用全称,用于取得字节码类<使用javassist>
                CtMethod[] ctMethods=ctClass.getDeclaredMethods();
                for (CtMethod ctMethod:ctMethods
                     ) {
                    if(methodName.contains(ctMethod.getName())){
                        //插入本地变量
                        ctMethod.addLocalVariable("begin",CtClass.longType);
                        ctMethod.addLocalVariable("end",CtClass.longType);

                        ctMethod.insertBefore("begin=System.currentTimeMillis();System.out.println(\"begin=\"+begin" +
                                "+\",className:\"+this.getClass().getName()+\",methodName:\"" +
                                "+Thread.currentThread().getStackTrace()[1].getMethodName());");

                        ctMethod.insertAfter("end=System.currentTimeMillis();System.out.println(\"end=\"+end" +
                                "+\",className:\"+this.getClass().getName()+\",methodName:\"" +
                                "+Thread.currentThread().getStackTrace()[1].getMethodName());");
                        ctMethod.insertAfter("System.out.println(\"methodName:\"+Thread.currentThread().getStackTrace()[1].getMethodName()" +
                                "+\" 性能:\"+(end-begin)+\"毫秒\");");

                        return ctClass.toBytecode();
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }
}
