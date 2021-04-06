precision mediump float; 

uniform sampler2D inputImageTexture;

varying vec2 textureCoordinate;
varying vec4 vColor;

void main()
{
	gl_FragColor = texture2D( inputImageTexture, textureCoordinate ) * vColor;
}


