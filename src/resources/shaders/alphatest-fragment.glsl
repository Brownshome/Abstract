#version 440

layout(binding = 0) uniform sampler2DArray img;

layout(location = 0) out vec4 colour;
layout(location = 1) out vec4 out_id;

layout(location = 0) in vec3 tex;
layout(location = 1) in vec4 in_colour;
layout(location = 2) flat in uint in_id;

const float cutoff = 0.5;
const float blur = 10;

void main() {
	vec2 deltaX = dFdx(tex.xy);
	vec2 deltaY = dFdy(tex.xy);
	
	float size = length(deltaX) + length(deltaY);
	
	float alpha = texture(img, tex).r;
	
    out_id.r = uintBitsToFloat(in_id);
    out_id.a = alpha < 0.5 ? 1 : 0;
	
	colour = in_colour;
	colour.a = smoothstep(cutoff + blur * size, cutoff - blur * size, alpha);
}