#version 150

in vec3 uPosition;
in vec3 N;
in vec3 V;
in vec2 vTextureCoords;

/*** LIGHTS ***/
in vec3[6] L;
in float[6] attenuationArray;
uniform vec3[6] uLightPosArray;
uniform vec3[6] uLightColorArray;
uniform float[6] uLightRange;
/**************/
uniform sampler2D uTexture;
uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uNormalMat;
uniform mat4 uInvertedUView;
uniform mat4 uLightMat;

uniform float uShininess;
uniform float uReflectivity; //how much the model is gonna reflect

out vec4 FragColor;

vec3 calculateDiffuse(vec3 N, vec3 L, vec3 lightColor, float nDotl) {
    vec3 diffuseLighting = lightColor * vec3(max(nDotl, 0.0)) ;
    return diffuseLighting;
}

vec3 calculateSpecularBlinn(vec3 N, vec3 V, vec3 L, vec3 lightColor, float nDotl, float ambilight) {
    vec3 specular = vec3(0,0,0);
    if(nDotl > ambilight) {
        vec3 lightAddCam = L+V;
        vec3 H = normalize( lightAddCam/sqrt(dot(lightAddCam,lightAddCam)) );
        specular =  lightColor * uReflectivity * vec3(max(pow(dot(N, H), uShininess+100), 0.0));
    }
    return specular;
}

void main(void) {
    float ambilight = 0.05;
    float lightStartDist = 0;

    vec3 diffuseFinal = vec3(0,0,0);
    vec3 specularFinal = vec3(0,0,0);

    int i;
    for(i=0 ; i<L.length() ; i++) {
        float nDotl = dot(N,L[i]);
        float lightEndDist = uLightRange[i];
        float lightIntense = attenuationArray[i];

        vec3 diffuse = calculateDiffuse(N, L[i], uLightColorArray[i],nDotl);
        vec3 specular = calculateSpecularBlinn(N, V, L[i], uLightColorArray[i], nDotl, ambilight);

        diffuseFinal += diffuse*lightIntense;
        specularFinal += specular*lightIntense;
    }
    diffuseFinal = max(diffuseFinal,ambilight);
    specularFinal = max(specularFinal,ambilight);

    vec4 textureColor = texture(uTexture,vTextureCoords);
    FragColor = vec4(diffuseFinal, 1.0) * textureColor + vec4(specularFinal, 1.0);
   // FragColor = vec4(diffuseFinal, 1.0)+ vec4(specularFinal, 1.0);
}