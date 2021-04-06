precision mediump float;

uniform sampler2D inputImageTexture;
uniform lowp vec4 inputColor;

varying vec2 textureCoordinate;

uniform lowp float brightness;
uniform lowp float contrast;
uniform lowp float saturate;
uniform lowp float temperature;

void main()
{
    vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);

    // birghtness
    vec3 rgb = textureColor.rgb + vec3(brightness);

    // contrast
    rgb = (rgb - vec3(0.5)) * contrast + vec3(0.5);

    if (saturate != 1.0) {
        // saturation
        float luminance = dot(rgb, vec3(0.2125, 0.7154, 0.0721));
        rgb = mix(vec3(luminance), rgb, saturate);
    }

	if (temperature == 0.0) {
	    gl_FragColor = vec4(rgb, textureColor.a) * inputColor;
	} else {
        // temperature
        float rr = rgb.r;
        float gg = rgb.g;
        float bb = rgb.b;
        vec3 processed = vec3(
            (rr < 0.5 ? (rr * 1.86) : (0.86 + 0.14*rr)),
            (gg < 0.5 ? (gg * 1.08) : (0.08 + 0.92*gg)),
            (bb < 0.5 ? 0.0 : (2.0 * bb - 1.0))
            );

        gl_FragColor = vec4(mix(rgb, processed, temperature), textureColor.a) * inputColor;
    }
}


