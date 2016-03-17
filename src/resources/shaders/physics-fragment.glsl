#version 430

layout(location = 1) uniform vec3 colour;

layout(location = 0) out vec4 out_colour;

void main() {
	out_colour = vec4(colour, 1);
}