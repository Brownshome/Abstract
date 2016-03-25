package abstractgame.ui.elements;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.vecmath.Vector2f;

import abstractgame.world.entity.Player;
import abstractgame.world.entity.playermodules.UpgradeModule;

/** Organizes the modules on the HUD */
public class ModuleHUDDisplay extends UIElement {
	List<ModuleElement> modules;
	
	public ModuleHUDDisplay(float width, Player player) {
		modules = player.getLoadout().stream().map(UpgradeModule::getHUDDisplay).collect(Collectors.toList());
		
		float x = 1 - width - UIElement.INSET_DIST_X;
		float y = -1 + UIElement.INSET_DIST_Y;
		
		width -= UIElement.INSET_DIST_X * 2;
		float height = width / UIElement.HEX_ASPECT;
		
		float totalHeight = modules.size() * (height + UIElement.INSET_DIST_Y) + UIElement.INSET_DIST_Y;
		if(totalHeight > 2) {
			height = (2 - (modules.size() + 1) * UIElement.INSET_DIST_Y) / modules.size();
			width = height * UIElement.HEX_ASPECT + UIElement.INSET_DIST_X * 2;
		}
		
		for(ModuleElement m : modules) {
			m.setDims(new Vector2f(x, y), new Vector2f(x + width, y + height), 0);
			y += height;
		}
	}
	
	@Override
	public void tick() {
		for(ModuleElement m : modules)
			m.tick();
	}
	
	@Override
	public void setID(int ID) {
		modules.forEach(m -> m.setID(ID));
	}

	@Override
	public int getLinesLength() {
		int acc = 0;
		for(ModuleElement e : modules) {
			acc += e.getLinesLength();
		}
		return acc;
	}
	
	@Override
	public void fillLines(FloatBuffer buffer) {
		for(ModuleElement e : modules) {
			e.fillLines(buffer);
		}
	}
	
	@Override
	public int getTrianglesLength() {
		int acc = 0;
		for(ModuleElement e : modules) {
			acc += e.getTrianglesLength();
		}
		return acc;
	}
	
	@Override
	public void fillTriangles(FloatBuffer buffer) {
		for(ModuleElement e : modules) {
			e.fillTriangles(buffer);
		}
	}
}
