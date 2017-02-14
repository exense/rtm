package org.rtm.e2e.ingestion.load;

import java.util.Map;

public abstract class LoadDescriptor {
	
	private long pauseTime;
	private int nbIterations;
	private int nbTasks;
	
	public LoadDescriptor(long pauseTime, int nbIterations, int nbTasks){
		this.pauseTime = pauseTime;
		this.nbIterations = nbIterations;
		this.nbTasks = nbTasks;
	}
	
	public abstract Map<String, Object> getNextMeasurementForSend(int id);

	public long getPauseTime() {
		return pauseTime;
	}

	public void setPauseTime(long pauseTime) {
		this.pauseTime = pauseTime;
	}

	public int getNbIterations() {
		return nbIterations;
	}

	public void setNbIterations(int nbIterations) {
		this.nbIterations = nbIterations;
	}

	public int getNbTasks() {
		return nbTasks;
	}

	public void setNbTasks(int nbTasks) {
		this.nbTasks = nbTasks;
	}
}
