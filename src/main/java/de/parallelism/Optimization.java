package de.parallelism;

import java.util.EventListener;

import de.manetmodel.units.Time;
import de.manetmodel.units.Timer;

public abstract class Optimization<M> implements EventListener {
	protected M manet;
	protected Timer duration;

	public Optimization(M manet) {
		this.manet = manet;
		this.duration = new Timer();
	}

	public abstract M execute();

	public Time stop() {
		return duration.getTime();
	}

	public Time getRuntime() {
		return duration.getTime();
	}
	
	public void start() {
		duration.start();
	}

	public M getManet() {
		return manet;
	}

}
