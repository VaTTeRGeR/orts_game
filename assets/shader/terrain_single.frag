#version 130

in float v_alpha;
in vec2 v_texCoords;

uniform sampler2D u_tex0;

out vec4 diffuseColor;

void main()
{
	float texSize = 1.0/float(textureSize(u_tex0,0).x);
	
	vec4 tex0_color = texture2D(u_tex0, v_texCoords * texSize);

	diffuseColor = vec4(tex0_color.rgb, tex0_color.a * v_alpha);
}

