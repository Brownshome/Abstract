#version 430

layout(location = 0) uniform mat4 matrix;

layout(location = 0) in vec3 in_position;
layout(location = 1) in vec3 in_normal;
layout(location = 2) in vec4 in_spec_colour;
layout(location = 2) in vec4 in_diffuse_colour;

layout(location = 0) out vec4 out_spec_colour;
layout(location = 1) out vec4 out_diffuse_colour;
layout(location = 2) out vec3 out_normal;

void main() {
	gl_Position = matrix * vec4(in_position, 1);
	out_diffuse_colour = in_diffuse_colour;
	out_spec_colour = in_spec_colour;
	out_normal = in_normal;
	
}