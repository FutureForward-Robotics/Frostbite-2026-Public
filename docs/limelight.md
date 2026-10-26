# Limelight

The Limelight 3A is a camera that runs its own vision processing and reports results to the Robot
Controller. This repository wraps it in `Vision`, which reads the camera once per loop.

The camera supports two uses with different setup requirements.

**Aiming at a target** needs almost no setup. The camera reports the angle to what it sees, and you
turn until that angle is zero.

**Getting the robot's position on the field** needs the field map, the camera's mounting position, and
a heading that agrees with the field coordinate system. If any of them is wrong, the camera reports
an incorrect position that still looks reasonable. Do this second, and check it before you rely on it.

## Setup

1. Plug the Limelight into a USB port on the Control Hub. It appears as an ethernet device.
2. On the Robot Controller configuration screen, find the Limelight in the device list and name it
   `limelight`. The number shown under the name is the IP address the Limelight assigned to the
   Control Hub, not the Limelight's own address.
3. Open the Limelight's web interface from a computer on the same network, at
   `http://limelight.local:5801`.
4. Set up a pipeline. For AprilTags, use an AprilTag pipeline and note its index.
5. For position estimates only: upload this season's field map, and enter the camera's mounting
   position relative to the center of the robot. This is under the 3D settings and includes forward,
   right, and up offsets plus yaw, pitch, and roll.

> **Warning**
> Skipping step 5 does not produce an error. The camera reports its own position instead of the
> robot's, so every estimate is off by however far the camera is mounted from the robot's center.

## OpMode setup

```java
@Override
protected void configure() {
    robot = new Robot(hardwareMap, alliance());
    vision = new Vision(new LimelightCamera(hardwareMap, "limelight"), robot.drive);
}
```

Build `Vision` after the drivetrain. It hands the drivetrain's heading to the camera, and it needs
this loop's heading rather than the previous loop's.

`Vision` is not part of `Robot`, because `hardwareMap.get` throws if the configuration has no device
named `limelight`. Keeping it in the OpModes that use it means a robot without a camera still runs.

Register its signals so the data lands in the run log:

```java
@Override
protected void logSignals(RunLog log) {
    vision.logSignals(log);
}
```

## Aiming

```java
double bearing = vision.txForTag(24);       // degrees, positive to the right
if (!Double.isNaN(bearing)) {
    turret.nudge(-bearing * KP);
}
```

| Method | Returns |
| --- | --- |
| `hasTarget()` | True when the camera saw something in a frame new enough to use |
| `tx()`, `ty()` | Angle from the crosshair to the target, degrees, NaN with no target |
| `sawTag(int id)` | True when that AprilTag is in the current frame |
| `txForTag(int id)` | Angle to one specific tag, NaN when it is not in the frame |
| `sample()` | The whole reading, including the tag list |
| `pipeline(int index)` | Switch pipelines |

Methods return NaN rather than zero when there is no target, because zero means "dead ahead" and
would send a mechanism to the wrong place.

`Vision` reads the camera one time per loop and answers every question from that reading. Do not call
`getLatestResult()` yourself. If you read the camera twice in a loop you can get an angle from one
frame and a position from another.

## Field position

```java
Pose seen = vision.fieldPose();
if (seen != null) {
    // A pose you can use, in the same coordinates as the rest of the code
}
```

`fieldPose()` returns `null` unless all of these are true:

- The frame is valid and no older than 100 ms.
- At least one AprilTag contributed to the estimate.
- The tags averaged no more than 96 inches away.

The camera reports position in the FIRST Tech Challenge field coordinate system, measured in meters
from the center of the field. Pedro Pathing uses inches from a corner. `Field.toPedro` converts
between them, and it is the only place that conversion happens.

> **Warning**
> `Field.toPedro` assumes the field's X axis matches Pedro's X axis. Check this once per season with
> the Vision Check OpMode before relying on any position estimate.

## Verification

Run the **Vision Check** OpMode, in the `test` group. Drive to a spot you can measure, stop, and read
the telemetry.

```
odometry          Pose(72.00, 36.00, 90.0)
camera            Pose(71.60, 36.30, 90.0)
delta now         x -0.4  y +0.3  heading +0.0 deg
tags              2
avgTagDist        41.3 in
staleness         38 ms
```

| Observation | Cause |
| --- | --- |
| `no trusted pose`, tags 0 | Wrong pipeline, no tag in view, or no field map uploaded |
| A constant offset in x and y | The camera's mounting position in the web interface is wrong |
| x and y swapped, or one sign flipped | Pedro's frame is rotated relative to the field frame. Fix `Field.toPedro`. |
| Heading off by a fixed amount | Odometry was started from the wrong heading |

Press Y to set odometry to the camera's estimate. It refuses above 4 inches per second, because a
moving robot bakes the frame's age into the pose.

## Constants

| Constant | Default | When to change it |
| --- | --- | --- |
| `Vision.MAX_STALENESS_MS` | 100 | Rarely. A frame older than this is ignored. |
| `Vision.MAX_POSE_DISTANCE_IN` | 96 | Set it from the `avgTagDist` values you see on this year's field. |
| `LimelightCamera.DEFAULT_POLL_RATE_HZ` | 100 | Rarely. This is the camera's maximum. |
