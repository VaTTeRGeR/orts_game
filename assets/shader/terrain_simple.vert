#version 130

in vec4 a_position;
in float a_alpha;
in vec2 a_texCoord0;

uniform mat4  u_projTrans;
uniform vec2  u_offset;
uniform float time;

out float v_alpha;
out vec2 v_texCoords;

void main()
{
   v_alpha = a_alpha * (255.0/254.0);

   v_texCoords = a_texCoord0;

   // 45 Degrees isometric projection by compressing along the Y axis
   gl_Position =  u_projTrans * ((a_position + vec4(u_offset.xy,0,0)) * vec4(1,0.7071068,1,1));
}