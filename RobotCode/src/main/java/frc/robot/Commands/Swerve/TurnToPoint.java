package frc.robot.Commands.Swerve;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.Subsystems.Swerve;
import frc.robot.Utils.Consts;
import frc.robot.Utils.Vector2d;
    
public class TurnToPoint extends CommandBase implements Consts{
    //target point
    private Vector2d m_target;

    public TurnToPoint(Vector2d target){
        m_target = target;
    }

    public TurnToPoint(double targetX, double targetY){
        m_target = new Vector2d(targetX, targetY);
    }

    @Override
    public void execute() {
        //get current position of robot
        Vector2d currentPos = Swerve.getInstance(ChassisValues.USES_ABS_ENCODER).getPos();
        // double targetAngle = -Math.toDegrees(deltaPos.theta() * Math.signum(deltaPos.x)); // 180  -
        double targetAngle = Math.toDegrees(Math.atan2(m_target.x - currentPos.x, m_target.y - currentPos.y)) -
            Swerve.getInstance(ChassisValues.USES_ABS_ENCODER).getOffsetAngle();
        SmartDashboard.putNumber("target x", m_target.x);
        SmartDashboard.putNumber("target y", m_target.y);
        SmartDashboard.putNumber("target angle", targetAngle); 
        //rotate swerve to point
        Swerve.getInstance(ChassisValues.USES_ABS_ENCODER).rotateTo(targetAngle);
    }
    
}
