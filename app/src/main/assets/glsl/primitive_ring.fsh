precision mediump float; 

uniform vec4 inputColor;
uniform float radius;
uniform float aaWidth;
uniform float thickness;

varying vec2 textureCoordinate;

void main()
{
	float ceneter = 1.0 - 0.5*thickness/radius;
	float dist = 1.0 - abs( ceneter - 2.0 * distance( textureCoordinate, vec2( 0.5, 0.5 ) ) ) / (0.5*thickness/radius);
	float t = min( max( dist/(aaWidth/(0.5*thickness) ), 0.0 ), 1.0 );
	
    gl_FragColor = inputColor * t;
} 


