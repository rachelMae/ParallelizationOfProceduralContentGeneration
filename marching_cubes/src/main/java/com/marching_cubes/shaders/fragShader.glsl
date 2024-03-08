#version 330 core

in vec3 exColor;
out vec4 fragColor;

void main() {
    // fragColor = vec4(1.0, 0.0, 0.0, 1.0);
    fragColor = vec4(exColor, 1.0);
}