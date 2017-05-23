#version 120

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_tex0;
uniform sampler2D u_tex1;
uniform sampler2D u_tex2;

uniform float time;

float lum(vec4 c) {
	return c.r*0.2125+c.g*0.7154+c.b*0.0721;
}

float invlum(vec4 c) {
	return 1.0 - c.r*0.2125+c.g*0.7154+c.b*0.0721;
}

void main()
{
	float a = v_color.a;
	vec4 water = texture2D(u_tex0, vec2(v_texCoords.y+0.005*time+0.0025*sin(time), v_texCoords.x));
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
		a = pow(smoothstep(0.1, 0.45, a),5);
		gl_FragColor = mix(water, sand + lum(sand)*(a*0.075), a);
	} else {
		gl_FragColor = water;
	}
}

