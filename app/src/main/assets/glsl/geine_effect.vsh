uniform mat4 projection;
uniform mat4 model;

uniform float width;
uniform float height;
uniform float minimize;
uniform float bend;
uniform float side;

attribute vec4 position;
attribute vec2 inputTextureCoordinate;

varying vec2 textureCoordinate;

void main() {
    vec4 pos = vec4(position);

    pos.y = mix(position.y, height/2.0, minimize);

    float t = (pos.y + height/2.0) / height;
    t = (3.0 - 2.0 * t) * t * t;
    pos.x = mix(position.x+width/2.0, side * width, t * bend) - width/2.0;
    
    gl_Position = projection * model * pos;
    textureCoordinate = inputTextureCoordinate;
}
