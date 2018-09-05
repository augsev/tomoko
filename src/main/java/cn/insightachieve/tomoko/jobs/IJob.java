package cn.insightachieve.tomoko.jobs;

import java.io.Serializable;

@FunctionalInterface
public interface IJob extends Runnable, Serializable, Cloneable {
}