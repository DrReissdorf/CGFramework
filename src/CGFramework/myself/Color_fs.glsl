#version 150

#define LIGHTS 1
#define BLINN_ADD_SHINE 100
#define SHADOW_BIAS 0.0004
#define AMBILIGHT 0.15
#define AMBILIGHT_MULT AMBILIGHT*3
#define PCF_SAMPLES 1

in vec3 N;
in vec3 V;
uniform mat4 uModel;

/************** TEXTURE ***************/
uniform sampler2D uTexture;
in vec2 vTextureCoords;
/**************************************/

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

vec3 calculateSpecularBlinn(vec3 N, vec3 V, vec3 L, vec3 lightColor, float nDotl) {
    vec3 specular = vec3(0,0,0);
    if(nDotl > AMBILIGHT) {
        vec3 lightAddCam = L+V;
        vec3 H = normalize( lightAddCam/sqrt(dot(lightAddCam,lightAddCam)) );
        specular =  lightColor * uReflectivity * vec3(max(pow(dot(N, H), uShininess+BLINN_ADD_SHINE), 0.0));
    }
    return specular;
}

float getAttenuationPCF( vec4 shadowmapCoord ) {
    vec3 ProjCoords = shadowmapCoord.xyz / shadowmapCoord.w;
    vec2 UVCoords;
    UVCoords.x = 0.5 * ProjCoords.x + 0.5;
    UVCoords.y = 0.5 * ProjCoords.y + 0.5;
    float z = 0.5 * ProjCoords.z + 0.5;

    /************ get texturesize *************/
    ivec2 texSize = textureSize(uShadowmap,0);
    float offset = 1.0/float(texSize.x);
    /******************************************/

    float shadowmap_factor = 0.0;
    float numberOfSamples = 0;
    for (int y = -PCF_SAMPLES ; y <= PCF_SAMPLES ; y++) {
        for (int x = -PCF_SAMPLES ; x <= PCF_SAMPLES ; x++) {

            //calculate offstes with size of texels
            vec2 Offsets = vec2(x * offset, y * offset);

            //add offsets to coordinates
            vec3 UVC = vec3(UVCoords + Offsets, z - SHADOW_BIAS);

            // add combined values to factor
            shadowmap_factor += texture(uShadowmap, UVC);
            numberOfSamples++;
        }
    }

    // divide factor by numberOfSamples and return it
    return shadowmap_factor/numberOfSamples;
}


void main(void) {
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
        vec3 specular = calculateSpecularBlinn(N, V, L[i], uLightColorArray[i], nDotl);

        diffuseFinal += diffuse*lightIntense;
        specularFinal += specular*lightIntense;
    }
    diffuseFinal = max(diffuseFinal,AMBILIGHT_MULT);
    specularFinal = max(specularFinal,0);
    /*************************************************************************/

    vec4 textureColor = texture(uTexture,vTextureCoords);
    float shadowFactor = getAttenuationPCF(vShadow);

    FragColor = vec4(diffuseFinal, 1.0)*(textureColor+AMBILIGHT)*(shadowFactor + AMBILIGHT_MULT) + vec4(specularFinal, 1.0)*shadowFactor;
}