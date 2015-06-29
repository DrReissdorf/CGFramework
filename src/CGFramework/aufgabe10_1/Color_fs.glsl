#version 150

in vec3 uPosition;
in vec3 N;
in vec3 V;

/*** LIGHTS ***/
in vec3 L;
in float attenuation;
uniform vec3 uLightPos;
uniform vec3 uLightColor;
uniform float uLightRange;
uniform float uShininess;
uniform float uReflectivity;

/**** TEXTURE *****/
uniform sampler2D uTexture;
in vec2 vTextureCoords;

uniform mat4 uView;
uniform mat4 uInvertedUView;

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

    float nDotl = dot(N,L);
    float lightIntense = attenuation;

    vec3 diffuse = calculateDiffuse(N, L, uLightColor,nDotl);
    vec3 specular = calculateSpecularBlinn(N, V, L, uLightColor, nDotl, ambilight);

    vec3 diffuseFinal = max(diffuse*lightIntense,0);
    vec3 specularFinal = max(specular*lightIntense,0);

    FragColor = vec4(diffuseFinal, 1.0);
}