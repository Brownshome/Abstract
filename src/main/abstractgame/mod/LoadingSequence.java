package abstractgame.mod;

import java.util.ArrayList;

import abstractgame.util.ApplicationException;

public class LoadingSequence {
	public static class Pre {
		final String modName;
		final int stageNumber;
		
		public Pre(String modName) {
			this(modName, -1);
		}
		
		public Pre(String modName, int numberOfStages) {
			this.modName = modName;
			this.stageNumber = numberOfStages;
		}
	}

	public static String currentMod;
	
	static class Stage {
		Runnable task;
		String modName;
		Pre[] prerequisites;
		
		Stage(Runnable task, Pre[] prerequisites) {
			this.task = task;
			
			if(LoadingSequence.currentMod == null) {
				throw new ApplicationException("The mod loader is not currently sequencing", "MOD LOADER");
			}
			
			this.prerequisites = prerequisites;
		}
	}
	
	static ArrayList<Stage> stages = new ArrayList<>();
	
	public static void startModLoading() {
		//all the mod jars should already be on the classpath, so we just read all mod classes
		//TODO
	}
	
	public static void addStage(Runnable task, Pre... prerequisites) {
		stages.add(new Stage(task, prerequisites));
	}
}
