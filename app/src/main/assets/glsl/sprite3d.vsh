uniform mat4 projection;
uniform mat4 model;
uniform vec4 inputColor;

attribute vec4 position;
attribute vec4 inputTextureCoordinate;
attribute vec4 vertexColor;

varying vec2 textureCoordinate;
varying vec4 vColor;

void main()
{
	gl_Position = projection * model * position;
	textureCoordinate = inputTextureCoordinate.xy;
	vColor = vertexColor*inputColor;
}
