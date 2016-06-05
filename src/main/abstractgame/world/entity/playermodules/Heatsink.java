package abstractgame.world.entity.playermodules;

import abstractgame.util.Language;
import abstractgame.world.entity.Player;

public class Heatsink extends UpgradeModule {
	Slider coolantRate;
	
	int ticksSinceInc = 0;
	float heatLastTick;
	
	public Heatsink(Player owner) {
		super(owner, new Slider(Language.get("module.heatsink.coolant rate.name"), Language.get("module.heatsink.coolant rate.desc"), 0, 10, 0));
		coolantRate = (Slider) getCustomizations()[0];
		
		coolantRate.value = 0.5f;
		
		owner.onHeat(() -> {
			ticksSinceInc = 0;
		});
		
		owner.onTick(() -> {
			if(ticksSinceInc++ >= getCooldown()) {
				activateCooling();
			}
		}); 
	}
	
	private void activateCooling() {
		owner.addHeat(-coolantRate.getValue() * (.01f + owner.getHeat() * .025f));
	}
	
	private int getCooldown() {
		return (int) (50 / (coolantRate.getValue() + 1));
	}

	@Override
	public String getDescription() {
		return Language.get("module.heatsink.desc");
	}
}
