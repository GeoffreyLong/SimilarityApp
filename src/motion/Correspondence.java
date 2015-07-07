package motion;

import main.Main;

import org.ejml.simple.SimpleMatrix;
import org.opencv.features2d.KeyPoint;

public class Correspondence {
	// uu' u'v u' uv' vv' v' u v 1
	public double[] correspondence = new double[9];
	public double u;
	public double v;
	public double u1;
	public double v1;
	
	// Modified by K
	public SimpleMatrix x1 = new SimpleMatrix(3,1);
	public SimpleMatrix x2 = new SimpleMatrix(3,1);
	
	// Original
	public SimpleMatrix origX1 = new SimpleMatrix(3,1);
	public SimpleMatrix origX2 = new SimpleMatrix(3,1);
	
	public Correspondence(KeyPoint keyPoint, KeyPoint keyPoint2, motion.MotionEstimation.Algorithm algorithm, SimpleMatrix K){
		x1.set(0,0,keyPoint.pt.x);
		x1.set(1,0,keyPoint.pt.y);
		x1.set(2,0,1);
		origX1 = x1.copy();
		
		x2.set(0,0,keyPoint2.pt.x);
		x2.set(1,0,keyPoint2.pt.y);
		x2.set(2,0,1);
		origX2 = x2.copy();
		
		if (K != null){
			x1 = K.invert().mult(x1);
			x2 = K.invert().mult(x2);
		}
		
		u = x1.get(0,0);
		v = x1.get(1,0);
		u1 = x2.get(0,0);;
		v1 = x2.get(1,0);;
		

		

		switch(algorithm){
			case EIGHT_POINT:
				correspondence[0] = u*u1;
				correspondence[1] = u1*v;
				correspondence[2] = u1;
				correspondence[3] = u*v1;
				correspondence[4] = v*v1;
				correspondence[5] = v1;
				correspondence[6] = u;
				correspondence[7] = v;
				correspondence[8] = 1;
				break;
			case SEVEN_POINT:
				// This is Same Same
				correspondence[0] = u*u1;
				correspondence[1] = u1*v;
				correspondence[2] = u1;
				correspondence[3] = u*v1;
				correspondence[4] = v*v1;
				correspondence[5] = v1;
				correspondence[6] = u;
				correspondence[7] = v;
				correspondence[8] = 1;
				break;
			default:
				break;
		}
	}
}
