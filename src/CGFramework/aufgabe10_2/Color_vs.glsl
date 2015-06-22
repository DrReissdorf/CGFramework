#version 150
#extension GL_ARB_explicit_attrib_location : enable

#define LIGHTS 1

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

uniform vec3[LIGHTS] uLightPosArray;
uniform float[LIGHTS] uLightRange;
out vec3[LIGHTS] L;
out float[LIGHTS] attenuationArray;

out vec2 vTextureCoords;

uniform mat4 uLightProjection;
uniform mat4 uLightView;
out vec4 vShadow;

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
    vShadow = uLightProjection * uLightView * vec4(aPosition,1.0);

    vTextureCoords = textureCoords;

    vec4 worldPosition = uModel * vec4(aPosition,1.0);
  //  vShadow = uLightProjection * uLightView * worldPosition;


    N = normalize( aNormal );
    V = normalize( (uInvertedUView * vec4(0.0,0.0,0.0,1.0)).xyz - worldPosition.xyz );

    int i;
    float lightEndDist;
    vec3 lightWorldPosition;

    for(i=0 ; i<L.length() ; i++) {
        lightWorldPosition =  uLightPosArray[i];
        L[i] = normalize(lightWorldPosition - worldPosition.xyz);
        lightEndDist = uLightRange[i];
        attenuationArray[i] = attenuationOfLight(worldPosition.xyz, lightWorldPosition, 0 , uLightRange[i] );
    }

    gl_Position = uProjection * uView * worldPosition;
}