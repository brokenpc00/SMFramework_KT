precision mediump float;

uniform vec2 u_p0;
uniform vec2 u_p1;
uniform vec2 u_p2;
uniform lowp vec4 inputColor;
uniform float u_aaWidth;

varying vec2 textureCoordinate;

float fillTriangle(vec2 p, vec2 p0, vec2 p1, vec2 p2)
{
    vec2 e0 = p1 - p0;
    vec2 e1 = p2 - p1;
    vec2 e2 = p0 - p2;
    
    vec2 v0 = p - p0;
    vec2 v1 = p - p1;
    vec2 v2 = p - p2;
    
    vec2 pq0 = v0 - e0*clamp( dot(v0,e0)/dot(e0,e0), 0.0, 1.0 );
    vec2 pq1 = v1 - e1*clamp( dot(v1,e1)/dot(e1,e1), 0.0, 1.0 );
    vec2 pq2 = v2 - e2*clamp( dot(v2,e2)/dot(e2,e2), 0.0, 1.0 );
    
    vec2 d = min( min( vec2( dot( pq0, pq0 ), v0.x*e0.y-v0.y*e0.x ),
                      vec2( dot( pq1, pq1 ), v1.x*e1.y-v1.y*e1.x )),
                 vec2( dot( pq2, pq2 ), v2.x*e2.y-v2.y*e2.x ));
    
    return -sqrt(d.x)*sign(d.y);
}

void main()
{
    float dist = fillTriangle(textureCoordinate, u_p0, u_p1, u_p2);
    float d = smoothstep(0.0, u_aaWidth, dist);
    float c = 1.0 - d;

    gl_FragColor = inputColor * c;
}
