import bpy, math

def hide_children(root):
    for obj in root.children:
        obj.hide_render = True

def show_children(root):
    for obj in root.children:
        obj.hide_render = False


projectname = bpy.path.display_name_from_filepath(bpy.data.filepath)

folderpath = "//output/"

scene = bpy.data.scenes["Scene"]


print("Applying render settings for", projectname)

scene.render.engine = 'CYCLES'
scene.cycles.device = 'GPU'
scene.render.film_transparent = True
scene.view_settings.exposure = -3
scene.view_settings.gamma = 1

scene.render.resolution_x = 1000
scene.render.resolution_y = 1000

scene.frame_start = 0
scene.frame_end = 16
scene.frame_step = 1


print("Rendering spritesheets for", projectname)

# copy since we modify this list mid iteration
render_objects = []
for obj in bpy.data.collections['renderables'].objects:
    if obj.type == 'EMPTY':
        render_objects.append(obj)

finished_frames = 0
total_frames = len(render_objects) * 16

# Hide all objects
for root in render_objects:
    hide_children(root)

# Unhide, render and hide again
for obj in render_objects:
    
    show_children(obj)

    # Move object to origin
    location_backup = obj.location.copy()
    rotation_backup = obj.rotation_euler.copy()
    obj.location = [0,0,0]
    obj.rotation_euler = [0,0,0]
    
    print("Rendering part:", obj.name)
    
    for frame_current in range(scene.frame_start,scene.frame_end):
        
        rotation = frame_current * ( 2 * math.pi / (scene.frame_end - scene.frame_start) )
        
        obj.rotation_euler = [0,0,rotation]
    
        filepath = folderpath + obj.name + "_" + str(frame_current) + ".png"
        
        scene.frame_current = frame_current
        
        bpy.context.scene.render.filepath = filepath

        bpy.ops.render.render(write_still=True)
        
        
    hide_children(obj)
    
    obj.location = location_backup
    obj.rotation_euler = rotation_backup
    
    finished_frames = finished_frames + 1

scene.frame_current = scene.frame_start

# Unhide all objects
for root in render_objects:
    show_children(root)

print("Finished.")