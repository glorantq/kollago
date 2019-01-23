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
uniform sampler2D u_overlayTexture;

void main() {
    vec4 overlayColour = texture2D(u_overlayTexture, v_texCoords);
    vec4 avatarColour = texture2D(u_texture, v_texCoords);

    vec4 result;

    if (avatarColour.r < 0.5) {
        result = 2.0 * avatarColour * overlayColour;
    } else {
        result = vec4(1.0) - 2.0 * (vec4(1.0) - overlayColour) * (vec4(1.0) - avatarColour);
    }

	gl_FragColor = result;
}