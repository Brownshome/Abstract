#version 440

layout(location = 0) in vec3 in_normal;

layout(location = 0) out vec4 out_colour;

void main() {
	out_colour.rgb = vec3(1, 0.8, 0) * (abs(dot(in_normal, normalize(vec3(0.5, 2, 1)))) * 0.5 + 0.5);
	out_colour.a = 1.0;
}