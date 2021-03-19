package de.parallelism;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

public class Task<O, R> implements Callable<List<Void>> {

	private Function<O, List<Void>> f;
	private O o;

	public Task(Function<O, List<Void>> f, O o) {
		this.o = o;
		this.f = f;
	}


	@Override
	public List<Void> call() throws Exception {
		return f.apply(o);
	}

}
