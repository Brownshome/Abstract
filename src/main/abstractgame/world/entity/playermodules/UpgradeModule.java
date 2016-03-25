package abstractgame.world.entity.playermodules;

import abstractgame.Server;
import abstractgame.ui.elements.ModuleElement;
import abstractgame.ui.elements.ModuleElement.State;
import abstractgame.world.entity.Player;

public abstract class UpgradeModule {
	public final Player owner;
	
	ModuleElement hudElement;
	private final Customization[] customizations;
	
	public UpgradeModule(Player owner, Customization... customizations) {
		this.owner = owner;
		this.customizations = customizations;
	}
	
	public ModuleElement getHUDDisplay() {
		return hudElement = new ModuleElement(this);
	}
	
	public State getState() {
		return State.PASSIVE;
	}
	
	public String getImage() {
		return "module/default";
	}
	
	public abstract String getDescription();
	
	public Customization[] getCustomizations() {
		return customizations;
	}
}
