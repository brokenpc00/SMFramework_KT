uniform mat4 projection;
uniform mat4 model;

attribute vec4 a_position;
attribute vec2 a_texCoord;
attribute vec4 a_color;

varying lowp vec4 v_fragmentColor;
varying mediump vec2 v_texCoord;

void main()
{
    gl_Position = projection * model * a_position;
    v_fragmentColor = a_color;
    v_texCoord = a_texCoord;
}
