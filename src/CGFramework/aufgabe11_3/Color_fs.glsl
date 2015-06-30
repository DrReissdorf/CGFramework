#version 150

#define AMBILIGHT 0

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
        specular =  lightColor * uReflectivity * vec3(max(pow(dot(N, H), uShininess), 0.0));
    }
    return specular;
}

void main(void) {
    float lightStartDist = 0;

    /************** DIFFUSE AND SPECULAR CALCULATION ************************/
    float nDotl = dot(N,L);
    float lightIntense = attenuation;

    vec3 diffuse = calculateDiffuse(N, L, uLightColor,nDotl);
    vec3 specular = calculateSpecularBlinn(N, V, L, uLightColor, nDotl);

    vec3 diffuseFinal = max(diffuse*lightIntense,AMBILIGHT);
    vec3 specularFinal = max(specular*lightIntense,0);
    /*************************************************************************/

    FragColor = vec4(diffuseFinal, 1.0) + vec4(specularFinal, 1.0);
}