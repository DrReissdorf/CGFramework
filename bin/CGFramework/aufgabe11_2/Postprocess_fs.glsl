#version 150

in vec2 vTextureCoords;
out vec4 FragColor;

uniform sampler2D uTexture;

void main() {
    FragColor = texture(uTexture,vTextureCoords);
}