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

vec4 gammaCorrection(vec4 color) {
    vec4 newColor;

    newColor.rgb = pow(color.rgb, vec3(1.0 / GAMMA_CORRECTION_VALUE));
    newColor.a = color.a;

    return newColor;
}

vec4 exponentialToneMapping(vec4 color, float exposure) {
    vec4 newColor;

    newColor.rgb = 1-exp(-exposure*color.rgb);
    newColor.a = color.a;

    return newColor;
}

vec4 filmicToneMapping(vec4 color) {
    vec4 newColor;

    newColor.x = ((color.x*(A*color.x+C*B)+D*E)/(color.x*(A*color.x+B)+D*F)) - E/F;
    newColor.y = ((color.y*(A*color.y+C*B)+D*E)/(color.y*(A*color.y+B)+D*F)) - E/F;
    newColor.z = ((color.z*(A*color.z+C*B)+D*E)/(color.z*(A*color.z+B)+D*F)) - E/F;
    newColor.a = color.a;

    //L_filmic = ( L(C)/L(WHITE) )
    float WHITE_LIN = ((WHITE*(A*WHITE+C*B)+D*E)/(WHITE*(A*WHITE+B)+D*F)) - E/F;
    newColor.x /= WHITE_LIN;
    newColor.y /= WHITE_LIN;
    newColor.z /= WHITE_LIN;

    return newColor;
}

void main() {
    vec4 texColor = texture(uTexture,vTextureCoords);
    vec4 filmic =  filmicToneMapping(texColor);
  //  vec4 exponential = exponentialToneMapping(texColor, 1.0);
    FragColor = filmic;
    FragColor = gammaCorrection(FragColor);
}