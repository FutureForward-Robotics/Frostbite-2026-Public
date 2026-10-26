package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.seattlesolvers.solverslib.command.button.Trigger;
import com.seattlesolvers.solverslib.gamepad.GamepadEx;
import com.seattlesolvers.solverslib.gamepad.GamepadKeys;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.subsystems.Assists;

/** Driver button map. */
public final class DriverBindings {

    private DriverBindings() {}

    private static final double PRECISION_SCALE = 0.35;

    public static void configure(Robot robot, GamepadEx driver, GamepadEx operator) {
        // GamepadEx.getLeftY already negates the raw axis, so forward needs no minus; the X axes
        // are not pre-negated and do.
        robot.drive.setDefaultCommand(
                robot.drive.teleop(
                        driver::getLeftY,
                        () -> -driver.getLeftX(),
                        () -> -driver.getRightX()));


        driver.getGamepadButton(GamepadKeys.Button.LEFT_BUMPER)
                .whenPressed(robot.intake.start())
                .whenPressed(robot.transfer.load())
                .whenReleased(robot.intake.stop())
                .whenReleased(robot.transfer.stop());

        new Trigger(() -> driver.getTrigger(GamepadKeys.Trigger.LEFT_TRIGGER) > 0.5)
                .whenActive(robot.transfer.backOff().andThen(robot.shooter.spinUp()))
                .whenInactive(robot.shooter.stop());

        driver.getGamepadButton(GamepadKeys.Button.RIGHT_BUMPER)
                .whenPressed(robot.transfer.feed())
                .whenReleased(robot.transfer.stop());

        driver.getGamepadButton(GamepadKeys.Button.A)
                .whenPressed(robot.intake.eject())
                .whenReleased(robot.intake.stop());

        driver.getGamepadButton(GamepadKeys.Button.DPAD_UP)
                .whenPressed(robot.shooter.increaseRPM());

        driver.getGamepadButton(GamepadKeys.Button.DPAD_DOWN)
                .whenPressed(robot.shooter.decreaseRPM());

    }
}
