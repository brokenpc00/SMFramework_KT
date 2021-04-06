uniform mat4 projection;
uniform mat4 model;

attribute vec4 position;

void main()
{
	gl_Position = projection * model * position;
}
