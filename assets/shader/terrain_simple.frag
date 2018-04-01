#version 120

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_tex0;
uniform sampler2D u_tex1;
uniform sampler2D u_tex2;

void main()
{
	float a = v_color.a;

	vec4 water = texture2D(u_tex0, v_texCoords);
	vec4 sand = texture2D(u_tex1, v_texCoords);
	vec4 grass = texture2D(u_tex2, v_texCoords);
	
	
	if(a >= 0.9) {
		gl_FragColor = grass;
	} else if(a >= 0.55) {
		a = smoothstep(0.55, 0.9, a);
		gl_FragColor = mix(sand, grass, a);
	} else if(a >= 0.45) {
		gl_FragColor = sand;
	} else if(a >= 0.1) {
		a = smoothstep(0.1, 0.45, a);
		gl_FragColor = mix(water, sand, a);
	} else {
		gl_FragColor = water;
	}
}

