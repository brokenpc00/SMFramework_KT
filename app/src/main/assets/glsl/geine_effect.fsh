precision mediump float;

uniform vec2 u_anchor;
uniform float u_progress;

uniform sampler2D inputImageTexture;
uniform lowp vec4 inputColor;

varying vec2 textureCoordinate;

const vec4 ZERO = vec4(0.0, 0.0, 0.0, 0.0);

void main()
{
    vec2 coord = textureCoordinate + normalize(textureCoordinate - u_anchor) * u_progress;

    if (coord.x >= 0.0 && coord.x <= 1.0 && coord.y >= 0.0 && coord.y <= 1.0) {
        gl_FragColor = inputColor * texture2D(inputImageTexture, coord);
    } else {
        gl_FragColor = ZERO;
    }
}
