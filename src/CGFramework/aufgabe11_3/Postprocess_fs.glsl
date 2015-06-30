#version 150

#define GAMMA_CORRECTION_VALUE 2.2

in vec2 vTextureCoords;
out vec4 FragColor;

uniform sampler2D uTexture;

vec4 gammaCorrection(vec4 color) {
    vec4 newColor;

    newColor.rgb = pow(color.rgb, vec3(1.0 / GAMMA_CORRECTION_VALUE));
    newColor.a = color.a;

    return newColor;
}

vec4 exponentialToneMapping(vec4 color, float exposure) {
    vec4 newColor;

    newColor.rgb = 1-exp(-exposure*color.rgb);
    newColor.a = color.a;

    return newColor;
}

void main() {
    vec4 texColor = texture(uTexture,vTextureCoords);
    vec4 exponential = exponentialToneMapping(texColor, 1.0);
    FragColor = exponential;

    FragColor = gammaCorrection(FragColor);
 //  FragColor = texColor;
}