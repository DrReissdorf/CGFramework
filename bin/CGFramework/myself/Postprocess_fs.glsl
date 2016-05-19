#version 150

#define GAMMA_CORRECTION_VALUE 2.2

#define A 0.22 //Shoulder Strength
#define B 0.30 //Linear Strength
#define C 0.10 //Linear Angle
#define D 0.20 //Toe Strength
#define E 0.01 //Toe Numerator
#define F 0.30 //Toe Denominator
#define WHITE 11.2 // Linear White Point

in vec2 vTextureCoords;
out vec4 FragColor;

uniform sampler2D uTexture;

vec3 gammaCorrection(vec3 color) {
    return pow(color.rgb, vec3(1.0 / GAMMA_CORRECTION_VALUE));
}

vec4 exponentialToneMapping(vec4 color, float exposure) {
    vec4 newColor;

    newColor.rgb = 1-exp(-exposure*color.rgb);
    newColor.a = color.a;

    return newColor;
}

vec3 filmicToneMapping(vec3 color) {
    vec3 newColor;

    newColor = ((color*(A*color+C*B)+D*E)/(color*(A*color+B)+D*F)) - E/F;
    newColor /= ((WHITE*(A*WHITE+C*B)+D*E)/(WHITE*(A*WHITE+B)+D*F)) - E/F;

    return newColor;
}

void main() {
    vec4 texColor = texture(uTexture,vTextureCoords);
    vec4 filmic =  vec4(filmicToneMapping(texColor.xyz),1.0);
 //   vec4 exponential = exponentialToneMapping(texColor, 1.0);
    FragColor = filmic;
    FragColor = vec4(gammaCorrection(FragColor.rgb),1.0);
}