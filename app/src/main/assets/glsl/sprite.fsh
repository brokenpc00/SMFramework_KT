precision mediump float; 

uniform sampler2D inputImageTexture;
uniform lowp vec4 inputColor;

varying vec2 textureCoordinate;

void main()
{
	gl_FragColor = texture2D( inputImageTexture, textureCoordinate ) * inputColor;
}


