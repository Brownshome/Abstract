#version 430 core

layout(binding = 0) uniform sampler2DArray​ img;

layout(location = 0) out vec4 colour;

layout(location = 0) in vec3 tex;
layout(location = 1) in vec4 in_colour;

const float cutoff = 0.5;
const float blur = 12;

void main() {
	vec2 deltaX = dFdx(tex.xy);
	vec2 deltaY = dFdy(tex.xy);
	
	float size = length(deltaX) + length(deltaY);
	
	float alpha = texture(img, tex).r;
	colour = in_colour;
	colour.a = smoothstep(cutoff + blur * size, cutoff - blur * size, alpha);
}