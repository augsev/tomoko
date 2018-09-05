package cn.insightachieve.tomoko.jobs;

import java.io.Serializable;
import java.util.concurrent.Callable;

@FunctionalInterface
public interface ICallableJob<V> extends Callable<V>, Serializable, Cloneable {
}