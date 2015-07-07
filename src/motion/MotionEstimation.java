package motion;

import java.util.LinkedList;
import java.util.List;

import main.Camera;

import org.ejml.simple.SimpleMatrix;

public class MotionEstimation {
	public enum Algorithm {EIGHT_POINT, SEVEN_POINT};
	public enum OutlierDetector{RANSAC_COUNT, RANSAC_ERROR};
	public enum ErrorEstimator {SAMPSON, SYMMETRIC_EPIPOLAR, NO_SCALE};
	
	private Sequence sequence;
	private double threshold = 0.01;
	private OutlierDetector outlierDetector = OutlierDetector.RANSAC_COUNT;
	private Algorithm algorithm = Algorithm.EIGHT_POINT;
	private ErrorEstimation errorEstimation = new ErrorEstimation();
	private ErrorEstimator errorEstimator;

	public MotionEstimation(Sequence sequence) {
		this.sequence = sequence;
	}
	
	public MotionEstimation(){
		
	}
		
	public void run(int N, double threshold){
		this.threshold  = threshold;
		Camera[] allCameras = sequence.getAllCameras();
		
		// Set initial camera R to identity
		SimpleMatrix initialR = new SimpleMatrix(3,3);
		initialR.zero();
		initialR.set(0,0,1);
		initialR.set(1,1,1);
		initialR.set(2,2,1);
		
		// Set initial camera t to 0
		SimpleMatrix initialt = new SimpleMatrix(3,1);
		initialt.zero();
		
		SimpleMatrix pose = new SimpleMatrix(4,4);
		pose.zero();
		pose.set(0,0,1);
		pose.set(1,1,1);
		pose.set(2,2,1);
		pose.set(3,3,1);

		
		allCameras[0].R = initialR;
		allCameras[0].t = initialt;
		allCameras[0].pose = pose;
		for (int i = 1; i < allCameras.length; i++){
			Camera C1 = allCameras[i-1];
			Camera C2 = allCameras[i];
			List<Correspondence> correspondences = sequence.getCorrespondences(C1, C2, algorithm);
			MotionEstimator estimate = ransac(N, 8, correspondences, sequence.getK());

			SimpleMatrix t = estimate.translation;
			SimpleMatrix R = estimate.rotation;
			SimpleMatrix T = new SimpleMatrix(4,4);
			T.zero();
			T.set(0, 0, R.get(0, 0));
			T.set(1, 0, R.get(1, 0));
			T.set(2, 0, R.get(2, 0));
			T.set(0, 1, R.get(0, 1));
			T.set(1, 1, R.get(1, 1));
			T.set(2, 1, R.get(2, 1));
			T.set(0, 2, R.get(0, 2));
			T.set(1, 2, R.get(1, 2));
			T.set(2, 2, R.get(2, 2));
			T.set(0, 3, t.get(0, 0));
			T.set(1, 3, t.get(1, 0));
			T.set(2, 3, t.get(2, 0));
			T.set(3, 3, 1);
			
			//R.negative().mult(t).print();
			C2.pose = C1.pose.mult(T);
		}
	}
	
	// N is number of iterations
	// s is the subset size for the correspondences
	public MotionEstimator ransac(int N, int s, List<Correspondence> correspondences, SimpleMatrix K){
		
		int n = 0;
		MotionEstimator bestEstimation = null;
		
		double bestEstimationCount = 0;
		switch(outlierDetector){
			case RANSAC_COUNT:
				bestEstimationCount = 0;
				break;
			case RANSAC_ERROR:
				bestEstimationCount = Double.MAX_VALUE;
			default:
				break;
		}
		
		while(n < N){
			MotionEstimator estimation = new MotionEstimator();
			SimpleMatrix randomCorrespondence = generateRandomCorrespondences(correspondences, s);
			if (estimation.estimate(algorithm, randomCorrespondence, correspondences, K)){
				// Inlier Computation Estimation
				
				// SimpleMatrix F = K.transpose().mult(estimation.E).mult(K);
				switch(outlierDetector){
					case RANSAC_COUNT:
						// int estimationCount = getInliers(F, correspondences).size();
						int estimationCount = getInliers(estimation.E, correspondences).size();
						if (estimationCount > bestEstimationCount){
							bestEstimationCount = estimationCount;
							bestEstimation = estimation;
						}
						break;
					case RANSAC_ERROR:
						double totalError = 0;
						for (Correspondence corr : correspondences){
							// totalError += fundamentalErrorEstimation(F, corr);
							totalError += errorEstimation.calculateError(estimation.E, corr);
						}
						if (totalError < bestEstimationCount){
							bestEstimationCount = totalError;
							bestEstimation = estimation;
						}
					default:
						break;
				}
			}	
			n++;
		}

		if (bestEstimation != null){
			MotionEstimator inlierFit = getBestEstimation(bestEstimation, correspondences, K);
			if(inlierFit.translation != null){
				return inlierFit;
			}
			else{
				System.out.println(n + " " + threshold + " error");
				System.out.println("Could not fit inliers, defaulting to best found estimation");
				return bestEstimation;
			}
		}
		return null;
	}

	private MotionEstimator getBestEstimation(MotionEstimator estimate, List<Correspondence> correspondences, SimpleMatrix K){
		// SimpleMatrix F = K.transpose().mult(E).mult(K);
		List<Correspondence> inliers = getInliers(estimate.E, correspondences);
		estimate.setInliers(inliers);
		double[][] aMatrix = new double[inliers.size()][9];
		
		
		for (int i = 0; i < inliers.size(); i++){
			aMatrix[i] = inliers.get(i).correspondence;
		}
		SimpleMatrix A = new SimpleMatrix(aMatrix);
		
		MotionEstimator bestEstimate = new MotionEstimator();
		bestEstimate.estimate(algorithm, A, correspondences, K);
		bestEstimate.setInliers(inliers);

		return bestEstimate;
	}
	
	private List<Correspondence> getInliers(SimpleMatrix E, List<Correspondence> correspondences){
		List<Correspondence> inliers = new LinkedList<Correspondence>();
		
		// TODO different thresholds for different error Estimators
		// TODO find a way to compute correct threshold
		for (Correspondence corr : correspondences){
			// double error = fundamentalErrorEstimation(F, corr);
			double error = errorEstimation.fundamentalErrorEstimation(E, corr);
			if (error < threshold){
				inliers.add(corr);
			}				
		}
		
		return inliers;
	}	
	
	public SimpleMatrix generateRandomCorrespondences(List<Correspondence> correspondences, int s){
		List<Integer> indexList = new LinkedList<Integer>();
		double[][] correspondenceMatrix = new double[s][9];
		int i = 0;
		
		while(indexList.size() < s){
			//TODO better with the Random class stuff
			int index = (int) (Math.random() * correspondences.size());

			if (! indexList.contains(index)){
				correspondenceMatrix[i] = correspondences.get(index).correspondence;
				indexList.add(index);
				i ++;
			}
		}
		
		return new SimpleMatrix(correspondenceMatrix);
	}
}
