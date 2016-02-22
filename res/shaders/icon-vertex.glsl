#version 440 core

layout(location = 0) in vec2 in_position;
layout(location = 1) in vec4 in_colour;
layout(location = 2) in vec3 in_textureCoord;
layout(location = 3) in uint in_id;

layout(location = 0) out vec3 out_textureCoord;
layout(location = 1) out vec4 out_colour;
layout(location = 2) flat out uint out_id;

void main() {
	out_id = in_id;
	out_textureCoord = in_textureCoord;
	out_colour = in_colour;
	gl_Position = vec4(in_position, 0, 1);
}