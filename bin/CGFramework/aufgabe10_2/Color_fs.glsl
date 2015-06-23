#version 150

#define LIGHTS 1

in vec3 N;
in vec3 V;

/*** LIGHTS ***/
in vec3[LIGHTS] L;
in float[LIGHTS] attenuationArray;
uniform vec3[LIGHTS] uLightPosArray;
uniform vec3[LIGHTS] uLightColorArray;
uniform float[LIGHTS] uLightRange;
uniform float uShininess;
uniform float uReflectivity;

/**** TEXTURE *****/
//uniform sampler2D uTexture;
in vec2 vTextureCoords;

/**** SHADOW ****/
in vec4 vShadow;
uniform sampler2DShadow uShadowmap;
//uniform sampler2D uShadowmap;

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
    diffuseFinal = max(diffuseFinal,ambilight);
    specularFinal = max(specularFinal,ambilight);

    //Shadow Mapping
    float shadow_bias = 0.0005;
    vec3 coord3 = 0.5 + 0.5 * vShadow.xyz / vShadow.w;
    coord3.z -= shadow_bias;
    float shadowmap_factor = texture(uShadowmap, coord3);

  //  vec4 textureColor = texture(uTexture,vTextureCoords);

  //  vec4 textureColor = texture(uShadowmap,vTextureCoords);
  //  FragColor = vec4(textureColor.r, textureColor.r, textureColor.r, 1.0);
    FragColor = ambilight + vec4(diffuseFinal, 1.0) * shadowmap_factor + vec4(specularFinal, 1.0);
   // FragColor = vec4(shadowmap_factor,shadowmap_factor,shadowmap_factor,1.0);
 //   FragColor = vec4(diffuseFinal, 1.0) * textureColor + vec4(specularFinal, 1.0);

}