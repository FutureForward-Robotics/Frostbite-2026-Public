package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.robocol.Command;
import com.seattlesolvers.solverslib.command.CommandBase;
import com.seattlesolvers.solverslib.command.InstantCommand;
import com.seattlesolvers.solverslib.command.SequentialCommandGroup;
import com.seattlesolvers.solverslib.command.WaitCommand;
import com.seattlesolvers.solverslib.command.WaitUntilCommand;

public class Shooter extends ForwardSubsystem {

    private DcMotorEx shooterMotor;
    public static final double TICKS_PER_REV = 28;
    public static final double READY_TOL_RPM = 50;

    private double targetRpm;
    private double RPM;

    public Shooter(HardwareMap hardwareMap) {
        shooterMotor = hardwareMap.get(DcMotorEx.class, "shooterMotor");
        shooterMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        shooterMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    @Override
    public void sense() {
        RPM = ticksPerSecondToRpm(shooterMotor.getVelocity());
    }

    @Override
    public void act() {
        if (targetRpm > 0) {
            shooterMotor.setVelocity(rpmToTicksPerSecond(targetRpm));
        }
        else if (targetRpm == 0) {
            shooterMotor.setVelocity(0);
        }
    }
    public void setTargetRpm(double rpm) {
        targetRpm = rpm;
    }
    public double rpm() {
        return RPM;
    }
    public double targetRpm () {
        return targetRpm;
    }
    public boolean atSpeed() {
        return targetRpm > 0 && Math.abs(targetRpm - RPM) < READY_TOL_RPM;
    }
    private double rpmToTicksPerSecond(double rpm) {
        return rpm / 60 * TICKS_PER_REV;
    }
    private double ticksPerSecondToRpm(double ticksPerSecond) {
        return ticksPerSecond * 60 / TICKS_PER_REV;
    }
    public CommandBase spinUp() {
        return new InstantCommand(() -> RPM = targetRpm);
    }

    public CommandBase stop() {
        return new InstantCommand(() -> RPM = 0);
    }

    public CommandBase shootAll(Transfer transfer) {
        return new SequentialCommandGroup(
                spinUp(),
                new WaitUntilCommand(this::atSpeed),
                transfer.feed(),
                new WaitCommand(5000)
        );
    }
}
