#version 440

layout(location = 0) uniform mat4 matrix;

layout(location = 0) in vec3 in_position;
layout(location = 1) in vec3 in_normal;

layout(location = 0) out vec3 out_position;
layout(location = 1) out vec3 out_normal;

void main() {
	gl_Position = matrix * vec4(in_position, 1);
	out_normal = in_normal;
	out_position = in_position; //TODO transform this to world coordinates
}