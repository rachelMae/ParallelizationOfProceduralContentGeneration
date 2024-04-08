#version 330 core

in vec3 pos;
in vec3 color;
in vec3 normal;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec3 exColor;
out vec3 exNormal;
out vec3 exPos; // world position

void main() {
    gl_Position = projection * view * model * vec4(pos, 1.0);
    exColor = normalize(color);
    exNormal = normal;
    exPos = vec3(model * vec4(pos, 1.0)); // world position
}