package de.parallelism;

import java.util.EventListener;

import de.manetmodel.units.Time;
import de.manetmodel.units.Timer;



public abstract class Optimization<Q, M> implements EventListener {
	protected M manet;
	protected Timer duration;

	public Optimization(M manet) {
		this.manet = manet;
		this.duration = new Timer();
		duration.start();
	}

	public abstract Q execute();

	public Time stop() {
		return duration.getTime();
	}

	public M getManet() {
		return manet;
	}

}
