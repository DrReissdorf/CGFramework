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
in vec3 L_light;    //L=vector to light
in vec3 V; //V=vector to Camera
in vec3 distToLightVector;

out vec4 FragColor;

uniform vec3 lightPosition;
uniform vec3 modelColor;
uniform vec3 lightColor; //color, that represents the specular reflection of the light
uniform float lightRange; //how far the light travels
uniform float shininess;
uniform float reflectivity; //how much the model is gonna reflect
uniform vec3 L_sun;  //normalized vector that points towards the sun
uniform vec3 sunlightColor;  //color, that represents the sunlight (specular reflection)
uniform int enableSpecular; //if this is true, specular reflection is enabled

void main(void) {
      vec3 unitNormal = normalize(N);
      vec3 unitVectorToCamera = normalize(V);

      float distanceToLight = sqrt(dot(L_light,L_light));
      float lightEndDist = lightRange;
      float lightStartDist = 0;
      float lightIntense;

      /* ATTENUATION OF LIGHT SOURCE (NOT SUN) */
        if(distanceToLight <= lightStartDist) {   //max helligkeit
          lightIntense = 1;
        } else if(distanceToLight >= lightEndDist) {
          lightIntense = 0;
        } else {
          lightIntense = (lightEndDist-distanceToLight)/(lightEndDist-lightStartDist);
        }
      /********************/

      /* DIFFUSE LIGHTING SUN*/
      float normalDotlightSun = max(dot(unitNormal, L_sun),0.2);
      vec3 diffuseLightingSun = normalDotlightSun * sunlightColor;
      /********************/

      /*  DIFFUSE LIGHTING LIGHT*/
      float normalDotlight = max(dot(unitNormal, normalize(L_light)),0.00);
      vec3 diffuseLighting = normalDotlight * lightColor * lightIntense;
      /********************/

      float specularFactorSun;
      float specularFactorLight;
      if(enableSpecular==1) {
            /* SPECULAR LIGHTING SUN (BLINN-PHONG) */
            float specularIntensitySun = 0.0;
            if(normalDotlightSun > 0.3) { // check if we are on the back of the model (where no reflection should be)
                vec3 lightAddCamSun = L_sun+V;
                vec3 H_sun = lightAddCamSun/sqrt(dot(lightAddCamSun,lightAddCamSun));  //H winkelhalbierende (blinn-Phong)
                specularIntensitySun = reflectivity * 1 * max(pow( dot(unitNormal,normalize(H_sun)),shininess),0.0); //no lightintense because sun is bright overall
            }
            specularFactorSun = max(specularIntensitySun,0.0);
            /********************************/
            /* SPECULAR LIGHTING LIGHT (BLINN-PHONG) */
            float specularIntensityLight = 0.0;
            if(normalDotlight > 0.0) { // check if we are on the back of the model (where no reflection should be)
                vec3 lightAddCam = L_light+V;
                vec3 H_light = lightAddCam/sqrt(dot(lightAddCam,lightAddCam));
                specularIntensityLight = reflectivity * lightIntense * pow( dot(unitNormal,normalize(H_light)),shininess);
            }
            specularFactorLight = max(specularIntensityLight,0.0);
            /*******************************/
      } else {
            specularFactorSun = 0.0;
            specularFactorLight = 0.0;
      }

      vec4 diffAndSpecSun = vec4(diffuseLightingSun*modelColor, 1.0) + vec4(specularFactorSun*sunlightColor,1.0); //diff und specular fertig berechnet, jetzt auf fragcolor addieren
      vec4 diffAndSpecLight = vec4(diffuseLighting*modelColor, 1.0) + vec4(specularFactorLight*lightColor,1.0); //diff und specular fertig berechnet, jetzt auf fragcolor addieren

      FragColor = diffAndSpecSun + diffAndSpecLight;
}