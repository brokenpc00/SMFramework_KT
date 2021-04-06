uniform mat4 projection;
uniform mat4 model;
uniform vec4 inputColor;

uniform float texelWidthOffset;
uniform float texelHeightOffset;

attribute vec4 position;
attribute vec2 inputTextureCoordinate;

varying vec2 textureCoordinate;

#define GAUSSIAN_SAMPLES 5
varying vec2 blurCoordinates[GAUSSIAN_SAMPLES];
 
void main()
{
	gl_Position = projection * model * position;
 
	vec2 singleStepOffset = vec2(texelWidthOffset, texelHeightOffset);
	blurCoordinates[0] = inputTextureCoordinate.xy;
	blurCoordinates[1] = inputTextureCoordinate.xy + singleStepOffset * 1.407333;
	blurCoordinates[2] = inputTextureCoordinate.xy - singleStepOffset * 1.407333;
	blurCoordinates[3] = inputTextureCoordinate.xy + singleStepOffset * 3.294215;
	blurCoordinates[4] = inputTextureCoordinate.xy - singleStepOffset * 3.294215;
}