package cn.insightachieve.tomoko.jobs;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Component
public class JobScheduler {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(JobScheduler.class);

    private final ExecutorService executorService;

    public JobScheduler() {
        LOGGER.info("JobScheduler is starting.");
        executorService = TomokoJobExecutors.newSingleThreadExecutor();

        LOGGER.info("JobScheduler started.");
    }

    public JobScheduler scheduleJob(IJob job) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Schedule runnable job[{}].", job);
        }
        this.executorService.submit(job);
        return this;
    }

    public JobScheduler scheduleJobs(IJob... jobs) {
        for (IJob job : jobs) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Schedule runnable job[{}].", job);
            }
            this.executorService.submit(job);
        }
        return this;
    }

    public Future<?> scheduleFJob(IJob job) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Schedule f-runnable job[{}].", job);
        }
        return this.executorService.submit(job);
    }

    public <T> Future<T> scheduleFrJob(IJob job, T result) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Schedule fr-runnable job[{}].", job);
        }
        return this.executorService.submit(job, result);
    }

    public <T> Future<T> scheduleCallableJob(ICallableJob<T> job) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Schedule callable job[{}].", job);
        }
        return this.executorService.submit(job);
    }

    @PreDestroy
    public void stop() {
        LOGGER.info("JobScheduler is stopping.");
        executorService.shutdownNow();
        LOGGER.info("JobScheduler stopped.");
    }
}