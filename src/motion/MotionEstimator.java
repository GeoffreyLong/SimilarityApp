package motion;

import java.util.LinkedList;
import java.util.List;

import main.Camera;
import main.Main;
import motion.MotionEstimation.Algorithm;

import org.ejml.simple.SimpleMatrix;
import org.ejml.simple.SimpleSVD;


// Might want to change to MotionEstimator
public class MotionEstimator {		
	private static final double[][] w = {{0,-1,0},{1,0,0},{0,0,1}};
	private static final double[][] wi = {{0,1,0},{-1,0,0},{0,0,1}};
	public List<Correspondence> correspondences;
	public SimpleMatrix E;
	public SimpleMatrix rotation = null;
	public SimpleMatrix translation = null;
	public SimpleMatrix K;
	
	public Camera C1;
	public Camera C2;
	
	public List<Correspondence> inliers;
	
	// 	Possible Params
	//		Outlier Detector (Ransac, etc)
	//		type of alg (8 pt, etc)
	
	public MotionEstimator() {}
	
	public MotionEstimator(Camera C1, Camera C2) {
		this.C1 = C1;
		this.C2 = C2;
	}

	public boolean estimate(Algorithm algorithm, SimpleMatrix A, List<Correspondence> correspondences, SimpleMatrix K){
		this.correspondences = correspondences;
		this.K = K;
		switch(algorithm){
			case EIGHT_POINT:
				return eightPoint(A, correspondences, K);
			default: 
				return false;
		}
	}
	
	public boolean eightPoint(SimpleMatrix A, List<Correspondence> correspondences, SimpleMatrix K){
		SimpleSVD svd = A.svd();
		
		SimpleMatrix V = svd.getV();
		int lastColIndex = V.numCols() - 1;

		double[][] essentialMatrix = new double[3][3];
		for (int row = 0; row < V.numCols(); row++){
			//TODO Might want to check this...
			double eVal = V.get(row, lastColIndex);
			if (eVal < 0.001 && eVal > -0.001) eVal = 0.0;
			essentialMatrix[row / 3][row % 3] = eVal;
		}
		
		if (decomposeMatrix(correspondences, essentialMatrix, K)) return true;
		return false;
	}
	
	
	private boolean decomposeMatrix(List<Correspondence> correspondences, double[][] essentialMatrix, SimpleMatrix K){
		E = new SimpleMatrix(essentialMatrix);
		SimpleSVD svd = E.svd();
		
		// Get the translation from u3 in U = [u1 u2 u3]
		SimpleMatrix t = new SimpleMatrix(3,1);
		t.set(0,0,svd.getU().get(2));
		t.set(1,0,svd.getU().get(5));
		t.set(2,0,svd.getU().get(8));
		
		// Get the rotation as one of the following
		// UW^{-1}V^T, UW_i^{-1}V^T, -UW^{-1}V^T, -UW_i^{-1}V^T
		SimpleMatrix W = new SimpleMatrix(w);
		SimpleMatrix Wi = new SimpleMatrix(wi);
		
		SimpleMatrix correctR = null;
		SimpleMatrix correctT = null;
		
		List<SimpleMatrix> allR = new LinkedList<SimpleMatrix>();
		SimpleMatrix Ra = svd.getU().mult(W.invert()).mult(svd.getV().transpose());
		SimpleMatrix Rb = svd.getU().mult(Wi.invert()).mult(svd.getV().transpose());
		SimpleMatrix Rc = svd.getU().negative().mult(W.invert()).mult(svd.getV().transpose());
		SimpleMatrix Rd = svd.getU().negative().mult(Wi.invert()).mult(svd.getV().transpose());

		if(Ra.determinant() > 0.999 && Ra.determinant() < 1.001){
			allR.add(Ra);
		}
		if(Rb.determinant() > 0.999 && Rb.determinant() < 1.001){
			allR.add(Rb);
		}
		if(Rc.determinant() > 0.999 && Rc.determinant() < 1.001){
			allR.add(Rc);
		}
		if(Rd.determinant() > 0.999 && Rd.determinant() < 1.001){
			allR.add(Rd);
		}
		
		if ((int) allR.size() == 2){			
			Correspondence[] randCorrs = new Correspondence[correspondences.size()];
			for (int j = 0; j < correspondences.size(); j++){
				// int randIdx = (int) Math.random() * correspondences.size();
				randCorrs[j] = correspondences.get(j);
			}
			
			// Number of correspondencepoints checked
			int chiralityErrors = randCorrs.length;
			
			for (SimpleMatrix R : allR){
				// Set R2 to identity
				SimpleMatrix R2 = new SimpleMatrix(3,3);
				R2.zero();
				R2.set(0,0,1);
				R2.set(1,1,1);
				R2.set(2,2,1);
				
				// Set t2 to 0
				SimpleMatrix t2 = new SimpleMatrix(3,1);
				t2.zero();
				
				int errors = 0;
				for (int k = 0; k < randCorrs.length; k++){
					SimpleMatrix possible = triangulate(R, R2, t, t2, K, K, randCorrs[k]);
					if (possible.get(2,0) < 0){
						errors ++;
					}
				}
				
				if (errors < chiralityErrors){
					correctR = R;
					correctT = t;
					chiralityErrors = errors;
				}

				//System.out.println(errors);
				errors = 0;
				for (int k = 0; k < randCorrs.length; k++){
					SimpleMatrix possible = triangulate(R, R2, t.negative(), t2, K, K, randCorrs[k]);

					if (possible.get(2,0) < -0.001){
						errors ++;
					}
				}
				
				if (errors < chiralityErrors){
					correctR = R;
					correctT = t;
					chiralityErrors = errors;
				}
				
				//System.out.println(errors);
				//System.out.println(chiralityErrors);
			}
		}
		else{
			System.out.println("Error in R decomposition");
			System.out.println(Ra.determinant() + " " + Rb.determinant() + " " + Rc.determinant() + " " + Rd.determinant());
			System.out.println();
			return false;
		}	
		
		if (correctR == null || correctT == null){
			System.out.println("Error in finding R and T");
			System.out.println();
			return false;
		}
		
		rotation = correctR;
		translation = correctT;
		return true;
	}
	
	// Can easily make N view triangulation by passing in lists of R's t's and k's... what to do about corrs though?
	public SimpleMatrix triangulate(SimpleMatrix R1, SimpleMatrix R2, SimpleMatrix t1, SimpleMatrix t2, SimpleMatrix K1, SimpleMatrix K2, Correspondence correspondence){
		// projection matrix P = K[R t]
		SimpleMatrix P1 = new SimpleMatrix(3,4);
		for (int i = 0; i < R1.numRows(); i++){
			for (int j = 0; j < R1.numCols(); j++){
				P1.set(i, j, R1.get(i, j));
			}
		}
		for (int i = 0; i < R1.numRows(); i++){
			P1.set(i, 3, t1.get(i, 0));
		}

		if (K1 != null){
			//TODO is K necessary?
			P1 = K1.mult(P1);
		}
		
		
		SimpleMatrix P2 = new SimpleMatrix(3,4);
		for (int i = 0; i < R2.numRows(); i++){
			for (int j = 0; j < R2.numCols(); j++){
				P2.set(i, j, R2.get(i, j));
			}
		}
		for (int i = 0; i < R2.numRows(); i++){
			P2.set(i, 3, t2.get(i, 0));
		}
		if (K2 != null){
			//TODO is K necessary?
			P2 = K2.mult(P2);
		}

		

		SimpleMatrix A = new SimpleMatrix(4,4);

		// Need the non K altered points
		double u = correspondence.origX1.get(0);
		double v = correspondence.origX1.get(1);
		double u1 = correspondence.origX2.get(0);
		double v1 = correspondence.origX2.get(1);
		
		A.set(0, 0, u*P1.get(2, 0) - P1.get(0,0));
		A.set(0, 1, u*P1.get(2, 1) - P1.get(0,1));
		A.set(0, 2, u*P1.get(2, 2) - P1.get(0,2));
		A.set(0, 3, u*P1.get(2, 3) - P1.get(0,3));

		A.set(1, 0, v*P1.get(2, 0) - P1.get(1,0));
		A.set(1, 1, v*P1.get(2, 1) - P1.get(1,1));
		A.set(1, 2, v*P1.get(2, 2) - P1.get(1,2));
		A.set(1, 3, v*P1.get(2, 3) - P1.get(1,3));

		A.set(2, 0, u1*P2.get(2, 0) - P2.get(0,0));
		A.set(2, 1, u1*P2.get(2, 1) - P2.get(0,1));
		A.set(2, 2, u1*P2.get(2, 2) - P2.get(0,2));
		A.set(2, 3, u1*P2.get(2, 3) - P2.get(0,3));

		A.set(3, 0, v1*P2.get(2, 0) - P2.get(1,0));
		A.set(3, 1, v1*P2.get(2, 1) - P2.get(1,1));
		A.set(3, 2, v1*P2.get(2, 2) - P2.get(1,2));
		A.set(3, 3, v1*P2.get(2, 3) - P2.get(1,3));

		for (int i = 0; i < A.numRows(); i++){
			for (int j = 0; j < A.numCols(); j++){
				if (A.get(i, j) < 0.001 && A.get(i, j) > -0.001) A.set(i,j,0.0);
			}
		}
		
		SimpleSVD svd = A.svd();
		SimpleMatrix V = svd.getV();
		
		// triangulation is last right svd of A
		SimpleMatrix triangulation = new SimpleMatrix(4,1);
		triangulation.set(0, 0, V.get(0,3) / V.get(3,3));
		triangulation.set(1, 0, V.get(1,3) / V.get(3,3));
		triangulation.set(2, 0, V.get(2,3) / V.get(3,3));
		triangulation.set(3, 0, V.get(3,3) / V.get(3,3));
		
		// Mostly seems to be 0.707 for all last row.. not right that one... I think it needs to be 1
		// triangulation.print();
		// System.out.println(triangulation.get(3, 0));
		
		return triangulation;
	}
	
	public void setInliers(List<Correspondence> inliers){
		this.inliers = inliers;
	}
}
