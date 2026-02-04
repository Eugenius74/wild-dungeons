uniform sampler2D DiffuseSampler0;
uniform sampler2D DiffuseDepthSampler;

uniform vec4 FogColor;
uniform int FogShape;
uniform float GameTime;
uniform float InfluenceR;

in vec2 texCoord;

out vec4 fragColor;
void main() {

    vec4 base = texture(DiffuseSampler0, texCoord);

    vec3 lum = vec3(0.599, 0.587, 0.114);
    float grey = dot(base.rgb, lum);
    vec3 greyColor = vec3(grey);

    float offset = 0.005;
    float redShift = texture(DiffuseSampler0, texCoord + vec2(offset, 0)).r;
    float diff = max(redShift - grey, 0.0);
    diff = pow(diff, 0.2);

    vec3 redAberration = vec3(diff * 2.0, 0.0, 0.0);
    vec3 finalColor = ((greyColor) + redAberration)* vec3(0.4);

    vec4 f = vec4(finalColor, base.a) * vec4(InfluenceR);
    vec4 f2 = base * vec4(1.0-InfluenceR);

    fragColor = f + f2;
}