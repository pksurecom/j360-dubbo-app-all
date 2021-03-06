package me.j360.dubbo.common.concurrent;

import lombok.extern.slf4j.Slf4j;
import me.j360.dubbo.common.concurrent.thread.PriorityCallable;
import me.j360.dubbo.common.concurrent.thread.PriorityRunnable;
import me.j360.dubbo.modules.util.concurrent.threadpool.ThreadPoolUtil;
import org.springframework.beans.factory.DisposableBean;

import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Package: cn.paomiantv.common.concurrent
 * User: min_xu
 * Date: 2017/6/29 下午4:00
 * 说明：并发优先级容器
 */


@Slf4j
public class DefaultPriorityExecutor implements DisposableBean {

    private static ThreadPoolExecutor executor;

    public DefaultPriorityExecutor() {

        PriorityBlockingQueue queue = new PriorityBlockingQueue(100);
        RejectedExecutionHandler reh = new ThreadPoolExecutor.CallerRunsPolicy();


        executor = new ThreadPoolExecutor(30, 100, 60, TimeUnit.SECONDS, queue, ThreadPoolUtil.buildThreadFactory("DefaultPriorityExecutor", true, (Thread t, Throwable e) -> {
            log.error("优先级任务发生异常:{}", t.toString(), e);
        }), reh);
        
    }


    //提交Future任务
    public static <T> CompletableFuture<T> supplySync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, executor);
    }


    //执行异步任务
    public static CompletableFuture<Void> runAsync(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, executor);
    }


    //提交Runnable任务
    public static void execute(PriorityRunnable command) {
        executor.execute(command);
    }

    //普通submit
    public static <T> Future<T> submit(PriorityCallable<T> command) {
        return executor.submit(command);
    }



    @Override
    public void destroy() throws Exception {
        ThreadPoolUtil.gracefulShutdown(executor, 3000);
    }
}
