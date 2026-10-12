package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.field.Alliance;
import org.firstinspires.ftc.teamcode.field.Waypoint;
import org.firstinspires.ftc.teamcode.subsystems.Drive;
import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Shooter;
import org.firstinspires.ftc.teamcode.subsystems.Transfer;
import org.firstinspires.ftc.teamcode.util.PoseStore;

/**
 * Composition root. Builds every subsystem once and holds the alliance for the match. OpModes
 * construct one and add bindings.
 */
public final class Robot {

    public final Alliance alliance;
    public final Drive drive;
    public final Intake intake;
    public final Transfer transfer;

    public final Shooter shooter;

    /** Auto. Starts from a known waypoint and clears the stored pose. */
    public Robot(HardwareMap hardwareMap, Alliance alliance, Waypoint start) {
        this.alliance = alliance;
        PoseStore.clear();
        this.drive = new Drive(hardwareMap, start.pose(alliance));
        this.intake = new Intake(hardwareMap);
        this.transfer = new Transfer(hardwareMap);
        this.shooter = new Shooter(hardwareMap);
    }

    /** Teleop. Resumes the pose auto left behind. */
    public Robot(HardwareMap hardwareMap, Alliance alliance) {
        this.alliance = alliance;
        this.drive = new Drive(hardwareMap);
        this.intake = new Intake(hardwareMap);
        this.transfer = new Transfer(hardwareMap);
        this.shooter = new Shooter(hardwareMap);
    }
}
