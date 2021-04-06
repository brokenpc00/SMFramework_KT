precision mediump float; 

uniform sampler2D inputImageTexture;
uniform sampler2D inputImageTexture2;

varying vec2 textureCoordinate;

const mediump vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);
 
void main()
{
    float r, g, b, y, u, v;
    y = texture2D(inputImageTexture, textureCoordinate).r;
    u = texture2D(inputImageTexture2, textureCoordinate).a - 0.5;
    v = texture2D(inputImageTexture2, textureCoordinate).r - 0.5;
    
    r = y + 1.13983*v;
    g = y - 0.39465*u - 0.58060*v;
    b = y + 2.03211*u;
    
    float saturation = 1.1; 
    
    lowp vec4 textureColor = vec4(r, g, b, 1.0);
    lowp float luminance = dot(textureColor.rgb, luminanceWeighting);
    lowp vec3 greyScaleColor = vec3(luminance);
    
	gl_FragColor = vec4(mix(greyScaleColor, textureColor.rgb, saturation), textureColor.w);    
}


