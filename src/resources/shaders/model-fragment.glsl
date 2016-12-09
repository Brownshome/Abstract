#version 450 core

layout(location = 0) in vec3 in_position;
layout(location = 1) in vec3 in_normal;

layout(location = 0) out vec3 out_colour;

//DO NOT RE-NAME THESE VARIABLES, BAD THINGS WILL HAPPEN
struct Patch {
	vec3 position;
	float area;
	vec3 normal;
	float brightness;
	vec3 illuminationDirection;
	int nextElement; //this is the next element if not going down a level. This value will be [0, -inf), the representative triangle is found at triangles[-nextElement]
	int layer; //max of MAX_LAYERS - 1. 0 is the coarsest patch.
};

struct Triangle {
	vec3 a;
	vec3 b;
	vec3 c;
};

layout(binding = 0) readonly buffer PatchData {
	Patch patches[];
};

layout(binding = 1) readonly buffer TriangleData {
	Triangle triangles[];
};

const int MAX_LAYERS = 16; //increase this if needed.
const float TOO_CLOSE = 3.0;
const float BAND = 0.6;
const float EPSILON = 1e-10;
const float SOLID_ANGLE_MULT = 3.14159 * 2;

float patchOcculsion(int index, float rSquared, vec3 ray) {
	return (1 - inversesqrt(patches[index].area / rSquared + 1)) *
				clamp(dot(patches[index].normal, ray), 0, 1) *
				clamp(4 * dot(in_normal, ray), 0, 1);
}

void clipTriangle(int triangle, out vec3 q1, out vec3 q2, out vec3 q3, out vec3 q4) {
	vec3 x = triangles[triangle].a;
	vec3 y = triangles[triangle].c;
	vec3 z = triangles[triangle].b;

	float planeConstant = dot(in_position, in_normal);
	vec3 signedDistance = vec3(dot(x, in_normal), dot(y, in_normal), dot(z, in_normal)) - vec3(planeConstant);

	//if the signed distance is close to zero, move it up or down to compensate and avoid div/0
	signedDistance = signedDistance * mix(vec3(1), vec3(2), lessThan(abs(signedDistance), vec3(EPSILON)));

	vec3 xy = x + (signedDistance.x / (signedDistance.x - signedDistance.y)) * (y - x);
	vec3 xz = x + (signedDistance.x / (signedDistance.x - signedDistance.z)) * (z - x);
	vec3 yz = y + (signedDistance.y / (signedDistance.y - signedDistance.z)) * (z - y);

	//There is probably a better way to do this...
	//care must be taken to ensure the winding order is correct for the occulsion calculations to work
	if(signedDistance.x < 0) {
		if(signedDistance.y < 0) {
			if(signedDistance.z < 0) {
				//output x, x, x
				q1 = x;
				q2 = x;
				q3 = x;
				q4 = x;
			} else {
				//output xz, yz, z, z
				q1 = xz;
				q2 = yz;
				q3 = z;
				q4 = z;
			}
		} else {
			if(signedDistance.z < 0) {
				//output yz, yx, y, y
				q1 = xy;
				q2 = y;
				q3 = y;
				q4 = yz;
			} else {
				//output y, z, xy, xz
				q1 = xy;
				q2 = y;
				q3 = z;
				q4 = xz;
			}
		}
	} else {
		if(signedDistance.y < 0) {
			if(signedDistance.z < 0) {
				//output x, xy, xz, xz
				q1 = x;
				q2 = xy;
				q3 = xz;
				q4 = xz;
			} else {
				//output x, z, xy, zy
				q1 = x;
				q2 = xy;
				q3 = yz;
				q4 = z;
			}
		} else {
			if(signedDistance.z < 0) {
				//output x, y, xz, yz
				q1 = x;
				q2 = y;
				q3 = yz;
				q4 = xz;
			} else {
				//output x, y, z, z
				q1 = x;
				q2 = y;
				q3 = z;
				q4 = z;
			}
		}
	}
}

float trippleProduct(vec3 a, vec3 b, vec3 c) {
	return dot(a, cross(b, c));
}

//Creates the characteristic complex number for the triangle defined by these
//three points.
vec2 getTriangleNumber(vec3 p1, vec3 p2, vec3 p3) {
	float a = dot(p1, p2);
	float b = dot(p1, p3);
	float c = dot(p2, p3);
	float d = trippleProduct(p1, p2, p3);

	return vec2(1.0 + a + b + c, d);
}

vec2 mult(vec2 a, vec2 b) {
	float y = dot(a, b.yx);
	a.y = -a.y;
	float x = dot(a, b);
	return vec2(x, y);
}

float triangleOcculsion(int triangle) {
	vec3 q1, q2, q3, q4;
	clipTriangle(triangle, q1, q2, q3, q4);

	q1 -= in_position;
	q2 -= in_position;
	q3 -= in_position;
	q4 -= in_position;

	q1 = normalize(q1);
	q2 = normalize(q2);
	q3 = normalize(q3);
	q4 = normalize(q4);

	vec2 m = mult(getTriangleNumber(q4, q1, q2), getTriangleNumber(q4, q2, q3));

	//TODO find out which segments of the atan function are valid...
	float solid_angle = atan(m.y, m.x);

	return clamp(solid_angle / SOLID_ANGLE_MULT, 0, 1);
}

//taken from http://http.developer.nvidia.com/GPUGems2/gpugems2_chapter14.html
void main() {
	int next = 0;
	float shadow = 0;
	
	//patchWeights[N] is the weight that should be multiplied by layer N.
	float[MAX_LAYERS] patchWeights = float[MAX_LAYERS](1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
	
	while(next < patches.length()) {
		if(patches[next].nextElement <= 0) {
			//base element, use triangle summation
			shadow += triangleOcculsion(-patches[next].nextElement) * patchWeights[patches[next].layer];
			next++;
		} else {
			//patch based occulsion

			//calculate ray
			vec3 ray = patches[next].position - in_position;
		
			//check if traversal is needed
			float rSquared = dot(ray, ray) + EPSILON;
			ray *= inversesqrt(rSquared);
		
			float contribution = patchOcculsion(next, rSquared, ray);

			if(patches[next].nextElement != next + 1 && rSquared < TOO_CLOSE * patches[next].area) {
				//traverse one step lower in the tree
				//A value of 1 means that the parent is not considered at all.
				float childWeighting = clamp((TOO_CLOSE * patches[next].area - rSquared) / (TOO_CLOSE * BAND * patches[next].area), 0, 1);
				shadow += contribution * patchWeights[patches[next].layer] * (1 - childWeighting);
				patchWeights[patches[next].layer + 1] = patchWeights[patches[next].layer] * childWeighting;
				next++;
			} else {
				//calculate shadow if traversal is not needed
				shadow += contribution * patchWeights[patches[next].layer];
				next = patches[next].nextElement;
			}
		}
	}
	
	out_colour.rgb = vec3(0.8, 0.2, 0) * clamp(1 - shadow, 0, 1);
}
