package motion;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import main.Camera;
import main.Main;
import motion.MotionEstimation.Algorithm;

import org.ejml.simple.SimpleMatrix;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.highgui.Highgui;

public class Sequence {
	private Camera[] allCameras;
	private SimpleMatrix K = null;
	private int featureDetector = FeatureDetector.STAR;
	private int descriptorExtractor = DescriptorExtractor.SIFT;
	private int descriptorMatcher = DescriptorMatcher.FLANNBASED;
	private int NUM_CAMERAS = 20;

	public Sequence(String filePath, String k, int featureDetector, int descriptorExtractor, int descriptorMatcher){
		this.featureDetector = featureDetector;
		this.descriptorExtractor = descriptorExtractor;
		this.descriptorMatcher = descriptorMatcher;
		
		generateMatFromImages(filePath);
		if (k != null){
			generateKFromFile(k);			
		}
	}
	
	public Sequence(String filePath, String k){
		generateMatFromImages(filePath);
		if (k != null){
			generateKFromFile(k);			
		}
	}
	
	public Sequence(Camera[] cameras, int featureDetector, int descriptorExtractor, int descriptorMatcher){
		this.featureDetector = featureDetector;
		this.descriptorExtractor = descriptorExtractor;
		this.descriptorMatcher = descriptorMatcher;
		this.allCameras = cameras;
	}

	public List<Correspondence> getCorrespondences(Camera c1, Camera c2, Algorithm algorithm){
		// Probably a native function in the OpenCV source... might be paired with the alg
		// so if I pass a parameter decide the detector might have to send same to matching
		// Easy if solver is instantiated with class variable of feature or whatever
		List<Correspondence> correspondences = new LinkedList<Correspondence>();
		
		MatOfDMatch matches = getMatches(c1,c2);
		
		for(DMatch match : matches.toList()){
			// System.out.println(match.queryIdx + " " + match.trainIdx);
			correspondences.add(new Correspondence(c1.getKeypointAtIndex(match.queryIdx), c2.getKeypointAtIndex(match.trainIdx), algorithm, K));
		}
		
		return correspondences;
	}
	
	public MatOfDMatch getMatches(Camera c1, Camera c2){
		// Probably a native function in the OpenCV source... might be paired with the alg
		// so if I pass a parameter decide the detector might have to send same to matching
		// Easy if solver is instantiated with class variable of feature or whatever	
		MatOfDMatch matches = new MatOfDMatch();
		DescriptorMatcher matcher = DescriptorMatcher.create(descriptorMatcher);
		
		//c1 is query
		//c2 is train
		matcher.match(c1.getDescriptors(), c2.getDescriptors(), matches);
		return matches;
	}
	
	private void generateKFromFile(String k) {
		List<String> kVals = null;
		try {
			kVals = Files.readAllLines(Paths.get(k));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		K = new SimpleMatrix(3,3);
		for (int i = 0; i < 3; i++){
			String string = kVals.get(i);
			String[] vals = string.split("\\s+");
			
			int j = 0;
			for (String val : vals){
				K.set(i, j, Double.parseDouble(val));	
				j++;
			}
		}
	}

	private void generateMatFromImages(String filepath){
		File directory = new File(filepath);
        File[] listOfFiles = directory.listFiles();
        Mat[] allMats = new Mat[listOfFiles.length];

        allCameras = new Camera[allMats.length];

        for (int i = 0; i < getNumberOfCameras(); i++){
			try{
				// f.getAbsolutePath(); or f.getName();
				allMats[i] = Highgui.imread(listOfFiles[i].getAbsolutePath());
				allCameras[i] = initializeCamera(allMats[i], listOfFiles[i].getAbsolutePath());
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
        
        // initializeCameras(allMats);
	}
	
	private Camera initializeCamera(Mat allMats, String string){
		return new Camera(string, allMats, featureDetector, descriptorExtractor);
	}
		
	public Camera[] getAllCameras(){
		return this.allCameras;
	}
	
	public SimpleMatrix getK(){
		return this.K;
	}

	public int getNumberOfCameras() {
		if (this.NUM_CAMERAS == 0){
			return this.allCameras.length;
		}
		return this.NUM_CAMERAS;
	}
}
