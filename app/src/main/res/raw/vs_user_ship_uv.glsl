// This matrix member variable provides a hook to manipulate
// the coordinates of the objects that use this vertex shader
precision lowp float;
uniform mat4 u_MVPMatrix;
uniform mat4 u_MVMatrix;

attribute vec4 a_Position;
attribute vec3 a_Normal;
attribute vec2 a_UV;

varying vec3 v_Position_Local;
varying vec3 v_Position_Global;
varying vec2 v_UV;
varying vec3 v_Normal;

void main() {
    // The matrix must be included as a modifier of gl_Position.
    // Note that the uMVPMatrix factor *must be first* in order
    // for the matrix multiplication product to be correct.
    v_UV = a_UV;
    v_Position_Global = vec3(a_Position);
    v_Position_Local = vec3(u_MVMatrix * a_Position);
//    v_Normal = a_Normal;
    // Transform the normal's orientation into eye space.
    v_Normal = vec3(u_MVMatrix * vec4(a_Normal, 0.1));

    gl_Position = u_MVPMatrix * a_Position;
//    gl_PointSize = 1.0;
}