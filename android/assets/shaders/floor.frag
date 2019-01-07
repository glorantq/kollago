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

uniform sampler2D u_texture;

varying MEDP vec2 texCoords;

const vec4 fog_colour = vec4(126.0 / 255.0, 153.0 / 255.0, 186.0 / 255.0, 1.0);

vec4 add_fog(vec4 fragColour) {
  float perspective_far = 1024.0;
  float fog_coord = (gl_FragCoord.z / gl_FragCoord.w) / perspective_far;

  float fog_density = 2.3;
  float fog = fog_coord * fog_density;
  return mix(fog_colour, fragColour, clamp(1.0 - fog, 0.0, 1.0));
}

void main() {
   vec4 frag_color = texture2D(u_texture, texCoords);

   gl_FragColor = add_fog(frag_color);
}