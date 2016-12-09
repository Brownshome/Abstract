package abstractgame.io.model;

import javax.vecmath.*;

import abstractgame.util.ApplicationException;

public class IndexedFace {
	RawModel model = null;
	
	int[] position = new int[3];
	int[] normal = new int[3];
	int[] textureCoord = {-1, -1, -1};
	
	public IndexedFace(String[] points, int nv, int nn) {
		if(points.length != 4)
			throw new ApplicationException("Faces other than triangles are not supported", "MODEL IO");
		
		for(int i = 0; i < 3; i++) {
			String[] indices = points[i + 1].split("/");
			switch(indices.length) {
				case 0:
					throw new ApplicationException(new ArrayIndexOutOfBoundsException(), "MODEL IO");
				case 3:
					if(!indices[2].isEmpty())
						normal[i] = Integer.parseInt(indices[2]) - 1;
				case 2:
					if(!indices[1].isEmpty())
						textureCoord[i] = Integer.parseInt(indices[1]) - 1;
				case 1:
					if(!indices[0].isEmpty())
						position[i] = Integer.parseInt(indices[0]) - 1;
					break;
				default:
					throw new ApplicationException("Malformed face line", "MODEL IO");
			}
		}
	}
	
	/** Zero indexed */
	public Vector3f getVertexPosition(int index) {
		return model.vertexs[position[index]];
	}
	
	/** Zero indexed */
	public Vector3f getVertexNormal(int index) {
		return model.normals[normal[index]];
	}
	
	/** Zero indexed */
	public Vector2f getTextureCoord(int index) {
		return model.textureCoordinates[textureCoord[index]];
	}

	public int[] getUVIndexs() {
		return textureCoord;
	}

	public Vector3f[] getTriangle() {
		return new Vector3f[] {getVertexPosition(0), getVertexPosition(1), getVertexPosition(2)};
	}

	public boolean hasTextureCoords() {
		return textureCoord[0] != -1;
	}
}