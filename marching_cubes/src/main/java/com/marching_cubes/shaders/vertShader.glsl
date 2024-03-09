#version 330 core

in vec3 pos;
in vec3 color;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec3 exColor;

void main() {
    gl_Position = projection * view * model * vec4(pos, 1.0);
    exColor = color;
}