package abstractgame.io.model;

import abstractgame.util.ApplicationException;

public class Face {
	int v1, v2, v3;
	int n1, n2, n3;
	
	public Face(String[] points, int nv, int nn) {
		if(points.length != 4)
			throw new ApplicationException("Faces other than triangles are not supported", "MODEL IO");
		
		String[] indices = points[1].split("/");
		switch(indices.length) {
			case 0:
				throw new ApplicationException(new ArrayIndexOutOfBoundsException(), "MODEL IO");
			case 3:
				if(!indices[2].isEmpty())
					n1 = Integer.parseInt(indices[2]);
			case 2:
			case 1:
				if(!indices[0].isEmpty())
					v1 = Integer.parseInt(indices[0]);
				break;
			default:
				throw new ApplicationException("Malformed face line", "MODEL IO");
		}
		
		indices = points[2].split("/");
		switch(indices.length) {
			case 0:
				throw new ApplicationException(new ArrayIndexOutOfBoundsException(), "MODEL IO");
			case 3:
				if(!indices[2].isEmpty())
					n2 = Integer.parseInt(indices[2]);
			case 2:
			case 1:
				if(!indices[0].isEmpty())
					v2 = Integer.parseInt(indices[0]);
				break;
			default:
				throw new ApplicationException("Malformed face line", "MODEL IO");
		}
		
		indices = points[3].split("/");
		switch(indices.length) {
			case 0:
				throw new ApplicationException(new ArrayIndexOutOfBoundsException(), "MODEL IO");
			case 3:
				if(!indices[2].isEmpty())
					n3 = Integer.parseInt(indices[2]);
			case 2:
			case 1:
				if(!indices[0].isEmpty())
					v3 = Integer.parseInt(indices[0]);
				break;
			default:
				throw new ApplicationException("Malformed face line", "MODEL IO");
		}
		
		if(n1 < 0) n1 += nn + 1;
		if(n2 < 0) n2 += nn + 1;
		if(n3 < 0) n3 += nn + 1;
		
		if(v1 < 0) v1 += nv + 1;
		if(v2 < 0) v2 += nv + 1;
		if(v3 < 0) v3 += nv + 1;
	}
}