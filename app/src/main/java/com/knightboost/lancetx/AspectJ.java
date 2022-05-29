package com.knightboost.lancetx;

import android.os.SystemClock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class AspectJ {
    @Pointcut("within(@com.knightboost.lancetx.AppSpeed *)")
    public void withinAnnotatedClass() {
    }

    @Pointcut("execution(!synthetic * *(..)) && withinAnnotatedClass()")
    public void methodInsideAnnotatedType() {
    }

    @Pointcut("execution(@com.knightboost.lancetx.AppSpeed * *(..)) || methodInsideAnnotatedType()")
    public void appSpeedMethod() {

    }

    @Around("appSpeedMethod()")
    public Object logSpeedEvent(ProceedingJoinPoint joinPoint) throws Throwable {
        long startMills = SystemClock.uptimeMillis();
        long startWallTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long stopWallTime = System.currentTimeMillis();
        long stopMills = SystemClock.uptimeMillis();
        return joinPoint.proceed();
    }
}
