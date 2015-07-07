package similarity;

import java.io.File;
import java.util.Collections;
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
import main.Main;
import motion.Sequence;

public class Similarity {
	private static final int NUM_CAMERAS = 0;
	List<Camera> allCameras = new LinkedList<Camera>();
	
	/**
	 * @param filepath
	 */
	public Similarity(String filepath){
		// Will error out with insufficient memory on relatively low (~100) photos
		File directory = new File(filepath);
        File[] listOfFiles = directory.listFiles();
        Mat[] allMats = new Mat[listOfFiles.length];
        int threshold = 75;

        
        // Initialize first
        allMats[0] = Highgui.imread(listOfFiles[0].getAbsolutePath());
		allCameras.add(initializeCamera(allMats[0], listOfFiles[0].getAbsolutePath()));
		

       for (int i = 0; i < listOfFiles.length; i++){
			try{
				// f.getAbsolutePath(); or f.getName();
				allMats[i] = Highgui.imread(listOfFiles[i].getAbsolutePath());
				allCameras.add(initializeCamera(allMats[i], listOfFiles[i].getAbsolutePath()));

				for (int j = 1; j < i; j++){
					Camera c1 = allCameras.get(i);
					Camera c2 = allCameras.get(j);
					
					//TODO really need some sort of mapping to get groups down
					//		I think I like the idea of finding cycles in a directed graph
					// Try feature matching in both directions
					float featureResult = featureSimilarity(getMatches(c1, c2), c1, c2);
					featureResult += featureSimilarity(getMatches(c2, c1), c1, c2);
					if (featureResult < threshold){
						System.out.println(c1.fileName + " " + c2.fileName);
					}

				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public void similarityTest(String filepath){
		File directory = new File(filepath);
        File[] listOfFiles = directory.listFiles();
        Mat[] allMats = new Mat[listOfFiles.length];

        
        // Initialize first
        allMats[0] = Highgui.imread(listOfFiles[0].getAbsolutePath());
		allCameras.add(initializeCamera(allMats[0], listOfFiles[0].getAbsolutePath()));
		
		//int threshold = 150;
		//TPH: 43
		//FPH: 21
		//FNH: 24
		//TNH: 1394
		//TPL: 34
		//FNL: 137

		
		// int threshold = 100;
		// TPH: 35
		// FPH: 3
		// FNH: 32
		// TNH: 1412
		// TPL: 23
		// FNL: 148
		
		int threshold = 75;
		//TPH: 32
		//FPH: 0
		//FNH: 35
		//TNH: 1415
		//TPL: 15
		//FNL: 156
		
		int TPH, FNH, TPL, FNL, FPH, TNH;
		TPH = FNH = TPL = FNL = FPH = TNH = 0;

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
						
						// Try it both ways
						float featureResult = featureSimilarity(getMatches(c1, c2), c1, c2);
						featureResult += featureSimilarity(getMatches(c2, c1), c1, c2);
						
						// Check to see what value should have been
						String[] c1Tokes = c1.fileName.split(Pattern.quote(System.getProperty("file.separator")));
						String[] c2Tokes = c2.fileName.split(Pattern.quote(System.getProperty("file.separator")));
						
						String c1Name = c1Tokes[c1Tokes.length-1].split(Pattern.quote("."))[0];
						String c2Name = c2Tokes[c1Tokes.length-1].split(Pattern.quote("."))[0];
						
						c1Tokes = c1Name.split("_");
						c2Tokes = c2Name.split("_");
								
						if (c1Tokes[0].equals(c2Tokes[0])){
							if (c1Tokes[1].equals(c2Tokes[1])){
								if (featureResult < threshold){
									// True Positive HARD
									if (Main.test) System.out.println("TPH: " + c1Name + " " + c2Name + " " + featureResult);
									TPH ++;
								}
								else{
									// False Negative HARD
									if (Main.test) System.out.println("FNH: " + c1Name + " " + c2Name + " " + featureResult);
									FNH ++;
								}
							}
							else{
								if (featureResult < threshold){
									// True Positive LITE
									if (Main.test) System.out.println("TPL: " + c1Name + " " + c2Name + " " + featureResult);
									TPL ++;
								}
								else{
									// False Negative LITE
									if (Main.test) System.out.println("FNL: " + c1Name + " " + c2Name + " " + featureResult);
									FNL ++;
								}
							}
						}
						else{
							if (featureResult < threshold){
								// False Positive HARD
								if (Main.test) System.out.println("FPH: " + c1Name + " " + c2Name + " " + featureResult);
								FPH ++;
							}
							else{
								// True Negative HARD
								if (Main.test) System.out.println("TNH: " + c1Name + " " + c2Name + " " + featureResult);
								TNH ++;
							}
						}
						//TODO Besides seeing feature matches can also get contrast similarities, perhaps texture too?
					}
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}

		System.out.println();
		System.out.println();
		System.out.println("TPH: " + TPH);
		System.out.println("FPH: " + FPH);
		System.out.println("FNH: " + FNH);
		System.out.println("TNH: " + TNH);
		System.out.println("TPL: " + TPL);
		System.out.println("FNL: " + FNL);
	}
	
	// Need to pipe out a value from this
	// Currently tested using Star detector, Sift extractor, Flann matcher
	//		Note that this won't give very good results in blurry images, need an edge detector as well
	// Will want to create a sort of mapping
	//		For instance if the correlation between 0_5 and 0_4 is high 
	//		and the correlation between 0_4 and 0_3 is high
	//		then there might be correlation between 0_5 and 0_3 even if the value is a bit high
	//		Need some sort of conditional probability things
	//		Will have to get the probabilities from some sort of run analysis or something
	private float featureSimilarity(MatOfDMatch matches, Camera c1, Camera c2){
		//Sorting the data
		//	Option 1: straight cutoffs
		//		Option 1.1: cutoff by the aggregate value
		//		Option 1.2: Cutoffs with aggregate and smallest match combined in some way
		//	Option 2: Relational
		//		Option 2.1: Put all nodes in a map where capacity is the aggregate find minimum flows
		//		Option 2.2: Nodes point to top picks (under cutoff nodes), cycles are taken
		
		
		float smallestMatch = Float.MAX_VALUE;
		
		int numberOfMatches = 5;
		float[] smallestValues = new float[numberOfMatches];
		
		for (DMatch match : matches.toArray()){
			// Arbitrarily set value
			java.util.Arrays.sort(smallestValues);
			if (smallestValues[0] == 0){
				smallestValues[0] = match.distance;
			}
			else if (match.distance < smallestValues[numberOfMatches - 1]){
				smallestValues[numberOfMatches - 1] = match.distance;
			}

			
			if (match.distance < smallestMatch){
				smallestMatch = match.distance;
			}
		}
		
		float aggregate = 0;
		for (int i = 0; i < numberOfMatches; i++){
			aggregate += smallestValues[i];
		}
		
		aggregate /= numberOfMatches;
		
		
		
		
		// I think that <110 for the average is decent
		if (Main.test){
			// File string splitting and printing
			String[] c1Tokes = c1.fileName.split(Pattern.quote(System.getProperty("file.separator")));
			String[] c2Tokes = c2.fileName.split(Pattern.quote(System.getProperty("file.separator")));
			
			System.out.println(c1Tokes[c1Tokes.length-1].split(Pattern.quote("."))[0] + " " 
							+ c2Tokes[c2Tokes.length-1].split(Pattern.quote("."))[0] 
							+ " " + aggregate + " " + smallestMatch);
		}
		
		return aggregate;
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
