#version 150

#define LIGHTS 1

in vec3 N;
in vec3 V;
uniform mat4 uModel;

/*** LIGHTS ***/
in vec3[LIGHTS] L;
in float[LIGHTS] attenuationArray;
uniform vec3[LIGHTS] uLightPosArray;
uniform vec3[LIGHTS] uLightColorArray;
uniform float[LIGHTS] uLightRange;
uniform float uShininess;
uniform float uReflectivity;

/**** TEXTURE *****/
in vec2 vTextureCoords;

/**** SHADOW ****/
in vec4 vShadow;
uniform sampler2DShadow uShadowmap;

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

float getAttenuation( vec4 shadowmapCoord ) {
    float shadow_bias = 0.0004;
    vec3 coord3 = 0.5 + 0.5 * vShadow.xyz / vShadow.w;
    coord3.z -= shadow_bias;
    float shadowmap_factor = texture(uShadowmap, coord3);
    return shadowmap_factor;
}

float getAttenuationPCF( vec4 shadowmapCoord ) {
    float shadow_bias = 0.0004;

    vec3 ProjCoords = shadowmapCoord.xyz / shadowmapCoord.w;
    vec2 UVCoords;
    UVCoords.x = 0.5 * ProjCoords.x + 0.5;
    UVCoords.y = 0.5 * ProjCoords.y + 0.5;
    float z = 0.5 * ProjCoords.z + 0.5;

    ivec2 texSize = textureSize(uShadowmap,0);
    float xOffset = 1.0/float(texSize.x);
    float yOffset = 1.0/float(texSize.y);

    float shadowmap_factor = 0.0;
    float numberOfSamples = 0;

    for (int y = -2 ; y <= 2 ; y++) {
        for (int x = -2 ; x <= 2 ; x++) {
            vec2 Offsets = vec2(x * xOffset, y * yOffset);
            vec3 UVC = vec3(UVCoords + Offsets, z - shadow_bias);

            shadowmap_factor += texture(uShadowmap, UVC);
            numberOfSamples++;
        }
    }


    return shadowmap_factor/numberOfSamples;
}

void main(void) {
    float ambilight = 0.1;
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
    diffuseFinal = max(diffuseFinal,0);
    specularFinal = max(specularFinal,0);

    float shadow = getAttenuationPCF(vShadow);
    FragColor =  ambilight + vec4(diffuseFinal, 1.0)*shadow + vec4(specularFinal, 1.0)*shadow;
}