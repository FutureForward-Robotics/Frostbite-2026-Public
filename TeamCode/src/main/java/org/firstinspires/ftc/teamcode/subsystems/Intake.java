package org.firstinspires.ftc.teamcode.subsystems;

import android.view.WindowManager;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.robocol.Command;
import com.seattlesolvers.solverslib.command.CommandBase;
import com.seattlesolvers.solverslib.command.InstantCommand;

public class Intake extends ForwardSubsystem {

    public enum State {
        INTAKING,
        EJECTING,
        IDLE
    }

    private DcMotorEx intakeMotor;
    private State currentState = State.IDLE;

    public Intake(HardwareMap hardwareMap) {
        intakeMotor = hardwareMap.get(DcMotorEx.class, "intakeMotor");
        intakeMotor.setDirection(DcMotorSimple.Direction.REVERSE);
    }


    @Override
    public void sense() {

    }

    @Override
    public void act() {
        switch (currentState) {
            case IDLE:
                intakeMotor.setPower(0);
                break;
            case INTAKING:
                intakeMotor.setPower(1);
                break;
            case EJECTING:
                intakeMotor.setPower(-1);
                break;
        }
    }



    public CommandBase start() {
        return new InstantCommand(() -> currentState = State.INTAKING);
    }

    public CommandBase stop() {
        return new InstantCommand(() -> currentState = State.IDLE);
    }
    public CommandBase eject() {
        return new InstantCommand(() -> currentState = State.EJECTING);
    }






}
