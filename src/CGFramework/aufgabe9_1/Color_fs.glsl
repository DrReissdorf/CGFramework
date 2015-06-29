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

/**** TEXTURE *****/
uniform sampler2D uTexture;
in vec2 vTextureCoords;

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

    float nDotl = dot(N,L);
    float lightEndDist = uLightRange;
    float lightIntense = attenuation;
     //lightIntense = 1;

    vec3 diffuse = calculateDiffuse(N, L, uLightColor,nDotl);
    vec3 specular = calculateSpecularBlinn(N, V, L, uLightColor, nDotl, ambilight);

    vec3 diffuseFinal = max(diffuse*lightIntense,ambilight);
    vec3 specularFinal = max(specular*lightIntense,ambilight);

    vec4 textureColor = texture(uTexture,vTextureCoords);
    FragColor = vec4(diffuseFinal, 1.0) * textureColor + vec4(specularFinal, 1.0);
}