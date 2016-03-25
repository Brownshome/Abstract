package abstractgame.world.entity.playermodules;

import abstractgame.world.entity.Player;

public class Heatsink extends UpgradeModule {
	Slider coolantRate;
	
	int ticksSinceInc = 0;
	float heatLastTick;
	
	public Heatsink(Player owner) {
		super(owner, new Slider("Coolant Rate", "How quickly the module removes heat", 0, 10, 0));
		coolantRate = (Slider) getCustomizations()[0];
		
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
		return "Decreaces your heat level after 5 seconds of not increacing."
			 + " Stacking this module decreaces the wait time and increaces the cooling rate";
	}
}
