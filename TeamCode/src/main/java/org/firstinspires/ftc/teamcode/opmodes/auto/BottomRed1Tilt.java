package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.seattlesolvers.solverslib.command.Command;
import com.seattlesolvers.solverslib.command.ParallelCommandGroup;
import com.seattlesolvers.solverslib.command.SequentialCommandGroup;
import com.seattlesolvers.solverslib.command.WaitCommand;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.field.Alliance;
import org.firstinspires.ftc.teamcode.field.Route;
import org.firstinspires.ftc.teamcode.field.Waypoint;
import org.firstinspires.ftc.teamcode.field.Waypoints;
import org.firstinspires.ftc.teamcode.opmodes.ForwardOpMode;

@Autonomous
public class BottomRed1Tilt extends ForwardOpMode {
    private Robot robot;
    private Route route;
    private Command routine;

    private static Waypoint parkControl = Waypoint.red("parkControl", 11.8, 58, 0);

    @Override
    protected void configure() {
        robot = new Robot(hardwareMap, Alliance.RED, Waypoints.bottomStart);
        route = robot.drive.route(Alliance.RED, Waypoints.bottomStart);

        Path park = route.curveTo(Waypoints.park, parkControl);


        routine = new SequentialCommandGroup(
                // ramp up shooter
                robot.shooter.spinUp(),
                new WaitCommand(1000),
                // shoot
                robot.intake.start(),
                robot.transfer.feed(),
                new WaitCommand(3000),
                // drive to park
                robot.drive.follow(park)
        );

    }


    @Override
    public void preRun() {
        schedule(routine);
    }
}
