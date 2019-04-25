precision lowp float;
uniform sampler2D u_Texture;

uniform vec3 uLightPos;        // The position of the light in eye space.
uniform vec4 uLightCol;        // The color of the light in eye space.

varying vec2 v_UV;
varying vec3 v_Normal;
varying vec3 v_Position_Local;
//varying vec3 v_Position_Global;

void main() {
    // Will be used for attenuation.
    float distance = length(uLightPos - v_Position_Local);

    // Doing lighting calculations in the fragment give us an interpolated normal--smoother shading
    // Get a lighting direction vector from the light to the vertex.
//    vec3 lightVectorGlobal = normalize(uLightPos - v_Position_Global);
    vec3 lightVectorLocal = normalize(uLightPos - v_Position_Local);

    // Calculate the dot product of the light vector and vertex normal. If the normal and light vector are
    // pointing in the same direction then it will get max illumination.
//    float diffuseGlobal = max(dot(v_Normal, lightVectorGlobal), 0.0);
    float diffuseLocal = max(dot(v_Normal, lightVectorLocal), 0.1);

    diffuseLocal = diffuseLocal * (1.0 / (1.0 + (0.25 * distance)));

//    diffuseLocal = diffuseLocal * (1.0 / (1.0 + (0.001 * distance)));    //We might lose Pluto...
    // Add attenuation.


    // Add ambient lighting
//    diffuseGlobal = diffuseGlobal - 0.2;  //very little ambient lighting.... this is space
//    diffuseLocal = diffuseLocal - 1.0;  //very little ambient lighting.... this is space

    vec4 texture = texture2D(u_Texture, vec2(v_UV.x, 1.0 - v_UV.y)) ;

//    gl_FragColor =  texture * (diffuseLocal + diffuseGlobal);
        gl_FragColor =  texture * diffuseLocal;

    gl_FragColor.a = 1.0;
}