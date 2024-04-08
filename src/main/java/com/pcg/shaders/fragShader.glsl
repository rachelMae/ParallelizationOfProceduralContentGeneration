#version 330 core

in vec3 exColor;
in vec3 exNormal;
in vec3 exPos;
out vec4 fragColor;

uniform vec3 lightPos;

void main() {
    float ambientStrength = 0.2;
    vec3 lightColor = vec3(1.0, 1.0, 1.0);
    vec3 ambient = ambientStrength * lightColor;

    // fragColor = vec4(1.0, 0.0, 0.0, 1.0); // red
    vec3 norm = normalize(exNormal);
    vec3 lightDir = normalize(lightPos - exPos);

    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = diff * lightColor;

    vec3 result = (ambient + diffuse) * exColor;
    fragColor = vec4(result, 1.0);

    // fragColor = vec4(exColor, 1.0); // normals
}