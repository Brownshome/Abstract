package abstractgame.time;

import java.util.PriorityQueue;

import org.lwjgl.Sys;

public class Clock {
	static class TimedTask implements Comparable<TimedTask> {
		long time;
		Runnable task;
		
		TimedTask(long time, Runnable task) {
			this.time = time;
			this.task = task;
		}
		
		@Override
		public int compareTo(TimedTask o) {
			return Long.signum(o.time - time);
		}

		@Override
		public int hashCode() {
			return (int) (time ^ (time >>> 32));
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof TimedTask))
				return false;
			
			TimedTask other = (TimedTask) obj;
			
			return time == other.time;
		}
	}
	
	PriorityQueue<TimedTask> tasks = new PriorityQueue<>();
	
	static final float RES = Sys.getTimerResolution();
	
	long frameNo = 0;
	long startTime = getABSTime(1000f);
	long lastFrame = 0;
	long lastDelta = 0;
	
	long lastAverage = 0;
	int tps = -1;
	int count = 0;

	public void startTimer() {
		startTime = getABSTime(1000f);
		frameNo = 0;
	}
	
	/** averaged over 1 seconds (in seconds), this method returns -1 if there has
	 * not been enough time to form an average */
	public float getAverageDelta() {
		return 1f / tps;
	}

	/** This method returns -1 if there has not been enough time to form
	 * an average */
	public int getTPS() {
		return tps;
	}

	public void addClockEvent(Runnable l, long time) {
		tasks.add(new TimedTask(time + getTime(), l));
	}

	public void addABSClockEvent(Runnable l, long time) {
		tasks.add(new TimedTask(time, l));
	}

	public static long getABSTime() {
		return Sys.getTime();
	}
	
	/** The time in 1 / resoloution of a second (Sys) */
	public static long getABSTime(float resoloution) {
		return (long) (getABSTime() * (resoloution / Sys.getTimerResolution()));
	}

	/** The time since started, in ms */
	public long getTime() {
		return getABSTime(1000f) - startTime;
	}

	/** Returns the time of the last tick in millis */
	public long getLastTick() {
		return lastFrame;
	}
	
	public void tick() {
		long time = getTime();
		
		frameNo++;
		count++;
		
		while(tasks.peek() != null && tasks.peek().time < time) {
			tasks.poll().task.run();
		}
		
		lastDelta = time - lastFrame;
		lastFrame = time;
		
		if(time - lastAverage > RES) {
			lastAverage = lastFrame;
			tps = count;
			count = 0;
		}
	}

	public long getTickNo() {
		return frameNo;
	}

	/** returns the last frame delta in seconds, this returns 0 for the first
	 * tick */
	public float getDelta() {
		return lastDelta / RES;
	}
}