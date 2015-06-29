#version 150

#define AMBILIGHT 0.1
#define LIGHTS 1
#define BLINN_ADD_SHINE 100
#define SHADOW_BIAS 0.002

in vec3 N;
in vec3 V;
uniform mat4 uModel;

/***************** LIGHTS *************/
in vec3 L;
in float attenuation;
uniform vec3 uLightPos;
uniform vec3 uLightColor;
uniform float uLightRange;
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

float getAttenuationPCF( vec4 shadowmapCoord ) {
    vec3 ProjCoords = shadowmapCoord.xyz / shadowmapCoord.w;
    vec2 UVCoords;
    UVCoords.x = 0.5 * ProjCoords.x + 0.5;
    UVCoords.y = 0.5 * ProjCoords.y + 0.5;
    float z = 0.5 * ProjCoords.z + 0.5;

    /************ get texturesize *************/
    ivec2 texSize = textureSize(uShadowmap,0);
    float xOffset = 1.0/float(texSize.x);
    float yOffset = 1.0/float(texSize.y);
    /******************************************/

    float shadowmap_factor = 0.0;
    float numberOfSamples = 0;
    for (int y = -2 ; y <= 2 ; y++) {
        for (int x = -2 ; x <= 2 ; x++) {

            //calculate offstes with size of texels
            vec2 Offsets = vec2(x * xOffset, y * yOffset);

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
    float nDotl = dot(N,L);
    float lightIntense = attenuation;

    vec3 diffuse = calculateDiffuse(N, L, uLightColor,nDotl);
    vec3 specular = calculateSpecularBlinn(N, V, L, uLightColor, nDotl, AMBILIGHT);

    vec3 diffuseFinal = max(diffuse*lightIntense,0);
    vec3 specularFinal = max(specular*lightIntense,0);

    /*************************************************************************/

    float shadowFactor = getAttenuationPCF(vShadow);

    FragColor =  AMBILIGHT + vec4(diffuseFinal, 1.0)*(shadowFactor+AMBILIGHT*2) + vec4(specularFinal, 1.0)*shadowFactor;
}