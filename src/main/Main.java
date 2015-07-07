package main;

import motion.MotionEstimation;
import motion.Sequence;

import org.opencv.core.Core;

import similarity.Similarity;

//TODO do a batching, like only pipe through a few at a time


public class Main {	
	public static void main(String[] args){
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
				
		getSimilarity(args);
		// estimateMotion(args);
	}
	
	private static void getSimilarity(String[] args) {
		String photoDir = "C:\\Users\\Geoff\\workspace\\Computer_Vision\\src\\Test_Images";
		
		Sequence sequence = null;
		if (photoDir != ""){
			System.out.println(photoDir);
			sequence = new Sequence(photoDir, null);
			System.out.println("Instantiated Cameras");
		}

		Similarity similarity = new Similarity(sequence);
	}

	public static void estimateMotion(String[] args){
		String photoDir = "C:\\Users\\Geoff\\workspace\\Computer_Vision\\src\\TestSetOne\\image_151_251";
		String k = "C:\\Users\\Geoff\\workspace\\Computer_Vision\\src\\TestSetOne\\K.txt";
		
		Sequence sequence = null;
		
		// Solver instantiated with the file path
		if(args.length == 2) {
			for(String arg : args){
				System.out.println(arg);
			}
			sequence = new Sequence(args[0], args[1]);
			System.out.println("Instantiated Cameras");
		}
		else if (args.length == 1){

		}
		else{
			if (photoDir != "" && k != ""){
				System.out.println(photoDir);
				System.out.println(k);
				sequence = new Sequence(photoDir, k);
				System.out.println("Instantiated Cameras");
			}
			else if (photoDir != ""){
				System.out.println(photoDir);
				sequence = new Sequence(photoDir, null);
				System.out.println("Instantiated Cameras");
			}
			else{
				System.out.println("Wrong Arguments");
				System.exit(0);
			}
		}

		MotionEstimation motionEstimation = new MotionEstimation(sequence);
		motionEstimation.run(100, 0.01);
		for (Camera camera : sequence.getAllCameras()){
			camera.pose.print();
		}
	}
}
