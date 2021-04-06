precision mediump float; 

uniform sampler2D inputImageTexture;
uniform lowp vec4 inputColor;

varying vec2 textureCoordinate;

const float PI = 3.1415926535;
const float aperture = 178.0;

// https://code.google.com/p/processing/source/browse/trunk/processing/java/libraries/opengl/examples/Shaders/FishEye/data/FishEye.glsl?r=9799
void main()
{
    float apertureHalf = 0.5 * aperture * (PI / 180.0);
    float maxFactor = sin(apertureHalf);

    vec2 uv;
    vec2 xy = 2.0 * textureCoordinate.xy - 1.0;
    float d = length(xy);

    float t;

    if (d < (2.0 - maxFactor)) {
        d = length(xy * maxFactor);
        float z = sqrt(1.0 - d * d);
        float r = atan(d, z) / PI;
        float phi = atan(xy.y, xy.x);

        uv.x = r * cos(phi) + 0.5;
        uv.y = r * sin(phi) + 0.5;

        gl_FragColor = texture2D( inputImageTexture, uv ) * inputColor;
    } else {
        gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
    }
}

