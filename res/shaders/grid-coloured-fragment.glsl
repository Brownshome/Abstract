#version 430

layout(location = 1) uniform vec3 view;
layout(location = 2) uniform vec3 light;

layout(location = 0) in vec4 in_spec_colour;
layout(location = 1) in vec4 in_diffuse_colour;
layout(location = 2) in vec3 in_normal;

layout(location = 0) out vec4 out_colour;

void main() {
	out_colour = in_diffuse_colour;
}