#version 440

layout(location = 0) in vec3 in_position;
layout(location = 1) in vec4 in_colour;
layout(location = 2) in uint in_id;

layout(location = 0) out vec4 out_colour;
layout(location = 1) flat out uint out_id;

void main() {
	out_id = in_id;
	gl_Position = vec4(in_position, 1);
	out_colour = in_colour;
}