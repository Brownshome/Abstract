package abstractgame.world;

import java.util.List;
import java.util.Map;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import abstractgame.io.model.ModelLoader;
import abstractgame.render.ModelRenderer;
import abstractgame.render.RenderEntity;

public class StaticMapObject extends RenderEntity implements MapObject {
	public static MapObject creator(Map<String, Object> data) {
		String model = (String) data.get("model");
		List<Double> position = (List<Double>) data.get("position");
		List<Double> orientation = (List<Double>) data.get("orientation");
	
		return new StaticMapObject(
				model, 
				new Vector3f(position.get(0).floatValue(), position.get(1).floatValue(), position.get(2).floatValue()),
				new Quat4f(orientation.get(0).floatValue(), orientation.get(1).floatValue(), orientation.get(2).floatValue(), orientation.get(3).floatValue())
		);
	}
	
	public StaticMapObject(String modelName, Vector3f position, Quat4f orientation) {
		super(ModelLoader.loadModel(modelName), position, orientation);
	}
	
	@Override
	public void addToWorld(World world) {
		ModelRenderer.addDynamicModel(this);
	}
}
