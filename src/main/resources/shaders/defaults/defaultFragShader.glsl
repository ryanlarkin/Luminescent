#version 450
#pragma shader_stage(fragment)

layout(binding = 2) uniform sampler2D texSampler;

layout(binding = 3) uniform LightSource {
	vec3 source[250];
} lights;

layout(location = 0) in vec4 fragColor;
layout(location = 1) in vec2 fragTexCoord;
layout(location = 2) flat in int fragLightCount;

layout(location = 0) out vec4 outColor;

void main() {
	vec4 texture = texture(texSampler, fragTexCoord);
	
	float sumLight = 0;
	
	if(fragLightCount >= 0) {
		for(int i = 0; i < fragLightCount; i++) {
			if(lights.source[i].z > 0) {
				float x = gl_FragCoord.x - lights.source[i].x;
				float y = gl_FragCoord.y - lights.source[i].y;
				float r = lights.source[i].z;

				float r2 = r*r;
				float d2 = min((x*x)+(y*y), r2);
				sumLight += (r2 - d2)/(d2 + r2);
			}
		}
	}
	else {
		sumLight = 1;
	}
	

	outColor = vec4((texture.rgb + fragColor.rgb * (1 - texture.a)) * min(sumLight, 1), texture.a + fragColor.a * (1 - texture.a));
}