precision highp float;

uniform sampler2D inputImageTexture;
uniform lowp vec4 inputColor;

uniform vec2 u_dimension;
uniform vec2 u_center;

uniform float u_radius;
uniform float u_border;

varying vec2 textureCoordinate;


void main()
{
    vec2 p = u_dimension * textureCoordinate;
    float d = distance(p, u_center) - u_radius;

    float a = 1.0;
    if (d > 0.0) {
        a = smoothstep(u_border, 0.0, d);
    }

    gl_FragColor = texture2D( inputImageTexture, textureCoordinate ) * inputColor * a;
}


