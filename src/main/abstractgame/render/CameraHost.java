package abstractgame.render;

import javax.vecmath.Vector3f;

import abstractgame.util.Util;
import abstractgame.world.entity.Entity;

public interface CameraHost extends Entity {
	default void setIsCamera(boolean isCamera) {
		if(isCamera != (Camera.host == this)) {
			if(isCamera) {
				Camera.setCameraHost(this);
			} else {
				Camera.setCameraHost(null);
			}
		}
	}
	
	default Vector3f getOffset() {
		return Util.ZERO_VEC3;
	}
	
	default void onCameraUnset() {}

	default void onCameraSet() {}
}
