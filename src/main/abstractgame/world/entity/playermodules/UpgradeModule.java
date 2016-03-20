package abstractgame.world.entity.playermodules;

import abstractgame.world.entity.Player;

public abstract class UpgradeModule {
	public final Player owner;
	private final Customization[] customizations;
	
	public UpgradeModule(Player owner, Customization... customizations) {
		this.owner = owner;
		this.customizations = customizations;
	}
	
	public String getImage() {
		return "module.default";
	}
	
	public abstract String getDescription();
	
	public Customization[] getCustomizations() {
		return customizations;
	}
}
