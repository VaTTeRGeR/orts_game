#version 130

in vec4 v_color;
in vec2 v_texCoords;

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

float simple_interpolate(in float a, in float b, in float x)
{
   return a + smoothstep(0.0,1.0,x) * (b-a);
}

float rand3D(in vec3 co){
    return fract(sin(dot(co.xyz ,vec3(12.9898,78.233,144.7272))) * 43758.5453);
}

float interpolatedNoise3D(in float x, in float y, in float z)
{
    float integer_x = x - fract(x);
    float fractional_x = x - integer_x;

    float integer_y = y - fract(y);
    float fractional_y = y - integer_y;

    float integer_z = z - fract(z);
    float fractional_z = z - integer_z;

    float v1 = rand3D(vec3(integer_x, integer_y, integer_z));
    float v2 = rand3D(vec3(integer_x+1.0, integer_y, integer_z));
    float v3 = rand3D(vec3(integer_x, integer_y+1.0, integer_z));
    float v4 = rand3D(vec3(integer_x+1.0, integer_y +1.0, integer_z));

    float v5 = rand3D(vec3(integer_x, integer_y, integer_z+1.0));
    float v6 = rand3D(vec3(integer_x+1.0, integer_y, integer_z+1.0));
    float v7 = rand3D(vec3(integer_x, integer_y+1.0, integer_z+1.0));
    float v8 = rand3D(vec3(integer_x+1.0, integer_y +1.0, integer_z+1.0));

    float i1 = simple_interpolate(v1,v5, fractional_z);
    float i2 = simple_interpolate(v2,v6, fractional_z);
    float i3 = simple_interpolate(v3,v7, fractional_z);
    float i4 = simple_interpolate(v4,v8, fractional_z);

    float ii1 = simple_interpolate(i1,i2,fractional_x);
    float ii2 = simple_interpolate(i3,i4,fractional_x);

    return simple_interpolate(ii1 , ii2 , fractional_y);
}

float Noise3D(in vec3 coord, in float wavelength)
{
   return interpolatedNoise3D(coord.x/wavelength, coord.y/wavelength, coord.z/wavelength);
}

void main()
{
	float texSize0 = 1.0/float(textureSize(u_tex0,0).x);
	float texSize1 = 1.0/float(textureSize(u_tex1,0).x);
	float texSize2 = 1.0/float(textureSize(u_tex2,0).x);

	vec2 v_texCoordsW = vec2(v_texCoords * texSize0);
	
	float a = v_color.a;
	vec4 water = texture2D(u_tex0, vec2(v_texCoordsW.y+0.005*time+0.02*sin((time/5+v_texCoordsW.y)*5), v_texCoordsW.x+0.01*cos((time/20+v_texCoordsW.y)*5)));
	vec4 sand = texture2D(u_tex1, v_texCoords * texSize1);
	
	vec4 grass = texture2D(u_tex2, v_texCoords * texSize2);
	
	if(a >= 0.9) {
		gl_FragColor = grass;
	} else if(a >= 0.55) {
		a = smoothstep(0.55, 0.9, a);
		gl_FragColor = mix(sand, grass, a);
	} else if(a >= 0.45) {
		gl_FragColor = sand;
	} else if(a >= 0.1) {
		a = pow(smoothstep(0.1, 0.45, a),5);
		float da = abs(0.6-a);
		float xa = 1.0 - smoothstep(0.0, 0.25, da);

		vec4 shore = vec4(1,1,1,0);
		shore *= Noise3D(vec3(v_texCoordsW.xy,time/20), 0.075);
		shore *= 0.15 * xa * (0.5+sin(time/3+v_texCoordsW.y*1)*0.25);
		//shore *= 0.15 * xa * (1.0+sin(time/5+v_texCoordsW.y*10))/2.0;
		
		gl_FragColor = mix(water, sand/* + lum(sand)*(a*0.075)*/, a);
		gl_FragColor = gl_FragColor + shore;
	} else {
		gl_FragColor = water;
	}
}

