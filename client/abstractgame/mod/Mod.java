package abstractgame.mod;

public interface Mod {
	/** Use LoadingSequence.addStage in this method, and only in this method */
	public void getLoadingSequence();
}
