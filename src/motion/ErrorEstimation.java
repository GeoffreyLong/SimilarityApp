package motion;

import java.util.LinkedList;
import java.util.List;

import motion.MotionEstimation.ErrorEstimator;

import org.ejml.simple.SimpleMatrix;

public class ErrorEstimation {
	public ErrorEstimator errorEstimator = ErrorEstimator.SAMPSON;
	
	private Double calculateAggregateErrorOnInliers(MotionEstimator estimate, List<Correspondence> inliers) {
		double totalError = 0;
		// SimpleMatrix F = estimate.K.transpose().mult(estimate.E).mult(estimate.K);
		for (Correspondence corr : inliers){
			// totalError += fundamentalErrorEstimation(F, corr);	
			totalError += calculateError(estimate.E, corr);	
		}
		
		return totalError;
	}
	double calculateError(SimpleMatrix E, Correspondence corr){
		// Multiple View Geometry in Computer Vision (Hartley, Zisserman) pg. 287 eq 11.9
		// Sampson distance for Fundamental
		// Best threshold is usually around 0.01
		
		double error = 0;
		double scale = 0;

		error += corr.x2.transpose().mult(E).mult(corr.x1).get(0);
		
		scale += Math.pow(corr.x2.transpose().mult(E).get(0), 2.0);
		scale += Math.pow(corr.x2.transpose().mult(E).get(1), 2.0);
		scale += Math.pow(E.mult(corr.x1).get(0), 2.0);
		scale += Math.pow(E.mult(corr.x1).get(1), 2.0);
		scale = Math.sqrt(scale);
		
		error /= scale;
		error = Math.pow(error, 2.0);
		return error;
	}	
	double fundamentalErrorEstimation(SimpleMatrix F, Correspondence corr){
		double error = 0;
		double scale = 0;
		
		switch(errorEstimator){
		case SAMPSON:
			// Multiple View Geometry in Computer Vision (Hartley, Zisserman) pg. 287 eq 11.9
			// Sampson distance for Fundamental
			error += Math.pow(corr.x2.transpose().mult(F).mult(corr.x1).get(0), 2);
			
			scale += Math.pow(F.mult(corr.x1).get(0), 2.0);
			scale += Math.pow(F.mult(corr.x1).get(1), 2.0);
			scale += Math.pow(F.transpose().mult(corr.x2).get(0), 2.0);
			scale += Math.pow(F.transpose().mult(corr.x2).get(1), 2.0);
			
			error /= scale;
			break;
		case SYMMETRIC_EPIPOLAR:
			// Symmetric epipolar distance (not as good as sampson) for Fundamental
			// Multiple View Geometry in Computer Vision (Hartley, Zisserman) pg. 288 eq 11.10
			error += Math.pow(corr.x2.transpose().mult(F).mult(corr.x1).get(0), 2);

			scale += Math.pow(F.transpose().mult(corr.x2).get(0), 2.0) 
					+ Math.pow(F.transpose().mult(corr.x2).get(1), 2.0)
					+ Math.pow(F.mult(corr.x1).get(0), 2.0) 
					+ Math.pow(F.mult(corr.x1).get(1), 2.0);
			scale /= (Math.pow(F.transpose().mult(corr.x2).get(0), 2.0) 
					+ Math.pow(F.transpose().mult(corr.x2).get(1), 2.0)) 
					* (Math.pow(F.mult(corr.x1).get(0), 2.0) 
					+ Math.pow(F.mult(corr.x1).get(1), 2.0));

			error *= scale;
			break;
		case NO_SCALE:
			// Simplest way of doing it, no scaling
			// Basically worthless, should not use
			error += Math.pow(corr.x1.transpose().mult(F).mult(corr.x2).get(0), 2);
			error += Math.pow(corr.x2.transpose().mult(F).mult(corr.x1).get(0), 2);
			break;
		default:
			break;
		}
		
		return error;
	}
	
	public double calculateError(List<MotionEstimator> estimates, ErrorEstimator errorEstimator){
		this.errorEstimator = errorEstimator;
		double totalError = 0;
		for (MotionEstimator estimate : estimates){
			// SimpleMatrix F = estimate.K.transpose().mult(estimate.E).mult(estimate.K);
			for (Correspondence corr : estimate.correspondences){
				// totalError += fundamentalErrorEstimation(F, corr);	
				totalError += calculateError(estimate.E, corr);	
			}
		}
		
		return totalError;
	}
	public double calculateErrorAvgOverCorr(List<MotionEstimator> estimates, ErrorEstimator errorEstimator){
		this.errorEstimator = errorEstimator;
		double totalError = 0;
		int numberOfCorrs = 0;
		for (MotionEstimator estimate : estimates){
			// SimpleMatrix F = estimate.K.transpose().mult(estimate.E).mult(estimate.K);
			for (Correspondence corr : estimate.correspondences){
				// totalError += fundamentalErrorEstimation(F, corr);	
				totalError += calculateError(estimate.E, corr);	
				numberOfCorrs ++;
			}
		}
		
		return totalError / (double) numberOfCorrs;
	}
	public double calculateInlierErrorAvgOverCorr(List<MotionEstimator> estimates, ErrorEstimator errorEstimator){
		this.errorEstimator = errorEstimator;
		double totalError = 0;
		int numberOfCorrs = 0;
		for (MotionEstimator estimate : estimates){
			// SimpleMatrix F = estimate.K.transpose().mult(estimate.E).mult(estimate.K);
			for (Correspondence corr : estimate.inliers){
				// totalError += fundamentalErrorEstimation(F, corr);	
				totalError += calculateError(estimate.E, corr);	
				numberOfCorrs ++;
			}
		}
		
		return totalError / (double) numberOfCorrs;
	}

	public double calculateError(MotionEstimator estimate){
		double totalError = 0;
		// SimpleMatrix F = estimate.K.transpose().mult(estimate.E).mult(estimate.K);
		for (Correspondence corr : estimate.correspondences){
			// totalError += fundamentalErrorEstimation(F, corr);
			totalError += calculateError(estimate.E, corr);
		}
		
		return totalError;
	}
}
