package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.robocol.Command;
import com.seattlesolvers.solverslib.command.CommandBase;
import com.seattlesolvers.solverslib.command.InstantCommand;
import com.seattlesolvers.solverslib.command.SequentialCommandGroup;
import com.seattlesolvers.solverslib.command.WaitCommand;
import com.seattlesolvers.solverslib.command.WaitUntilCommand;

import org.firstinspires.ftc.teamcode.util.BusWrites;
import org.firstinspires.ftc.teamcode.util.MathUtil;
import org.firstinspires.ftc.teamcode.util.RunLog;

public class Shooter extends ForwardSubsystem {

    private DcMotorEx shooterMotor;
    private DcMotorEx topShooter;

    public static final double TICKS_PER_REV = 28;
    public static final double READY_TOL_RPM = 100;

    public static final double KF = 0.612 / 3150;
    public static final double KP = 0.0006;
    public static final double KI = 0.002;
    public static final double MAX_INTEGRAL = 100;
    public static final double INTEGRAL_ZONE_RPM = 200;

    private double targetRpm;
    private double currentRpm = 0;
    private double power = 0;
    private double integral = 0;
    private double lastPower = Double.NaN;
    private long lastTime = System.nanoTime();
    private double RPM = 2600;

    public Shooter(HardwareMap hardwareMap) {
        shooterMotor = hardwareMap.get(DcMotorEx.class, "shooterMotor");
        shooterMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        topShooter = hardwareMap.get(DcMotorEx.class, "topShooter");
        topShooter.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    @Override
    public void sense() {
        currentRpm = shooterMotor.getVelocity() / 28 * 60;
    }

    @Override
    public void act() {
        long now = System.nanoTime();
        double dt = (now - lastTime) / 1e9;
        lastTime = now;

        if (targetRpm > 0) {
            double error = targetRpm - currentRpm;
            if (Math.abs(error) < INTEGRAL_ZONE_RPM) {
                integral += error * dt;
            }
            integral = MathUtil.clamp(integral, -MAX_INTEGRAL, MAX_INTEGRAL);
            power = KF * targetRpm + KP * error + KI * integral;
            power = MathUtil.clamp(power, 0, 1);
        } else {
            integral = 0;
            power = 0;
        }

        if (BusWrites.worthMotor(power, lastPower)) {
            shooterMotor.setPower(power);
            topShooter.setPower(power);
            lastPower = power;
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
        return targetRpm > 0 && Math.abs(targetRpm - currentRpm) < READY_TOL_RPM;
    }
    private double rpmToTicksPerSecond(double rpm) {
        return rpm / 60 * TICKS_PER_REV;
    }
    private double ticksPerSecondToRpm(double ticksPerSecond) {
        return ticksPerSecond * 60 / TICKS_PER_REV;
    }
    public CommandBase spinUp() {
        return new InstantCommand(() -> targetRpm = RPM);
    }

    public CommandBase stop() {
        return new InstantCommand(() -> targetRpm = 0);
    }

    public CommandBase increaseRPM() {
        return new InstantCommand(() -> RPM += 25);
    }

    public CommandBase decreaseRPM() {
        return new InstantCommand(() -> RPM -= 25);
    }

    public CommandBase shootAll(Transfer transfer) {
        return new SequentialCommandGroup(
                transfer.backOff(),
                spinUp(),
                new WaitUntilCommand(this::atSpeed),
                transfer.feed(),
                new WaitCommand(5000)
        );
    }




    @Override
    public void logSignals(RunLog log) {
        log.addSignal("shooter.rpm", () -> currentRpm);
        log.addSignal("shooter.power", () -> power);
        log.addSignal("shooter.targetRpm", () -> targetRpm);
        log.addFlag("shooter.atSpeed", this::atSpeed);
        log.addSignal("shooter.integral", () -> integral);
    }
}
