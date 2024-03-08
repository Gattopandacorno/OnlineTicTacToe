# ABOUT THE PROJECT

This is a University project for Mobile Programming and Distributed Algorithm lessons.

It is an implementation of the popular [Tic Tac Toe game](https://en.wikipedia.org/wiki/Tic-tac-toe).
Fun Fact: in italian is called Tris because tris is the latin for tre (or three) .
It will contain two mode: 
  - Offline; one device needed because the two players are in the same room
  - Online; two device needed because they bluetooth connect with each other 

# DEPENDENCIES
You can find all the dependencies used for this project in the build.gradle.kts(Module) file.
In that file you can also find the java version, the default configurations and plugins.

If you can't find it, is probably because you have "Project" view in Android Studio so
search it in OnlineTicTacToe > app > build.gradle.kts.
If you are in "Android" view you shouldn't have problem finding it in gradle scripts.

# DIRECTORY OF THE PROJECT
In the directory app you will find:
    - **manifest**; it contains the AndroidManifest.xml.
                If you change/add/remove an activity remember to change the manifest too.
                If you have to add some implicit permissions check it here using <uses-permission.
                If you need some particular setting for an activity (like the screenOrientation).
    - **java**; it contains the java classes.
    - **res**;  It contains .xml resources like drawable, layout or values (such as strings or colors).
                If you want to change how the View is showed you probably need to check the layout folder.
                If you want to add some images data you can do it by adding them to the drawable folder.

# VERSION CONTROL
The local offline part is the same in every branch.
Otherwise the online part was hard for me to make so i split it in various branches:
    - **master** was used to create the online game with Firebase, due to project's requirement
        from my school i decided to pass entirely to Socket.
    - **BTsocket** I used this branch to work with a bluetooth connection. 
    - **Socket** it was used to try a connection with a simple socket.

# OTHER
If there are any problems you can open a github issue or you can first check some online forums to 
be sure is not project related issue.
A more detailed project's architecture and description will be released in Italian soon, stay tuned.

**Thank you for cloning or simply using it**

