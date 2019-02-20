precision lowp float;
varying vec2 v_UV;
uniform sampler2D u_Texture;
void main() {
    gl_FragColor = texture2D(u_Texture, vec2(v_UV.x, 1.0 - v_UV.y));
}
