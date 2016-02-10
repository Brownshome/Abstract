#version 430 core

layout(points) in;
layout(triangle_strip, max_vertices = 4) out;

layout(location = 0) uniform int rows;
layout(location = 1) uniform int columns;
layout(location = 2) uniform float correction;

layout(location = 0) in vec3[] texturePosition;
layout(location = 1) in vec4[] colour;
layout(location = 2) in float[] size;

layout(location = 0) out vec3 out_tex;
layout(location = 1) out vec4 out_colour;
layout(location = 2) out float out_inv_size;

void main() {
	vec2 tmp = vec2(1.0 / columns, 1.0 / rows);
	float inv_size = 1.0 / size[0];
	
	gl_Position = gl_in[0].gl_Position;
	out_tex = texturePosition[0];
	out_tex.y += tmp.y;
	out_colour = colour[0];
	out_inv_size = inv_size;
	EmitVertex();
	
	gl_Position = gl_in[0].gl_Position + vec4(size[0] * correction, 0, 0, 0);
	out_tex = texturePosition[0];
	out_tex.xy += tmp;
	out_colour = colour[0];
	out_inv_size = inv_size;
	EmitVertex();
	
	gl_Position = gl_in[0].gl_Position + vec4(0, size[0], 0, 0);
	out_tex = texturePosition[0];
	out_colour = colour[0];
	out_inv_size = inv_size;
	EmitVertex();
	
	gl_Position = gl_in[0].gl_Position + vec4(size[0] * correction, size[0], 0, 0);
	out_tex = texturePosition[0];
	out_tex.x += tmp.x;
	out_colour = colour[0];
	out_inv_size = inv_size;
	EmitVertex();
	EndPrimitive();
}