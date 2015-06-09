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

uniform vec3 light1Position;
uniform vec3 light2Position;

out vec3 uColor;
out vec3 vNormal;
out vec3 L1;
out vec3 L2;
out vec3 V;

void main(void) {
    vec4 worldPosition = uModel * vec4(aPosition,1.0);

    V = -normalize( vec3(uView * worldPosition) );

    L1 = normalize( mat3(uModel) * (light1Position - aPosition) );
    L2 = normalize( mat3(uModel) * (light2Position - aPosition) );

    gl_Position = uProjection * uView * worldPosition;

    vNormal = aNormal;
}