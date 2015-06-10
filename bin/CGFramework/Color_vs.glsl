#version 150
#extension GL_ARB_explicit_attrib_location : enable

layout(location=0) in vec3 aPosition;
layout(location=1) in vec3 aNormal;
layout(location=2) in vec2 textureCoords;

uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;
uniform mat4 uNormalMat;

out vec3 uPosition;
out vec3 N;
out vec3 V;

out vec2 vTextureCoords;

void main(void) {
    uPosition = aPosition;
    N = normalize( mat3(uNormalMat) * aNormal );
    gl_Position = uProjection * uView * uModel * vec4(aPosition,1.0);
    vTextureCoords = textureCoords;
}