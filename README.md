# Gradius 2D Game Engine

## Polytech Grenoble - 4th Year Software Engineering Portfolio

This repository features a custom-built 2D game engine and a fully playable shoot-'em-up game inspired by *Gradius*. Developed entirely in Java 21, this project demonstrates software engineering principles, custom rendering loops, algorithmic entity management, and dynamic behavior parsing.

## Key Technical Features

### Engine Architecture & Rendering

* **Toroidal World Generation:** Implements a wrap-around toroidal world coordinate system on both the X and Y axes.


* **Dynamic Viewport & Scrolling:** Features a constant-speed scrolling viewport with dynamic map generation, seamlessly spawning and despawning obstacles based on camera position.


* **Custom Animation System:** Includes visual particle effects for sand, weapon projectiles, entity destruction, and specific environmental states.



### AI & Entity Behavior (Finite State Machines)

* **Custom GAL Parser:** Entity behaviors are driven by a custom-built parser for `.gal` files, allowing Finite State Machine (FSM) logic to be modified and loaded without recompiling the Java codebase.


* **Dynamic State Transitions:** Bots can alter their FSM and behavior mid-game; for example, a Dragon boss enters an "enraged" state with increased speed and active player-tracking when its health drops below 50%.


* **Kinematic Modeling:** Engineered a multi-part composite entity (Dragon) where trailing body segments smoothly follow the head's historical positions and rotation vectors using a calculated movement delay.



### Gameplay Mechanics

* **Simultaneous Action Processing:** The engine supports executing multiple inputs concurrently, allowing actions like dashing while rotating or moving diagonally.


* **Combat & Collision System:** Utilizes strict bounding box collision detection integrated with a health point (HP) system, post-hit invincibility frames, and visual damage indicators.


* **Advanced Entity Spawning:** Includes wave-based enemy generation, ground-based enemy factory entities, and a multi-entity composite final boss that utilizes zone attacks.


* **Power-up Ecosystem:** Features an in-game drop system for temporary speed boosts, triple-shot modifications, single-hit shields, and three distinct weapon types (Classic, Expanding Laser, Missiles).



## Development & Debugging Tools

Built-in developer tools are accessible in real-time to profile engine performance and monitor entity states.

* **Performance Profiling:** Press *Tab* to monitor FPS, average tick duration, and paint times to manage engine load.


* **Entity Telemetry:** Displays current FSM states, precise position coordinates, velocity/rotation vectors, and collision bounding boxes.


* **Map Overview:** Press *V* to zoom out the camera and view the entire generated map boundaries beyond the active viewport limits.


* **Combined View:** Use *Middle-click* to toggle both the map overview and telemetry data simultaneously.



## Getting Started

This engine requires **Java 21**.

### Standalone Execution

You can run the compiled game directly from the `Moteur` directory:

* **Executable Launcher:** Run `./GradiusGame` (Ensure it has execution rights via `chmod +x GradiusGame` if necessary).
* **JAR File:** Run `java -jar Gradius.jar`

### Setup via Eclipse IDE

1. Open Eclipse and select the `gradius` folder as your workspace.


2. Create a new Java project named `Moteur` using Java 21.


3. **Do not** create a `module-info.java` file.


4. Link the required dependency: Right-click the project -> **Properties > Java Build Path > Libraries** -> **Add External JARs...** -> select `given.jar`.


5. Run the project using the **Run** button. If prompted, select `Main.java` as the primary class.



### Running Tests

1. Right-click the `Moteur` project and navigate to **Properties > Java Build Path > Libraries**.


2. Click **Add Library**, select **JUnit**, and choose **JUnit 6**.


3. Click **Apply and Close**, then execute the test suite via **Run as > JUnit Test**.



## Controls

* **Movement:** Hold *Up*, *Down*, *Left*, or *Right* arrows. Combine two arrows for diagonal movement.


* **Dash / Dodge:** Press *D* simultaneously with any directional arrow to perform a quick dash. Note: This mechanic has a 3-to-5 second cooldown.


* **Shoot:** Hold *Space* to fire your currently equipped weapon.
