precision mediump float;

uniform sampler2D inputImageTexture;
uniform vec4 inputColor;
 
#define GAUSSIAN_SAMPLES 5
varying highp vec2 blurCoordinates[GAUSSIAN_SAMPLES];

void main()
{
	lowp vec4 sum = vec4(0.0);
	sum += texture2D(inputImageTexture, blurCoordinates[0]) * 0.204164;
	sum += texture2D(inputImageTexture, blurCoordinates[1]) * 0.304005;
	sum += texture2D(inputImageTexture, blurCoordinates[2]) * 0.304005;
	sum += texture2D(inputImageTexture, blurCoordinates[3]) * 0.093913;
	sum += texture2D(inputImageTexture, blurCoordinates[4]) * 0.093913;
	gl_FragColor = sum;
}
