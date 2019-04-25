// This matrix member variable provides a hook to manipulate
// the coordinates of the objects that use this vertex shader
precision lowp float;
uniform mat4 uMVPMatrix;
attribute vec4 vPosition;
attribute vec2 a_UV;
varying vec2 v_UV;

void main() {
    // The matrix must be included as a modifier of gl_Position.
    // Note that the uMVPMatrix factor *must be first* in order
    // for the matrix multiplication product to be correct.
    v_UV = a_UV;
    gl_Position = uMVPMatrix * vPosition;
}