package de.parallelism;

public abstract class Optimization<Q, M> {
	protected M manet;

	public Optimization(M manet) {
		this.manet = manet;
	}

	public abstract Q execute();

	public M getManet() {
		return manet;
	}

}
