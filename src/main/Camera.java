package main;

import java.util.List;

import org.ejml.simple.SimpleMatrix;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.KeyPoint;

public class Camera {
	// Array of features
	// Camera rotation matrix R (first camera rotation is I?)
	// Camera position t (first camera is at 0,0,0)
	// Camera Intrinsics K
	private Mat image;
	private MatOfKeyPoint keypoints = new MatOfKeyPoint();
	private List<KeyPoint> listOfKeypoints;
	private Mat descriptors = new Mat();
	//public SimpleMatrix pose = new SimpleMatrix(4,4);
	public SimpleMatrix pose;
	public SimpleMatrix t = new SimpleMatrix(3,1);
	public SimpleMatrix R = new SimpleMatrix(3,3);
	public String fileName = "";
	
	// Remember that Camera matrix P is K[R t]
	// This matrix will project a 3d point p into 2d point x via x = Pp
	// The essential matrix is E = t_x R
	
	// For the 8 point algorithm need to compute the local ray directions 
	// This is done by \hat{x} = k^{-1} x 
	//		where \hat{x} is the local ray and x is the point, K is the intrinsics
	
	public Camera(Mat image, int featureDetector, int descriptorExtractor) {
		this.image = image;

		detectAndExtractFeatures(featureDetector, descriptorExtractor);
	}
	
	public Camera(String fileName, Mat image, int featureDetector, int descriptorExtractor) {
		this.fileName = fileName;
		this.image = image;

		detectAndExtractFeatures(featureDetector, descriptorExtractor);
	}
	
	private void detectAndExtractFeatures(int featureDetector, int descriptorExtractor){
		FeatureDetector detector = FeatureDetector.create(featureDetector);
		detector.detect(image, keypoints);
		listOfKeypoints = keypoints.toList();

		DescriptorExtractor extractor = DescriptorExtractor.create(descriptorExtractor);
		extractor.compute(image, keypoints, descriptors);
	}
	
	public Mat getDescriptors(){		
		return descriptors;
	}
	
	public KeyPoint getKeypointAtIndex(int i){
		return listOfKeypoints.get(i);
	}
	
	public MatOfKeyPoint getFeatures(){
		return keypoints;
	}
}