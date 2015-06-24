#version 150

#define LIGHTS 1
#define BLINN_ADD_SHINE 100
#define SHADOW_BIAS 0.0004

in vec3 N;
in vec3 V;
uniform mat4 uModel;

/***************** LIGHTS *************/
in vec3[LIGHTS] L;
in float[LIGHTS] attenuationArray;
uniform vec3[LIGHTS] uLightPosArray;
uniform vec3[LIGHTS] uLightColorArray;
uniform float[LIGHTS] uLightRange;
uniform float uShininess;
uniform float uReflectivity;
/**************************************/

/********** SHADOW ********************/
in vec4 vShadow;
uniform sampler2DShadow uShadowmap;
/**************************************/

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
        specular =  lightColor * uReflectivity * vec3(max(pow(dot(N, H), uShininess+BLINN_ADD_SHINE), 0.0));
    }
    return specular;
}

float getAttenuation( vec4 shadowmapCoord ) {
    vec3 coord3 = 0.5 + 0.5 * vShadow.xyz / vShadow.w;
    coord3.z -= SHADOW_BIAS;
    float shadowmap_factor = texture(uShadowmap, coord3);
    return shadowmap_factor;
}

void main(void) {
    float ambilight = 0.1;
    float lightStartDist = 0;

    /************** DIFFUSE AND SPECULAR CALCULATION ************************/
    vec3 diffuseFinal = vec3(0,0,0);
    vec3 specularFinal = vec3(0,0,0);
    int i;
    for(i=0 ; i<LIGHTS ; i++) {
        float nDotl = dot(N,L[i]);
        float lightEndDist = uLightRange[i];
        float lightIntense = attenuationArray[i];

        vec3 diffuse = calculateDiffuse(N, L[i], uLightColorArray[i],nDotl);
        vec3 specular = calculateSpecularBlinn(N, V, L[i], uLightColorArray[i], nDotl, ambilight);

        diffuseFinal += diffuse*lightIntense;
        specularFinal += specular*lightIntense;
    }
    diffuseFinal = max(diffuseFinal,0);
    specularFinal = max(specularFinal,0);
    /*************************************************************************/

    float shadowFactor = getAttenuation(vShadow);

    FragColor =  ambilight + ( vec4(diffuseFinal, 1.0) + vec4(specularFinal, 1.0) )*shadowFactor;
}