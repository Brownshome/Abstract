
package abstractgame.render;

public interface Renderer {
	void initialize();
	void render();
	float getPass();
	default void onAspectChange() {}
}
