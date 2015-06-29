#version 150
#extension GL_ARB_explicit_attrib_location : enable

layout(location=0) in vec3 aPosition;
layout(location=1) in vec3 aNormal;
layout(location=2) in vec2 textureCoords;

uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;
uniform mat4 uNormalMat;
uniform mat4 uInvertedUView;
uniform int numberOfLights;

out vec3 uPosition;
out vec3 N;
out vec3 V;

uniform vec3 uLightPos;
uniform float uLightRange;
out vec3 L;
out float attenuation;

uniform sampler2D uHeight;
uniform sampler2D uTexNormals;

out vec2 vTextureCoords;
out vec3 localPosition;
out vec3 localNormal;

float attenuationOfLight(vec3 vPos, vec4 lightPos, float lightStartDist, float lightEndDist) {
    float distance = length(vPos-lightPos.xyz);
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
    vec4 heightColor = texture(uHeight, textureCoords);
    vec4 uTexNormalsColor = texture(uTexNormals, textureCoords);

    N = normalize( mat3(uNormalMat) * uTexNormalsColor.xyz );

    localNormal = uTexNormalsColor.xyz;

    vec3 position =  aPosition + ( (normalize(aNormal) * heightColor.x) );  // transformed position (with height.jpg x-coord)
    localPosition = position;
    vec4 worldPosition = uModel * vec4(position, 1.0);

    uPosition = worldPosition.xyz;

    vTextureCoords = textureCoords;

    V = normalize( (uInvertedUView * vec4(0.0,0.0,0.0,1.0)).xyz - worldPosition.xyz );

    vec4 lightWorldPosition;

    lightWorldPosition =  uModel * vec4(uLightPos,1.0);
    L = normalize(lightWorldPosition.xyz - worldPosition.xyz);
    attenuation = attenuationOfLight(worldPosition.xyz, lightWorldPosition, 0 , uLightRange );


    gl_Position = uProjection * uView * worldPosition;
}