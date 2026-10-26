package org.firstinspires.ftc.teamcode.opmodes.auto;

import static org.firstinspires.ftc.teamcode.field.Waypoints.topPark;

import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.seattlesolvers.solverslib.command.Command;
import com.seattlesolvers.solverslib.command.SequentialCommandGroup;
import com.seattlesolvers.solverslib.command.WaitCommand;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.field.Alliance;
import org.firstinspires.ftc.teamcode.field.Route;
import org.firstinspires.ftc.teamcode.field.Waypoint;
import org.firstinspires.ftc.teamcode.field.Waypoints;
import org.firstinspires.ftc.teamcode.opmodes.ForwardOpMode;

@Autonomous
public class TopRed1Tilt extends ForwardOpMode {

    private Robot robot;

    private Route route;

    private Command routine;

    private static Waypoint parkControl = Waypoint.red("parkControl", 67.3, 86.5, 0);

    @Override
    protected void configure(){
        robot = new Robot(hardwareMap, Alliance.RED, Waypoints.topStart);
        route = robot.drive.route(Alliance.RED, Waypoints.topStart);

        Path park = route.curveTo(topPark, parkControl);

        routine = new SequentialCommandGroup(
                robot.shooter.spinUp(),
                new WaitCommand(5000),
                robot.intake.start(),
                robot.transfer.feed(),
                new WaitCommand(2000),
                robot.drive.follow(park)
        );
    }

    @Override
    public void preRun() {
        schedule(routine);
    }
}
