precision mediump  float;
uniform sampler2D u_Texture;

varying vec2 v_UV;
varying vec3 v_Normal;
varying vec3 v_Position_Local;
//varying vec3 v_Position_Global;
varying vec3 v_Light_Pos;        // The position of the light in eye space.
varying vec3 v_Light_Col;        // The color of the light in eye space.

void main() {
    // ambient lighting
    float amientStrength = 0.1;
    vec3 ambientColor = vec3(1.0, 1.0, 1.0);
    vec3 ambient = amientStrength * ambientColor;

    // diffuse lighting
    vec3 norm = normalize(v_Normal);
    vec3 lightDirectiton = normalize(v_Light_Pos - v_Position_Local);
    float diff = max(dot(norm, lightDirectiton), 0.0);
    float diffuseStrength = 1.0;
    vec3 diffuse = diff * (diffuseStrength + v_Light_Col);

    // result lighting
    vec3 result = ambient + diffuse;

    // texures
    vec4 texture = texture2D(u_Texture, vec2(v_UV.x, 1.0 - v_UV.y));

    // final preparing
    gl_FragColor =  texture * vec4(result, 1.0);
    gl_FragColor.a = 1.0;
}