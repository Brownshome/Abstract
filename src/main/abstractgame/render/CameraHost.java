package abstractgame.render;

import javax.vecmath.Vector3f;

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
	
	default Vector3f getOffest() {
		return new Vector3f();
	}
	
	void onCameraUnset();
}
