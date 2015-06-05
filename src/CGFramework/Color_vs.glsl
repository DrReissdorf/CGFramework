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
#extension GL_ARB_explicit_attrib_location : enable

layout(location=0) in vec3 aPosition;
layout(location=1) in vec3 aNormal;

uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;

uniform vec3 lightPosition;

out vec3 uColor;
out vec3 N;
out vec3 L_light;
out vec3 V;

void main(void) {
    vec4 worldPosition= uModel * vec4(aPosition,1.0);
    N = (uModel * vec4(aNormal,0.0)).xyz;
    L_light = lightPosition - worldPosition.xyz;

    V = (inverse(uView) * vec4(0.0,0.0,0.0,1.0)).xyz - worldPosition.xyz;
    gl_Position = uProjection * uView * worldPosition;    
}