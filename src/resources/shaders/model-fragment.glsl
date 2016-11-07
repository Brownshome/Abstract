#version 440 core

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
	int nextElement; //this is the next element if not going down a level. This value will equal -1 for the base patches.
};

layout(binding = 0) buffer PatchData {
	Patch patches[];
};

//taken from http://http.developer.nvidia.com/GPUGems2/gpugems2_chapter14.html
void main() {
	int next = 0;
	float shadow = 0;
	
	while(next < patches.length()) {
		//calculate ray
		vec3 ray = patches[next].position - in_position;
		
		//check if traversal is needed
		float rSquared = dot(ray, ray) + 1e-16;
		
		if(patches[next].nextElement != -1 && rSquared < 4 * patches[next].area) {
			//traverse one step lower in the tree
			next++;
		} else {
			//calculate shadow if traversal is not needed
			ray *= inversesqrt(rSquared);
			shadow += (1 - inversesqrt(patches[next].area / rSquared + 1)) *
				clamp(dot(patches[next].normal, ray), 0, 1) *
				clamp(4 * dot(in_normal, ray), 0, 1);
				
			next = max(next + 1, patches[next].nextElement);
		}
	}
	
	out_colour.rgb = vec3(0.8, 0.2, 0) * clamp(1 - shadow, 0, 1);
}