# ABOUT THE PROJECT

This is a University project for Mobile Programming and Distributed Algorithm lessons.

It is an implementation of the popular [Tic Tac Toe game](https://en.wikipedia.org/wiki/Tic-tac-toe).
Fun Fact: in italian is called Tris because tris is the latin for tre (or three) .
It will contain two mode: 
  - Offline; one device needed because the two players are in the same room
  - Online; two device needed because one is hosting the game and the other one enters with a code

# DEPENDENCIES
You can find all the dependencies used for this project in the build.gradle.kts(Module) file.
In that file you can also find the java version, the default configurations and plugins.

If you can't find it, is probably because you have "Project" view in Android Studio so
search it in OnlineTicTacToe > app > build.gradle.kts.
If you are in "Android" view you shouldn't have problem finding it in gradle scripts.

# DIRECTORY OF THE PROJECT
In the directory app you will find:
    - **manifest**; it contains the AndroidManifest.xml.
                If you change/add/remove an activity remember to control if the manifest changed too.
                If you have to add some permissions check it here using <uses-permission.
                If you need some particular setting for an activity (like the screenOrientation) check it.
    - **java**; it contains the java classes (all are activities)
    - **res**;  It contain .xml resources like drawable, layout or values (such as strings or colors)
            If you want to change how the View is showed you probably need to check the layout folder.
            If you want to add some images data you can do it by adding them to the drawable folder.

# VERSION CONTROL
The local offline part is the same in every branch.
Otherwise the online part was hard for me to make so i split it in various branches:
    - **master** was used to create the online game with Firebase, due to project's requirement
        from my school i decided to pass entirely to Socket.
    - **WifiDirect** was used to test if WifiP2P (or WifiDirect) could be used instead of Socket. 
        WifiP2P wasn't doable for different problems. So i decided to rename this branch **BTsocket**.
        BTsocket stands for Bluetooth Socket, from now on i will use this branch to work with a bluetooth connection. 
    - **Socket** is used to connect the devices with a simple socket.

The merge of the 23/02/2024 makes master and WifiDirect/BTsocket the same.

# OTHER
If there are any problems you can open a github issue or you can first check some online forums to 
be sure is not project related issue.
You can find a small mockup image in the same folder of readme.md, it shows how the project should work.

**Thank you for cloning or simply using it**

