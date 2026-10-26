package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.command.CommandBase;
import com.seattlesolvers.solverslib.command.InstantCommand;
import com.seattlesolvers.solverslib.command.SequentialCommandGroup;
import com.seattlesolvers.solverslib.command.WaitUntilCommand;

import org.firstinspires.ftc.teamcode.util.BusWrites;
import org.firstinspires.ftc.teamcode.util.RunLog;

import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

public class Transfer extends ForwardSubsystem {
    public enum State {
        INTAKING,
        BACKING_OFF,
        TRANSFERRING,
        EJECTING,
        IDLE

    }

    public static final long BACK_OFF_DURATION_NS = TimeUnit.MILLISECONDS.toNanos(100);
    private DcMotorEx transferMotor;
    private State currentstate = State.IDLE;
    private long backOffStartTsNano;
    private LongSupplier tsSupplier;
    private double lastPower = Double.NaN;
    private RunLog log = RunLog.disabled();
    private State loggedState = null;


    public Transfer(HardwareMap hardwareMap, LongSupplier timestamp) {
        transferMotor = hardwareMap.get(DcMotorEx.class, "transferMotor");
        transferMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        transferMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        tsSupplier = timestamp;
    }

    public Transfer(HardwareMap hardwareMap) {
        this(hardwareMap, System::nanoTime);

    }

    @Override
    public void sense(){
    }
    @Override
    public void act(){
        if (currentstate == State.BACKING_OFF
                && tsSupplier.getAsLong() - backOffStartTsNano >= BACK_OFF_DURATION_NS) {
            currentstate = State.IDLE;
        }

        if (currentstate != loggedState) {
            log.event("transfer.state", currentstate.name());
            loggedState = currentstate;
        }

        double power;
        switch (currentstate) {
            case INTAKING:
                power = .5;
                break;
            case BACKING_OFF:
            case EJECTING:
                power = -1;
                break;
            case TRANSFERRING:
                power = 1;
                break;
            case IDLE:
            default:
                power = 0;
                break;
        }

        if (BusWrites.worthMotor(power, lastPower)) {
            transferMotor.setPower(power);
            lastPower = power;
        }
    }

    public CommandBase load(){return new InstantCommand(() -> currentstate = State.INTAKING);
    }

    public CommandBase backOff(){return new SequentialCommandGroup(
            new InstantCommand(() -> {
                currentstate = State.BACKING_OFF;
                backOffStartTsNano = tsSupplier.getAsLong();
            }),
            new WaitUntilCommand(() -> currentstate != State.BACKING_OFF));
    }
    public CommandBase feed(){return new InstantCommand(() -> currentstate = State.TRANSFERRING);
    }
    public CommandBase reverse(){return new InstantCommand(() -> currentstate = State.EJECTING);
    }

    public CommandBase stop(){return new InstantCommand(() -> currentstate = State.IDLE);
    }

    @Override
    public void logSignals(RunLog log) {
        this.log = log;
        log.addSignal("transfer.state", () -> currentstate.ordinal());
        log.addSignal("transfer.power", () -> lastPower);
    }

}
