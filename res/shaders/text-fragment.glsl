#version 430 core

layout(binding = 0) uniform sampler2D img;

layout(location = 0) out vec4 colour;

layout(location = 0) in vec3 tex;
layout(location = 1) in vec4 in_colour;
layout(location = 2) in float inv_size;

const float cutoff = 0.525;
const float blur = 0.005;

void main() {
	float blur_calc = blur * inv_size;
	float alpha = texture(img, tex.xy).r;
	colour = in_colour;
	colour.a = smoothstep(cutoff + blur_calc, cutoff - blur_calc, alpha);
}