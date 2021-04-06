precision mediump float; 

uniform sampler2D inputImageTexture;
uniform lowp vec4 inputColor;
uniform vec2 textureCenter;  
uniform vec2 aspectRatio;
uniform float radius;
uniform float aaWidth;

varying vec2 textureCoordinate;

void main()
{
	float dist = radius - length( vec2(textureCoordinate - textureCenter) * aspectRatio );
	float t = min( max( dist/aaWidth, 0.0 ), 1.0 );
	
	if (textureCoordinate.x < 0.0 || textureCoordinate.y < 0.0 ||
	    textureCoordinate.x > 1.0 || textureCoordinate.y > 1.0 ) t = 0.0;
	 
	
    gl_FragColor = texture2D( inputImageTexture, textureCoordinate ) * inputColor * t;
      
}


