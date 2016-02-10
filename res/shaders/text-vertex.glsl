#version 430 core

layout(location = 0) uniform int rows;
layout(location = 1) uniform int columns;

layout(location = 0) in int image;
layout(location = 1) in int character;
layout(location = 2) in vec2 position;
layout(location = 3) in vec4 colour;
layout(location = 4) in float size;

layout(location = 0) out vec3 texturePos;
layout(location = 1) out vec4 out_colour;
layout(location = 2) out float out_size;

void main() {
	gl_Position = vec4(position, 0, 1);
	texturePos = vec3(character % columns, character / columns, image);
	texturePos.x /= columns;
	texturePos.y /= rows;
	out_colour = colour;
	out_size = size;
}