import bpy

projectname = bpy.path.display_name_from_filepath(bpy.data.filepath)

folderpath = "//render/" + projectname + "/"

scene = bpy.data.scenes["Scene"]

print("Rendering spritesheets for", projectname)

# copy since we modify this list mid iteration
render_objects = [bpy.data.objects.get("Root")]

i = 0

while i < len(render_objects):
    
    obj = render_objects[i]
    
    i = i + 1
    
    for obj_child in obj.children:
        render_objects.append(obj_child)


# Hide all objects
for obj in render_objects:
    obj.hide_render = True

# Unhide, render and hide again
for obj in render_objects:
    
    if obj.type != "MESH":
        continue
    
    obj.hide_render = False

    # Move object to origin
    location_backup = obj.location.copy()
    rotation_backup = obj.rotation_euler.copy()
    obj.location = [0,0,0]
    obj.rotation_euler = [0,0,0]
    
    print("Rendering part:", obj.name)
    
    for frame_current in range(scene.frame_start,scene.frame_end):
        
        filepath = folderpath + projectname + "_" + obj.name + "_" + str(frame_current) + ".png"
        
        scene.frame_current = frame_current
        
        bpy.context.scene.render.filepath = filepath

        bpy.ops.render.render(write_still=True)
        
        
    obj.hide_render = True
    
    obj.location = location_backup
    obj.rotation_euler = rotation_backup

scene.frame_current = scene.frame_start

print("Finished.")