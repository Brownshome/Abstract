#version 440

layout(location = 0) in vec4 in_colour;
layout(location = 1) flat in uint in_id;

layout(location = 0) out vec4 out_colour;
layout(location = 1) out uint out_id;

void main() {
	out_colour = in_colour;
	out_id = in_id;
}