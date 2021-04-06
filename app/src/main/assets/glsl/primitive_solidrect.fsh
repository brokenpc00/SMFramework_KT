precision highp float; 

uniform lowp vec4 inputColor;
uniform vec2 dimension;
uniform float round;
uniform float aaWidth;

varying vec2 textureCoordinate;

void main()
{
	vec2  p = 2.0 * (textureCoordinate - 0.5);	
	vec2  b = dimension - round;
	float d = length(max(abs(p)-b, 0.0)) - round;
	float c = 1.0 - smoothstep(0.0, aaWidth, d);

	gl_FragColor = inputColor * c;
} 
