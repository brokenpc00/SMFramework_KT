uniform mat4 projection;
uniform mat4 model;

attribute vec4 position;
attribute vec2 inputTextureCoordinate;

varying vec2 textureCoordinate;

void main()
{
	gl_Position = projection * model * position;
	textureCoordinate = inputTextureCoordinate;
}
