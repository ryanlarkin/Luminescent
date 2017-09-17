#version 450
#extension GL_ARB_separate_shader_objects : enable

layout (binding = 0) uniform UboView {
	mat4 view;
	mat4 projection;
} uboView;

layout (binding = 1) uniform UboInstance {
	mat4 model;
	int frameIdx;
	int frameCount;
	float width;
} uboInstance;

layout(location = 0) in vec2 inPosition;
layout(location = 1) in vec4 inColor;
layout(location = 2) in vec2 inTexCoord;

layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec2 fragTexCoord;

out gl_PerVertex {
	vec4 gl_Position;
};

void main() {
	gl_Position = uboView.projection * uboView.view * uboInstance.model * vec4(inPosition, 0.0, 1.0);
	fragColor = inColor;
	
	fragTexCoord = vec2(inTexCoord.s + uboInstance.width * float(uboInstance.frameIdx) / uboInstance.frameCount, inTexCoord.t);
}