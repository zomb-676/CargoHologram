#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 BlurDir;
uniform float Radius;
uniform vec4 Area;

out vec4 fragColor;

bool inArea() {
    return texCoord.x >= Area.x && texCoord.x <= Area.y &&
    texCoord.y <= Area.z && texCoord.y >= Area.w;
}

void main() {
    if (inArea()) {
        vec4 blur = vec4(0.0);

        float radius = floor(Radius);
        // sigma = radius * 0.5
        // base = -0.5 / (sigma * sigma)
        // factor = 1.0 / (sigma * sqrt(2*PI))
        float base = -2.0 / (radius * radius);
        float factor = 0.79788456 / radius;
        vec2 dir = oneTexel * BlurDir;
        float wsum = 0.0, w;
        for (float r = -radius; r <= radius; r += 1.0) {
            w = exp(r * r * base) * factor;
            blur += texture(DiffuseSampler, texCoord + r * dir) * w;
            wsum += w;
        }

        fragColor = vec4(blur.rgb / wsum, 1.0);
    } else {
        //must set alpha to 1.0 ,the origin is 0.0 ,due to mojang's stupid alpha set
        fragColor = vec4(texture(DiffuseSampler, texCoord).rgb, 1.0);
    }
}