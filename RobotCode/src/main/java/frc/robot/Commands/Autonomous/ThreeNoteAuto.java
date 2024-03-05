package frc.robot.Commands.Autonomous;

import java.util.ArrayList;
import java.util.function.BooleanSupplier;

import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import frc.robot.Robot;
import frc.robot.Commands.Intake.IntakeWithoutPID;
import frc.robot.Commands.Shooter.ShootToSpeaker;
import frc.robot.Commands.Shooter.TurnShooterToSpeaker;
import frc.robot.Commands.Swerve.FollowRoute;
import frc.robot.Commands.Swerve.TurnToSpeaker;
import frc.robot.Commands.Swerve.TurnToSpeakerAuto;
import frc.robot.Subsystems.Intake;
import frc.robot.Subsystems.Shooter;
import frc.robot.Subsystems.Swerve;
import frc.robot.Utils.Consts;
import frc.robot.Utils.Funcs;
import frc.robot.Utils.SwervePoint;
import frc.robot.Utils.Vector2d;

public class ThreeNoteAuto extends Command implements Consts{ 

        public ThreeNoteAuto(){ 
            addRequirements(Swerve.getInstance(ChassisValues.USES_ABS_ENCODER)); 
          
          } 
     
        @Override 
        public void initialize() { 

            Vector2d speaker = Funcs.getSpeaker2d();
            Swerve.getInstance(ChassisValues.USES_ABS_ENCODER).resetOdometry(); 
            
            Swerve.getInstance(ChassisValues.USES_ABS_ENCODER).setOdometryVals((Robot.getAlliance() == Alliance.Red) ? 15.5 : 0.5,
                                                                               (Robot.getAlliance() == Alliance.Red) ? 5.5 : 5.5,
                                                                               (Robot.getAlliance() == Alliance.Red) ? 270 : 90);
            

            ArrayList<SwervePoint> posList = new ArrayList<SwervePoint>();
            posList.add(new SwervePoint((Robot.getAlliance() == Alliance.Red) ? 15.5 - 2.2: 0.5 + 2.2
                                          ,(Robot.getAlliance() == Alliance.Red) ? 5.5 : 5.5 
                                          ,(Robot.getAlliance() == Alliance.Red) ? 270 : 90));
            FollowRoute driveRoute = new FollowRoute(posList);

            ArrayList<SwervePoint> rotate180 = new ArrayList<SwervePoint>();
            
            FollowRoute rotate180Route = new FollowRoute(rotate180);

            ArrayList<SwervePoint> returnPosList = new ArrayList<SwervePoint>();
            
            FollowRoute returnDriveRoute = new FollowRoute(returnPosList);

            new SequentialCommandGroup(
                 //shoot first note to speaker 
                 new ParallelCommandGroup(new InstantCommand(() -> {Shooter.getInstance().turnToAngle(114);}),
                                     new InstantCommand(()->{Shooter.getInstance().setShootSpeed(7000, ShooterValues.SPEAKER_SHOOT_SPEED * 1.1 / 3);}))
                ,new WaitCommand(2)
                ,new InstantCommand(() -> {Shooter.getInstance().pushNoteToRollers(ShooterValues.CONTAINMENT_SPEED); }) 
                ,new WaitCommand(0.5)
                ,new InstantCommand(() -> {Shooter.getInstance().pushNoteToRollers(0); 
                                          Shooter.getInstance().setShootSpeed(0);
                                          Shooter.getInstance().stopRollers();
                                          Intake.getInstance().stopMotor();})
                //second note
                ,new ParallelCommandGroup(driveRoute, new WaitCommand(0.2).andThen(new IntakeWithoutPID(IntakeValues.INTAKE_SPEED)))
                .until(new BooleanSupplier() {
                  @Override
                  public boolean getAsBoolean() {
                      return driveRoute.getIsFinished() && Shooter.getInstance().isNoteIn();
                  }
                }).withTimeout(2.5)
                , new InstantCommand(() -> {
                  Shooter.getInstance().pullNoteWithoutPID(-0.3);
                  // Shooter.getInstance().pushNoteToRollers(0);
                })
                ,new WaitCommand(1.5)
                ,new InstantCommand(() -> { 
                                          
                                          Shooter.getInstance().testMotors(0);
                                        })
                ,new WaitCommand(0.5)
                ,new InstantCommand(() -> {rotate180.clear(); 
                                          rotate180.add(new SwervePoint(Swerve.getInstance(ChassisValues.USES_ABS_ENCODER).getX(), 
                                                        Swerve.getInstance(ChassisValues.USES_ABS_ENCODER).getY(),
                                                        Swerve.getInstance(ChassisValues.USES_ABS_ENCODER).getFieldOrientedAngle() + 180));
                                        // Shooter.getInstance().stopRollers();
                                      })
                ,rotate180Route
                ,new InstantCommand(() -> {returnPosList.add(new SwervePoint((Robot.getAlliance() == Alliance.Red) ? 15.5 - 1: 0.5 + 1
                                          ,(Robot.getAlliance() == Alliance.Red) ? 5.5 : 5.5 
                                          ,Swerve.getInstance(ChassisValues.USES_ABS_ENCODER).getFieldOrientedAngle()));})
                ,returnDriveRoute
                ,new ParallelCommandGroup(new TurnToSpeakerAuto())
                ,new ParallelCommandGroup(
                                          new TurnShooterToSpeaker(), new InstantCommand(() -> {Shooter.getInstance().setShootSpeed(ShooterValues.SPEAKER_SHOOT_SPEED);})).until(new BooleanSupplier() {
                    @Override
                    public boolean getAsBoolean() {
                      return Shooter.getInstance().isReadyToShoot();
                    }
                 }).withTimeout(2.5)
                ,new WaitCommand(0.5)
                ,new InstantCommand(() ->{ Shooter.getInstance().pushNoteToRollers(ShooterValues.CONTAINMENT_SPEED);})
                ,new WaitCommand(0.5)
                ,new InstantCommand(() ->{ Shooter.getInstance().pushNoteToRollers(0);})
                ,new InstantCommand(() -> {Shooter.getInstance().stopRollers(); Intake.getInstance().stopMotor();})
                
            ).schedule();
        } 
        @Override 
        public void execute() { 
    
             
        } 
     
        @Override 
        public void end(boolean interrupted) { 
            Swerve.getInstance(ChassisValues.USES_ABS_ENCODER).stop(); 
        } 
     
}
