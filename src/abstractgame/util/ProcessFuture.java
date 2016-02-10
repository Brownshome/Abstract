package abstractgame.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/** Represents a result made by processing a future, T is the item accepted, V is the item returned.
 * Note that the function is called in the thread that called get(). */
public class ProcessFuture<T, V> implements Future<V> {
	Future<T> future;
	Function<T, V> func;
	
	public ProcessFuture(Future<T> future, Function<T, V> func) {
		this.future = future;
		this.func = func;
	}
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return future.cancel(mayInterruptIfRunning);
	}

	@Override
	public V get() throws InterruptedException, ExecutionException {
		return func.apply(future.get());
	}

	@Override
	public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return func.apply(future.get(timeout, unit));
	}

	@Override
	public boolean isCancelled() {
		return future.isCancelled();
	}

	@Override
	public boolean isDone() {
		return future.isDone();
	}
}
