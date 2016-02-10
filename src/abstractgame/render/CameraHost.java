package abstractgame.render;

import abstractgame.world.entity.Entity;

public interface CameraHost extends Entity {
	public default void setIsCamera(boolean isCamera) {
		if(isCamera != (Camera.host == this)) {
			if(isCamera) {
				Camera.setCameraHost(this);
			} else {
				Camera.setCameraHost(null);
			}
		}
	}
	
	void onCameraUnset();
}
