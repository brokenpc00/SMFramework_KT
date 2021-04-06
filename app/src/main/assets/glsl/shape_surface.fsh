precision highp float;

uniform vec2 dimension;
uniform float round;
uniform float border;
uniform lowp vec4 inputColor;

varying vec2 textureCoordinate;

// Ver 4.0
void main()
{
    vec2 half_dimen = 0.5 * dimension;
    vec2 s = abs(dimension * (textureCoordinate - 0.5));
    vec2 p = s - (half_dimen - vec2(round, round));
    
    float c;
    if (p.x * p.y > 0.0)
    {
        if (p.x > 0.0)
        {
            c = smoothstep(round, round-border, length(p));
        }
        else
        {
            c = 1.0;
        }
    }
    else
    {
        if (p.x > 0.0)
        {
            c = smoothstep(half_dimen.x, half_dimen.x-border, s.x);
        }
        else
        {
            c = smoothstep(half_dimen.y, half_dimen.y-border, s.y);
        }
    }
    
    gl_FragColor = inputColor * c;
}

