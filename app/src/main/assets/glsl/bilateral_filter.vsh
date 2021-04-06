uniform mat4 projection;
uniform mat4 model;
uniform vec4 inputColor;

uniform float texelWidthOffset;
uniform float texelHeightOffset;

attribute vec4 position;
attribute vec2 inputTextureCoordinate;

varying vec2 textureCoordinate;

#define GAUSSIAN_SAMPLES 9
varying vec2 blurCoordinates[GAUSSIAN_SAMPLES];

uniform vec2 u_resolution;
uniform vec2 u_center;
uniform vec2 u_radius;
uniform float u_aaWidth;

varying vec2 center;
varying float aspect;
varying float aaWidth;
varying float invRadius;

void main()
{
	gl_Position = projection * model * position;
	textureCoordinate = inputTextureCoordinate.xy;

	center = u_center / u_resolution;
	if (u_radius.x > 0.0 && u_radius.y > 0.0) {
		aspect = u_radius.x / u_radius.y;
		aaWidth = u_aaWidth / u_radius.x;
		invRadius = 1.1 * u_resolution.x / u_radius.x;
	} else {
		aspect = 1.0;
		aaWidth = 1.0;
		invRadius = 0.0;
	}

	int multiplier = 0;
	vec2 blurStep;
	vec2 singleStepOffset = vec2(texelWidthOffset, texelHeightOffset);

	for (int i = 0; i < GAUSSIAN_SAMPLES; i++) {
	    multiplier = (i - ((GAUSSIAN_SAMPLES - 1) / 2));
	    blurStep = float(multiplier) * singleStepOffset;
	    blurCoordinates[i] = inputTextureCoordinate.xy + blurStep;
	}
}

