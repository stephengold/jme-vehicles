# Release log for the MaVehicles library

## Version 0.7.1 released on TBD

+ API changes:
  + Used the `Steering` enum to simplify the APIs
    of the `Bike`, `Vehicle`, and `Wheel` classes.
  + Added `static` qualifiers to 5 protected methods:
    + `Sky.getAmbientLight()`
    + `Sky.getApplication()`
    + `Sky.getDirectionalLight()`
    + `Sky.getShadowRenderer()`
    + `Vehicle.addPassenger()`
  + Protected the constructors of 6 abstract classes:
    + `Bike`
    + `Engine`
    + `Sky`
    + `TireSettings`
    + `Vehicle`
    + `World`
+ Bugfixes:
  + sounds aren't always muted before being detached
  + drop orientation isn't taken into account by `Prop.addToWorld()`
+ New library features:
  + Added `findStartLocation()`, `isLoaded()`, and `scaledHeight()` methods
    to the `Prop` class.
  + Added `areShadowsEnabled()` and `setShadowsEnabled()` methods
    to the `Sky` class.
  + Added the `Steering` enum to describe how wheels are steered.
+ Updated 17 textures provided by the Georg project.
+ Began using the CheckStyle tool.
+ Upgrade JME to v3.6.0-stable, Garrett to v0.5.3, Heart to v8.3.2,
  and Minie to v7.4.0 .

## Version 0.7.0 released on 7 October 2021

+ API change: renamed `calcLongtitudeTireForce()` method in `PacejkaTireModel`.
+ Added support for articulated vehicles:  those which include multiple bodies.
+ Added `Bike` subclass of `Vehicle`, for 2-wheeled articulated vehicles,
  with example asset derived from "Classic Motorcycle" by Hector Mora.
+ Added a subsystem for "props" (non-vehicle rigid bodies) with example assets
  derived from "Barrier & Traffic Cone Pack" by Sabri Ayeş.
+ Added "passengers" (animated humanoids) to vehicles,
  with an example asset created using MakeHuman v1.2.0 .
+ Added many methods to the `Vehicle` class, including:
  + `addPassenger()`
  + `addToPhysicsSpace()`
  + an `addToWorld()` that specifies position
  + `contactTest()`
  + `getMass()`
  + `isLoaded()`
  + `removeFromPhysicsSpace()`
  + `setMass()`
  + a `setChassis()` that specifies the controlled spatial
  + `setSteeringRatio()`
  + `toString()`
  + a `warp()` that specifies position
  + `warpAllBodies()`
+ Upgrade Garrett to v0.2.0, Heart to v7.1.0, and Minie to v4.4.0 .

## Version 0.6.2 released on 9 June 2021

Upgrade JME to v3.4.0-stable, Garrett to v0.1.3, Heart to v6.4.4,
LemurPower to v0.4.2, Minie to v4.1.1, SkyControl to v0.9.32,
and jme-ttf to v2.2.4 .

## Version 0.6.1+for34 released on 9 May 2021

Upgrade JME to v3.4.0-beta3, Heart to v6.4.3+for34, Minie to v4.1.0+for34, and
jme-ttf to v2.2.3+for34 .

## Version 0.6.0 released on 11 February 2021

+ API changes:
  + specify a `PhysicsSpace` in the `World.attach()` method
  + changed the semantics of a vehicle's "accelerate" control signal,
     which now ranges from 0 to 1 instead of -1 to +1
  + made the engine sound an `Engine` property (instead of a `Vehicle` one)
  + replaced `Sound.addOgg()` with `addAssetPath()` and publicized it
  + deleted public method `Vehicle.getRotation()`
  + moved the `TireGraph` class out of the MaVehicles library
+ bugfix: engine sounds don't move with the Vehicle
+ Publish to MavenCentral instead of JCenter
+ new features:
  + `SteeringWheelState` (split off from the `DriverHud` class of MavDemo1)
  + an alternative `Vehicle.setChassis()` that doesn't require assets
  + `HasNode` and `VehicleSteering` interfaces
  + `Sound.isPositional()` and `Sound.setPositional()` methods
  + assign a name to each `AudioNode`
  + assign application data to each `PhysicsCollisionObject`

## Version 0.5.0 released on 31 January 2021

The initial baseline release,
based on code and assets formerly in the MavCommon subproject.
