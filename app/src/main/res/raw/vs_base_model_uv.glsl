// This matrix member variable provides a hook to manipulate
// the coordinates of the objects that use this vertex shader
uniform mat4 u_MVPMatrix;
uniform mat4 u_MVMatrix;
uniform vec3 a_Light_Pos;
uniform vec3 a_Light_Col;

attribute vec4 a_Position;
attribute vec3 a_Normal;
attribute vec2 a_UV;
//attribute vec3 a_Light_Pos;        // The position of the light in eye space.
//attribute vec3 a_Light_Col;        // The color of the light in eye space.

varying vec3 v_Position_Local;
//varying vec3 v_Position_Global;
varying vec2 v_UV;
varying vec3 v_Normal;

varying vec3 v_Light_Pos;        // The position of the light in eye space.
varying vec3 v_Light_Col;        // The color of the light in eye space.


void main() {
    // The matrix must be included as a modifier of gl_Position.
    // Note that the uMVPMatrix factor *must be first* in order
    // for the matrix multiplication product to be correct.
    gl_Position = u_MVPMatrix * a_Position;

    // Transform the normal's orientation into eye space.
    v_Normal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));

    v_UV = a_UV;
    v_Light_Pos = a_Light_Pos;
    v_Light_Col = a_Light_Col;
    v_Position_Local = vec3(u_MVMatrix * a_Position);
//    v_Position_Global = vec3(a_Position);
}