precision mediump float; 

uniform float radius;
uniform float aaWidth;
uniform vec2 anchor;
uniform vec4 inputColor;

varying vec2 textureCoordinate;

void main()
{
	float dist = 1.0 - 2.0 * distance( textureCoordinate, anchor );
	float t = clamp(dist/(aaWidth/radius), 0.0, 1.0);

    gl_FragColor = inputColor * t;
} 


