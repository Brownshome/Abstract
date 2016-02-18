#version 440

layout(location = 0) in vec2 in_position;
layout(location = 1) in vec4 in_colour;
layout(location = 2) in vec3 in_textureCoord;

layout(location = 0) out vec3 tex;
layout(location = 1) out vec4 out_colour;

void main() {
	tex = in_textureCoord;
	out_colour = in_colour;
	gl_Position = vec4(in_position, 0, 1);
}