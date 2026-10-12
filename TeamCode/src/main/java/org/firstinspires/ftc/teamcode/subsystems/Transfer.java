package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.seattlesolvers.solverslib.command.CommandBase;
import com.seattlesolvers.solverslib.command.InstantCommand;

import org.firstinspires.ftc.teamcode.util.BusWrites;

public class Transfer extends ForwardSubsystem {
    public enum State {
        INTAKING,
        TRANSFERRING,
        EJECTING,
        IDLE

    }
    private DcMotorEx transferMotor;
    private Servo gate;
    private State currentstate = State.IDLE;
    public Transfer(HardwareMap hardwareMap) {
        transferMotor = hardwareMap.get(DcMotorEx.class, "transferMotor");
        gate = hardwareMap.get(Servo.class, "gate");
    }

    @Override
    public void sense(){
    }
    @Override
    public void act(){
        switch (currentstate) {
            case INTAKING:
                transferMotor.setPower(.5);
                gate.setPosition(1);
                break;
            case IDLE:
                transferMotor.setPower(0);
                gate.setPosition(1);
                break;
            case TRANSFERRING:
                transferMotor.setPower(1);
                gate.setPosition(0);
                break;
            case EJECTING:
                transferMotor.setPower(-1);
                gate.setPosition(1);
                break;
        }
    }

    public CommandBase load(){return new InstantCommand(() -> currentstate = State.INTAKING);
    }
    public CommandBase feed(){return new InstantCommand(() -> currentstate = State.TRANSFERRING);
    }
    public CommandBase reverse(){return new InstantCommand(() -> currentstate = State.EJECTING);
    }

    public CommandBase stop(){return new InstantCommand(() -> currentstate = State.IDLE);
    }

//Not sure where/how to implement the BusWrites.worthServo(...) in the Subset Create task

}
