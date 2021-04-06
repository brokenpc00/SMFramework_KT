precision mediump float; 

uniform sampler2D inputImageTexture;
uniform vec4 inputColor;
uniform float inputTextureWidth;
uniform float inputTextureHeight;
uniform highp float scanPosition;

varying vec2 textureCoordinate;

float scanOffset = scanPosition / inputTextureHeight;
float d = sin(scanOffset * 5.0)*0.5 + 1.5;
vec2 iResolution = vec2(inputTextureWidth, inputTextureHeight);

#define M_PI 3.1415926535897932384626433832795
#define SCAN_RADIUS 0.1

float lookup(float dx, float dy)
{
    vec2 uv = textureCoordinate.xy + vec2(dx * d, dy * d) / iResolution.xy;
    vec4 c = texture2D(inputImageTexture, uv.xy);
    return 0.2126*c.r + 0.7152*c.g + 0.0722*c.b;
}

void main(void)
{
    float a;
    float d = (scanOffset-textureCoordinate.y);
    
    if (abs(d) < SCAN_RADIUS) {
        a = 0.5 + 0.5 * cos( d * M_PI / SCAN_RADIUS );
    } else {
    	a = 0.0;
    }
    
    if (a > 0.0) {
        float gx = 0.0;
        gx += -1.0 * lookup(-1.0, -1.0);
        gx += -2.0 * lookup(-1.0,  0.0);
        gx += -1.0 * lookup(-1.0,  1.0);
        gx +=  1.0 * lookup( 1.0, -1.0);
        gx +=  2.0 * lookup( 1.0,  0.0);
        gx +=  1.0 * lookup( 1.0,  1.0);
    
        float gy = 0.0;
        gy += -1.0 * lookup(-1.0, -1.0);
        gy += -2.0 * lookup( 0.0, -1.0);
        gy += -1.0 * lookup( 1.0, -1.0);
        gy +=  1.0 * lookup(-1.0,  1.0);
        gy +=  2.0 * lookup( 0.0,  1.0);
        gy +=  1.0 * lookup( 1.0,  1.0);
    
        float g = gx*gx + gy*gy;
        float g2 = g * (sin(scanOffset*M_PI) / 2.0 + 0.5);
    
        vec4 col = texture2D(inputImageTexture, textureCoordinate);
        if (scanOffset < textureCoordinate.y) {
        	float s = 1.0-sin( 0.5 * d * M_PI / SCAN_RADIUS);
        	col += vec4(0.0, g+0.5*s, g2, 1.0);
        } else {
        	col += vec4(0.0, g*2.0, 0, 1.0);
        } 
        gl_FragColor = (a * col + (1.0-a) * texture2D(inputImageTexture, textureCoordinate));
    } else {
        gl_FragColor = texture2D(inputImageTexture, textureCoordinate) * inputColor;
    }
    
}