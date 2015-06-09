/* 
 * Cologne University of Applied Sciences
 * Institute for Media and Imaging Technologies - Computer Graphics Group
 *
 * Copyright (c) 2012 Cologne University of Applied Sciences. All rights reserved.
 *
 * This source code is property of the Cologne University of Applied Sciences. Any redistribution
 * and use in source and binary forms, with or without modification, requires explicit permission. 
 */

#version 150

in vec3 N;  //normalvector
in vec3 L1;    //L=vector to light
in vec3 L2;
in vec3 V; //V=vector to Camera
in vec3 distToLightVector;
in vec3 vNormal;

out vec4 FragColor;

uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 normalMat;

uniform vec3 light1Color;    //color, that represents the specular reflection of the light
uniform float light1Range;   //how far the light travels
uniform vec3 light2Color;    //color, that represents the specular reflection of the light
uniform float light2Range;   //how far the light travels
uniform vec3 modelColor;
uniform float shininess;
uniform float reflectivity; //how much the model is gonna reflect
uniform int isPhong;        //if this is 1, Phong-Model used, else Blinn-Phong-Model

vec3 calculateSpecularPhong(vec3 N, vec3 V, vec3 L, vec3 lightColor, float nDotl, float lightIntense, float ambilight) {
    vec3 specular;
    vec3 R = normalize(reflect(L, N));
    if(nDotl > ambilight)
        specular = lightIntense * lightColor * reflectivity * vec3(max(pow(dot(-R, V), shininess), 0.0));
    return specular;
}

vec3 calculateSpecularBlinn(vec3 N, vec3 V, vec3 L, vec3 lightColor, float nDotl, float lightIntense, float ambilight) {
    vec3 specular;
    vec3 lightAddCam = L+V;
    vec3 H = normalize( lightAddCam/sqrt(lightAddCam.x*lightAddCam.x+lightAddCam.y*lightAddCam.y+lightAddCam.z*lightAddCam.z) );
    if(nDotl > ambilight)
        specular = lightIntense * lightColor * reflectivity * vec3(max(pow(dot(N, H), shininess+100), 0.0));
    return specular;
}

float attenuationOfLight (float distanceToLight, float lightStartDist, float lightEndDist) {
    float lightIntense;

    if(distanceToLight <= lightStartDist) {   //max helligkeit
        lightIntense = 1;
    } else if(distanceToLight >= lightEndDist) {
        lightIntense = 0;
    } else {
        lightIntense = (lightEndDist-distanceToLight)/(lightEndDist-lightStartDist);
    }

    return lightIntense;
}

vec3 calculateDiffuse(vec3 N, vec3 L, vec3 lightColor, float nDotl, float lightIntense) {
    vec3 diffuseLighting = lightColor * lightIntense * vec3(max(nDotl, 0.0));
    return diffuseLighting;
}

void main(void) {
    vec3 N = normalize(mat3(normalMat) * vNormal);
    float nDotl1 = dot(N, L1);
    float nDotl2 = dot(N, L2);

    float ambilight = 0.05;

    float distanceToLight1 = sqrt(dot(L1,L1));
    float lightEndDist1 = light1Range;
    float lightStartDist1 = 0;
    float lightIntense1 = attenuationOfLight(distanceToLight1,lightStartDist1,lightEndDist1);

    float distanceToLight2 = sqrt(dot(L2,L2));
    float lightEndDist2 = light2Range;
    float lightStartDist2 = 0;
    float lightIntense2 = attenuationOfLight(distanceToLight2,lightStartDist2,lightEndDist2);

    vec3 diffuseLighting1 = calculateDiffuse(N,L1,light1Color,nDotl1,lightIntense1);
    vec3 specular1;
    if(isPhong == 1) {
        specular1 = calculateSpecularPhong(N, V, L1, light1Color, nDotl1, lightIntense1, ambilight);
    } else {
        specular1 = calculateSpecularBlinn(N, V, L1, light1Color, nDotl1, lightIntense1, ambilight);
    }

    vec3 diffuseLighting2 = calculateDiffuse(N,L2, light2Color, nDotl2,lightIntense2);
    vec3 specular2;
    if(isPhong == 1) {
        specular2 = calculateSpecularPhong(N, V, L2, light2Color, nDotl2, lightIntense2, ambilight);
    } else {
        specular2 = calculateSpecularBlinn(N, V, L2, light2Color ,nDotl2, lightIntense2, ambilight);
    }

    vec4 diffAndSpecLight1 = vec4(diffuseLighting1, 1.0) + vec4(specular1,1.0); //diff und specular fertig berechnet, jetzt auf fragcolor addieren
    vec4 diffAndSpecLight2 = vec4(diffuseLighting2, 1.0) + vec4(specular2,1.0); //diff und specular fertig berechnet, jetzt auf fragcolor addieren

    FragColor = diffAndSpecLight1 + diffAndSpecLight2;
}