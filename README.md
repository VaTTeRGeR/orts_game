#WWII RTS - Work in progress
<img src="https://i.imgur.com/rydvH0r.jpg"><br><br>
Tested on Windows 7/10 and Lubuntu 18.04<br><br>
#Requirements<br>
-Eclipse with Java 12 or newer and Maven (latest Eclipse/Java combo basically)<br>
-OpenGL 3.2 enabled graphics card, older Intel HD chipsets do not support OpenGL properly<br><br>
#Installation in Eclipse<br>
-Import -> Projects from git -> https://github.com/VaTTeRGeR/orts_game.git -> DONE<br><br>
OR<br><br>
-Clone to local disk<br>
-Import -> existing project into workspace -> DONE<br><br>
OR<br><br>
-Download as ZIP<br>
-Unzip<br>
-Import -> existing project into workspace -> DONE<br><br>
Maven automatically downloads dependencies after you import the project.<br>
The project is old and large, importing from github may take a while.<br><br>
#Execution<br>
-Run the main class: de.vatterger.game.ClientApplication2D as "Java Application"<br><br>
#Runnable JAR Export
-Right click on the Eclipse project<br>
-Click "Run As" -> "Maven build..."<br>
-Type "package" into the field "goal" in the "Maven-build..."-screen<br>
-Click "Apply" and "Run"
