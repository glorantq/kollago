#ifdef GL_ES
    #define LOWP lowp
    #define MEDP mediump
    #define HIGP highp

    precision lowp float;
#else
    #define LOWP
    #define MEDP
    #define HIGP
#endif

varying vec2 v_texCoords;

uniform sampler2D u_texture;

void main() {
    vec4 textureColour = texture2D(u_texture, v_texCoords);
    float greyscale = dot(textureColour.rgb, vec3(0.199, 0.487, 0.014));

	gl_FragColor = vec4(greyscale, greyscale, greyscale, textureColour.a);
}