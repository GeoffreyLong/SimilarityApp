package similarity;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.highgui.Highgui;

import main.Camera;
import motion.Sequence;

public class Similarity {
	private static final int NUM_CAMERAS = 0;
	List<Camera> allCameras = new LinkedList<Camera>();
	
	public Similarity(String filepath){
		// Will error out with insufficient memory on relatively low (~100) photos
		File directory = new File(filepath);
        File[] listOfFiles = directory.listFiles();
        Mat[] allMats = new Mat[listOfFiles.length];

        
        // Initialize first
        allMats[0] = Highgui.imread(listOfFiles[0].getAbsolutePath());
		allCameras.add(initializeCamera(allMats[0], listOfFiles[0].getAbsolutePath()));
		
        // Iterate through comparing each file to one another
		// Creating the camera takes a while so that is done lazily
        for (int i = 0; i < listOfFiles.length; i++){
			try{
				// f.getAbsolutePath(); or f.getName();
				allMats[i] = Highgui.imread(listOfFiles[i].getAbsolutePath());
				allCameras.add(initializeCamera(allMats[i], listOfFiles[i].getAbsolutePath()));

				for (int j = 1; j < i; j++){
					Camera c1 = allCameras.get(i);
					Camera c2 = allCameras.get(j);
					MatOfDMatch matches = getMatches(c1, c2);
					
					featureSimilarity(matches, c1, c2);
					//TODO Besides seeing feature matches can also get contrast similarities, perhaps texture too?
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		
	}
	
	// Need to pipe out a value from this
	// Currently tested using Star detector, Sift extractor, Flann matcher
	//		Note that this won't give very good results in blurry images, need an edge detector as well
	private void featureSimilarity(MatOfDMatch matches, Camera c1, Camera c2){
		int numberOfMatches = 0;
		int numberOfTotalMatches = 0;
		float smallestMatch = Float.MAX_VALUE;
		for (DMatch match : matches.toArray()){
			if (match.distance < Float.MAX_VALUE){
				//TODO see if I can get the match location in each image (i.e. pixel location)
				// System.out.println(match.imgIdx + " " + match.trainIdx + " " + match.queryIdx);
				numberOfTotalMatches ++;
			}
			// Arbitrarily set value
			if (match.distance < 100){
				numberOfMatches ++;
			}
			if (match.distance < smallestMatch){
				smallestMatch = match.distance;
			}
			//TODO perhaps a sort of average over 10 windows thing?
		}
		
		// File string splitting and printing
		String[] c1Tokes = c1.fileName.split(Pattern.quote(System.getProperty("file.separator")));
		String[] c2Tokes = c2.fileName.split(Pattern.quote(System.getProperty("file.separator")));
		
		System.out.println(c1Tokes[c1Tokes.length-1].split(Pattern.quote("."))[0] + " " 
						+ c2Tokes[c2Tokes.length-1].split(Pattern.quote("."))[0] 
						+ " " + numberOfTotalMatches + " " + numberOfMatches + " " + smallestMatch);
		
		// numberOfTotalMatches: Doesn't really say much about similarity... could be used to scale the other two values
		// numberOfMatches: This is good, usually when this is 0 the images have no correlation
		// smallestMatch: The smaller the more likely similar
	}
	
	private Camera initializeCamera(Mat allMats, String string){
		// STAR, SIFT, FLANN might be best
		// ORB, BRIEF, BRUTEFORCE probably the fastest / least memory
		return new Camera(string, allMats, FeatureDetector.ORB, DescriptorExtractor.BRIEF);
	}
	
	private MatOfDMatch getMatches(Camera c1, Camera c2){
		// Probably a native function in the OpenCV source... might be paired with the alg
		// so if I pass a parameter decide the detector might have to send same to matching
		// Easy if solver is instantiated with class variable of feature or whatever	
		MatOfDMatch matches = new MatOfDMatch();
		DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
		
		//c1 is query
		//c2 is train
		matcher.match(c1.getDescriptors(), c2.getDescriptors(), matches);
		return matches;
	}
}
