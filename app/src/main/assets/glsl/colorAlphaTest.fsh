precision lowp float;

uniform sampler2D inputImageTexture;
uniform float alpha_value;

varying vec4 v_fragmentColor;
varying vec2 v_texCoord;

void main()
{
    vec4 texColor = texture2D(inputImageTexture, v_texCoord);

    if (texColor.a <= alpha_value)
        discard;

    gl_FragColor = texColor * v_fragmentColor;
}