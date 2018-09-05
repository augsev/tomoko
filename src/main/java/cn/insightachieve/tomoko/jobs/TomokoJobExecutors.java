package cn.insightachieve.tomoko.jobs;

import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TomokoJobExecutors {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TomokoJobExecutors.class);

    private static class DelegatedExecutorService extends AbstractExecutorService {
        private final ExecutorService e;

        DelegatedExecutorService(ExecutorService executor) {
            e = executor;
        }

        public void execute(Runnable command) {
            e.execute(command);
        }

        public void shutdown() {
            e.shutdown();
        }

        public List<Runnable> shutdownNow() {
            return e.shutdownNow();
        }

        public boolean isShutdown() {
            return e.isShutdown();
        }

        public boolean isTerminated() {
            return e.isTerminated();
        }

        public boolean awaitTermination(long timeout, TimeUnit unit)
                throws InterruptedException {
            return e.awaitTermination(timeout, unit);
        }

        public Future<?> submit(Runnable task) {
            return e.submit(task);
        }

        public <T> Future<T> submit(Callable<T> task) {
            return e.submit(task);
        }

        public <T> Future<T> submit(Runnable task, T result) {
            return e.submit(task, result);
        }

        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
                throws InterruptedException {
            return e.invokeAll(tasks);
        }

        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
                                             long timeout, TimeUnit unit)
                throws InterruptedException {
            return e.invokeAll(tasks, timeout, unit);
        }

        public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
                throws InterruptedException, ExecutionException {
            return e.invokeAny(tasks);
        }

        public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
                               long timeout, TimeUnit unit)
                throws InterruptedException, ExecutionException, TimeoutException {
            return e.invokeAny(tasks, timeout, unit);
        }
    }

    private static class FinalizableDelegatedExecutorService extends TomokoJobExecutors.DelegatedExecutorService {
        FinalizableDelegatedExecutorService(ExecutorService executor) {
            super(executor);
        }

        protected void finalize() {
            super.shutdown();
        }
    }

    private static class TomokoJobThreadPoolExecutor extends ThreadPoolExecutor {
        public TomokoJobThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        }

        @Override
        protected void beforeExecute(Thread t, Runnable r) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Start job[{}].", r);
            }
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Finish job[{}].", r);
            }
        }
    }

    private static class TomokoJobThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        public TomokoJobThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    "tomoko-ui-job-thread-" + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

    public static ExecutorService newSingleThreadExecutor() {
        return new TomokoJobExecutors.FinalizableDelegatedExecutorService(
                new TomokoJobThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<Runnable>(), new TomokoJobThreadFactory()));
    }
}
