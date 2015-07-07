package similarity;

import java.util.regex.Pattern;

import org.opencv.core.MatOfDMatch;
import org.opencv.features2d.DMatch;

import main.Camera;
import motion.Sequence;

public class Similarity {
	public Similarity(Sequence sequence){
		Camera[] allCameras = sequence.getAllCameras();
		for (int i = 1; i < sequence.getNumberOfCameras(); i++){
			Camera c1 = allCameras[i-1];
			Camera c2 = allCameras[i];
			MatOfDMatch matches = sequence.getMatches(c1, c2);
			
			featureSimilarity(matches, c1, c2);
			//TODO Besides seeing feature matches can also get contrast similarities, perhaps texture too?
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
}
