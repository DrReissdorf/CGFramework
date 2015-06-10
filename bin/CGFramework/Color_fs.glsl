#version 150

in vec3 uPosition;
in vec3 N;
in vec3 V;

in vec2 vTextureCoords;
uniform sampler2D uTexture;

out vec4 FragColor;

uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uNormalMat;
uniform mat4 uInvertedUView;

uniform vec3[6] uLightPosArray;
uniform vec3[6] uLightColorArray;
uniform float[6] uLightRange;

uniform float uShininess;
uniform float uReflectivity; //how much the model is gonna reflect
uniform vec3 uModelColor;

vec3 calculateDiffuse(vec3 N, vec3 L, vec3 lightColor, float nDotl) {
    vec3 diffuseLighting = lightColor * vec3(max(nDotl, 0.0)) ;
    return diffuseLighting;
}

vec3 calculateSpecularBlinn(vec3 N, vec3 V, vec3 L, vec3 lightColor, float nDotl, float ambilight) {
    vec3 specular = vec3(0,0,0);
    vec3 lightAddCam = L+V;
    vec3 H = normalize( lightAddCam/sqrt(dot(lightAddCam,lightAddCam)) );
    if(nDotl > ambilight)
        specular =  lightColor * uReflectivity * vec3(max(pow(dot(N, H), uShininess+100), 0.0));
    return specular;
}

float attenuationOfLight(vec3 vPos, vec3 lightPos, float lightStartDist, float lightEndDist) {
    float distance = sqrt(pow(vPos.x-lightPos.x,2)+pow(vPos.y-lightPos.y,2)+pow(vPos.z-lightPos.z,2));
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
    float ambilight = 0.05;
    float lightStartDist = 0;
    vec4 worldPosition = uModel * vec4(uPosition,1.0);
    vec3 V = normalize( (uInvertedUView * vec4(0.0,0.0,0.0,1.0)).xyz - worldPosition.xyz );

    vec3 diffuseFinal = vec3(0,0,0);
    vec3 specularFinal = vec3(0,0,0);
    int i;
    for(i=0 ; i<uLightPosArray.length() ; i++) {
        vec3 L = normalize( mat3(uModel) * (uLightPosArray[i] - uPosition) );  // licht bewegt sich mit bei linksklick
       // vec3 L = normalize(uLightPosArray[i] - (mat3(uModel)*uPosition));    // licht bewegt sich nicht mit bei linksklick
        float nDotl = dot(N,L);
        float lightEndDist = uLightRange[i];
        float lightIntense = attenuationOfLight(uPosition,uLightPosArray[i],lightStartDist,lightEndDist);

        vec3 diffuse = calculateDiffuse(N, L, uLightColorArray[i],nDotl);
        vec3 specular = calculateSpecularBlinn(N, V, L, uLightColorArray[i], nDotl, ambilight);

        diffuseFinal += diffuse*lightIntense;
        specularFinal += specular*lightIntense;
    }
    diffuseFinal = max(diffuseFinal,ambilight);
    specularFinal = max(specularFinal,ambilight);

    vec4 textureColor = texture(uTexture,vTextureCoords);
    FragColor = vec4(diffuseFinal, 1.0) * textureColor + vec4(specularFinal, 1.0);


}