#version 150
#extension GL_ARB_explicit_attrib_location : enable

layout(location=0) in vec3 aPosition;
layout(location=1) in vec3 aNormal;
layout(location=2) in vec2 textureCoords;

uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;
uniform mat4 uNormalMat;
uniform mat4 uLightMat;
uniform mat4 uInvertedUView;
uniform int numberOfLights;

out vec3 uPosition;
out vec3 N;
out vec3 V;

uniform vec3 uLightPos;
uniform float uLightRange;
out vec3 L;
out float attenuation;

out vec2 vTextureCoords;

float attenuationOfLight(vec3 vPos, vec3 lightPos, float lightStartDist, float lightEndDist) {
    float distance = length(vPos-lightPos);
    float lightIntense;
    if(distance <= lightStartDist) {   //max helligkeit
        lightIntense = 1;
    } else if(distance >= lightEndDist) {
        lightIntense = 0;
    } else {
        lightIntense = max( (lightEndDist-distance)/(lightEndDist-lightStartDist), 0.0 );
    }
    return lightIntense;
}

void main(void) {
    uPosition = aPosition;
    vTextureCoords = textureCoords;

    vec4 worldPosition = vec4(aPosition,1.0);

    N = normalize( aNormal );
    V = normalize( (uInvertedUView * vec4(0.0,0.0,0.0,1.0)).xyz - worldPosition.xyz );


    vec3 lightWorldPosition = uLightPos;
    L = normalize(lightWorldPosition - worldPosition.xyz);
    float lightEndDist = uLightRange;
    attenuation = attenuationOfLight(worldPosition.xyz, lightWorldPosition, 0 , uLightRange );

    gl_Position = uProjection * uView * worldPosition;
}