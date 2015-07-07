package test;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;
import motion.Correspondence;
import motion.MotionEstimation;
import motion.MotionEstimator;
import motion.MotionEstimation.Algorithm;
import motion.Sequence;

import org.junit.Test;
import org.opencv.features2d.KeyPoint;
import org.ejml.*;
import org.ejml.simple.SimpleMatrix;

public class Main {

	@Test
	public void exampleOne() {
		SimpleMatrix I1 = new SimpleMatrix(3,10);
		I1.set(0, 0, 0.7914);
		I1.set(0, 1, 0.8204);
		I1.set(0, 2, 0.5424);
		I1.set(0, 3, 0.4462);
		I1.set(0, 4, 0.8685);
		I1.set(0, 5, 0.8824);
		I1.set(0, 6, 0.5805);
		I1.set(0, 7, 0.4651);
		I1.set(0, 8, 0.7285);
		I1.set(0, 9, 0.6280);
		
		I1.set(1, 0, 0.9467);
		I1.set(1, 1, 0.9912);
		I1.set(1, 2, 1.0194);
		I1.set(1, 3, 0.9668);
		I1.set(1, 4, 1.3536);
		I1.set(1, 5, 1.3144);
		I1.set(1, 6, 1.3229);
		I1.set(1, 7, 1.3941);
		I1.set(1, 8, 1.5516);
		I1.set(1, 9, 0.8254);
		
		I1.set(2, 0, 1);
		I1.set(2, 1, 1);
		I1.set(2, 2, 1);
		I1.set(2, 3, 1);
		I1.set(2, 4, 1);
		I1.set(2, 5, 1);
		I1.set(2, 6, 1);
		I1.set(2, 7, 1);
		I1.set(2, 8, 1);
		I1.set(2, 9, 1);
		
		SimpleMatrix I2 = new SimpleMatrix(3,10);
		I2.set(0, 0, 1.5649);
		I2.set(0, 1, 1.3777);
		I2.set(0, 2, 1.1194);
		I2.set(0, 3, 1.2048);
		I2.set(0, 4, 1.6684);
		I2.set(0, 5, 1.4543);
		I2.set(0, 6, 1.1681);
		I2.set(0, 7, 1.2816);
		I2.set(0, 8, 1.4221);
		I2.set(0, 9, 1.2649);
		
		I2.set(1, 0, 0.9467);
		I2.set(1, 1, 0.9912);
		I2.set(1, 2, 1.0194);
		I2.set(1, 3, 0.9668);
		I2.set(1, 4, 1.3536);
		I2.set(1, 5, 1.3144);
		I2.set(1, 6, 1.3229);
		I2.set(1, 7, 1.3941);
		I2.set(1, 8, 1.5516);
		I2.set(1, 9, 0.8254);		
		
		I2.set(2, 0, 1);
		I2.set(2, 1, 1);
		I2.set(2, 2, 1);
		I2.set(2, 3, 1);
		I2.set(2, 4, 1);
		I2.set(2, 5, 1);
		I2.set(2, 6, 1);
		I2.set(2, 7, 1);
		I2.set(2, 8, 1);
		I2.set(2, 9, 1);
		
		SimpleMatrix K = new SimpleMatrix(3,3);
		K.set(0, 0, 1);
		K.set(0, 1, 0);
		K.set(0, 2, 1);
		K.set(1, 0, 0);
		K.set(1, 1, 1);
		K.set(1, 2, 1);
		K.set(2, 0, 0);
		K.set(2, 1, 0);
		K.set(2, 2, 1);
		
		
		double[][] correspondenceMatrix = new double[I1.numCols()][9];
		List<Correspondence> correspondences = new LinkedList<Correspondence>();		
		
		for(int i = 0; i < I1.numCols(); i++){
			KeyPoint keypoint1 = new KeyPoint();
			KeyPoint keypoint2 = new KeyPoint();
			keypoint1.pt.x = I1.get(0, i);
			keypoint1.pt.y = I1.get(1, i);
			
			keypoint2.pt.x = I2.get(0, i);
			keypoint2.pt.y = I2.get(1, i);
			
			Correspondence corr = new Correspondence(keypoint1, keypoint2, Algorithm.EIGHT_POINT, K);
			correspondences.add(corr);
			correspondenceMatrix[i] = corr.correspondence;
		}
		
		SimpleMatrix A = new SimpleMatrix(correspondenceMatrix);
		//TODO assert A as equal to the exampleTwo points in matrix or something
		
		MotionEstimator estimate = new MotionEstimator();
		estimate.eightPoint(A, correspondences, K);
		
		
		
		// TEST VALUES
		SimpleMatrix E_true = new SimpleMatrix(3,3);
		E_true.set(0, 0, 0);
		E_true.set(0, 1, 0);
		E_true.set(0, 2, 0);
		E_true.set(1, 0, 0);
		E_true.set(1, 1, 0);
		E_true.set(1, 2, -0.7071);
		E_true.set(2, 0, 0);
		E_true.set(2, 1, 0.7071);
		E_true.set(2, 2, 0);
		
		SimpleMatrix t_true = new SimpleMatrix(3,1);
		t_true.set(0,0,1);
		t_true.set(1,0,0);
		t_true.set(2,0,0);
		
		SimpleMatrix R_true = new SimpleMatrix(3,3);
		R_true.set(0, 0, 1);
		R_true.set(0, 1, 0);
		R_true.set(0, 2, 0);
		R_true.set(1, 0, 0);
		R_true.set(1, 1, 1);
		R_true.set(1, 2, 0);
		R_true.set(2, 0, 0);
		R_true.set(2, 1, 0);
		R_true.set(2, 2, 1);
		
		Assert.assertTrue(estimate.E.isIdentical(E_true, 0.001));
		Assert.assertTrue(estimate.rotation.isIdentical(R_true, 0.001));
		Assert.assertTrue(estimate.translation.isIdentical(t_true, 0.001));
	}
	
	@Test
	public void testTriangulate() {
		SimpleMatrix I1 = new SimpleMatrix(3,1);
		I1.set(0, 0, 0.7914);
		I1.set(1, 0, 0.9467);
		I1.set(2, 0, 1);
		
		SimpleMatrix I2 = new SimpleMatrix(3,1);
		I2.set(0, 0, 1.5649);
		I2.set(1, 0, 0.9467);
		I2.set(2, 0, 1);
		
		SimpleMatrix K = new SimpleMatrix(3,3);
		K.set(0, 0, 1);
		K.set(0, 1, 0);
		K.set(0, 2, 1);
		K.set(1, 0, 0);
		K.set(1, 1, 1);
		K.set(1, 2, 1);
		K.set(2, 0, 0);
		K.set(2, 1, 0);
		K.set(2, 2, 1);
		
		SimpleMatrix t1 = new SimpleMatrix(3,1);
		t1.set(0,0,-2);
		t1.set(1,0,-2);
		t1.set(2,0,7);
		
		SimpleMatrix t2 = new SimpleMatrix(3,1);
		t2.set(0,0,2);
		t2.set(1,0,-2);
		t2.set(2,0,7);
		
		SimpleMatrix R = new SimpleMatrix(3,3);
		R.set(0, 0, 1);
		R.set(0, 1, 0);
		R.set(0, 2, 0);
		R.set(1, 0, 0);
		R.set(1, 1, 0);
		R.set(1, 2, 1);
		R.set(2, 0, 0);
		R.set(2, 1, -1);
		R.set(2, 2, 0);

		
		double[][] correspondenceMatrix = new double[1][9];
		KeyPoint keypoint1 = new KeyPoint();
		KeyPoint keypoint2 = new KeyPoint();
		keypoint1.pt.x = I1.get(0, 0);
		keypoint1.pt.y = I1.get(1, 0);
		
		keypoint2.pt.x = I2.get(0, 0);
		keypoint2.pt.y = I2.get(1, 0);
		
		Correspondence correspondence = new Correspondence(keypoint1, keypoint2, Algorithm.EIGHT_POINT, K);
		
		
		// Testing data
		SimpleMatrix triangulate_True = new SimpleMatrix(4,1);
		triangulate_True.set(0,0,0.9213);
		triangulate_True.set(1,0,1.8283);
		triangulate_True.set(2,0,1.7246);
		triangulate_True.set(3,0,1);
		
		MotionEstimator estimate = new MotionEstimator();
		assertTrue(estimate.triangulate(R, R, t1, t2, K, K, correspondence).isIdentical(triangulate_True, 0.001));
	}

	
	@Test
	public void testTriangulateTwo() {
		SimpleMatrix I1 = new SimpleMatrix(3,1);
		I1.set(0, 0, 0.7914);
		I1.set(1, 0, 0.9467);
		I1.set(2, 0, 1);
		
		SimpleMatrix I2 = new SimpleMatrix(3,1);
		I2.set(0, 0, 1.5649);
		I2.set(1, 0, 0.9467);
		I2.set(2, 0, 1);
		
		SimpleMatrix K = new SimpleMatrix(3,3);
		K.set(0, 0, 1);
		K.set(0, 1, 0);
		K.set(0, 2, 1);
		K.set(1, 0, 0);
		K.set(1, 1, 1);
		K.set(1, 2, 1);
		K.set(2, 0, 0);
		K.set(2, 1, 0);
		K.set(2, 2, 1);
		
		SimpleMatrix t1 = new SimpleMatrix(3,1);
		t1.set(0,0,0);
		t1.set(1,0,0);
		t1.set(2,0,0);
		
		SimpleMatrix t2 = new SimpleMatrix(3,1);
		t2.set(0,0,1);
		t2.set(1,0,0);
		t2.set(2,0,0);
		
		SimpleMatrix R1 = new SimpleMatrix(3,3);
		R1.set(0, 0, 1);
		R1.set(0, 1, 0);
		R1.set(0, 2, 0);
		R1.set(1, 0, 0);
		R1.set(1, 1, 1);
		R1.set(1, 2, 0);
		R1.set(2, 0, 0);
		R1.set(2, 1, 0);
		R1.set(2, 2, 1);
		
		SimpleMatrix R2 = new SimpleMatrix(3,3);
		R2.set(0, 0, 1);
		R2.set(0, 1, 0);
		R2.set(0, 2, 0);
		R2.set(1, 0, 0);
		R2.set(1, 1, 1);
		R2.set(1, 2, 0);
		R2.set(2, 0, 0);
		R2.set(2, 1, 0);
		R2.set(2, 2, 1);

		
		double[][] correspondenceMatrix = new double[1][9];
		KeyPoint keypoint1 = new KeyPoint();
		KeyPoint keypoint2 = new KeyPoint();
		keypoint1.pt.x = I1.get(0, 0);
		keypoint1.pt.y = I1.get(1, 0);
		
		keypoint2.pt.x = I2.get(0, 0);
		keypoint2.pt.y = I2.get(1, 0);
		
		Correspondence correspondence = new Correspondence(keypoint1, keypoint2, Algorithm.EIGHT_POINT, K);
		
		
		// Testing data
		SimpleMatrix triangulate_True = new SimpleMatrix(4,1);
		triangulate_True.set(0,0,-0.270);
		triangulate_True.set(1,0,-0.069);
		triangulate_True.set(2,0,1.293);
		triangulate_True.set(3,0,1);
		
		MotionEstimator estimate = new MotionEstimator();
		assertTrue(estimate.triangulate(R1, R2, t1, t2, K, K, correspondence).isIdentical(triangulate_True, 0.001));
	}

	
	
	@Test
	public void testRansac() {
		SimpleMatrix I1 = new SimpleMatrix(3,10);
		I1.set(0, 0, 0.7914);
		I1.set(0, 1, 0.8204);
		I1.set(0, 2, 0.5424);
		I1.set(0, 3, 0.4462);
		I1.set(0, 4, 0.8685);
		I1.set(0, 5, 0.8824);
		I1.set(0, 6, 0.5805);
		I1.set(0, 7, 0.4651);
		I1.set(0, 8, 0.7285);
		I1.set(0, 9, 0.6280);
		
		I1.set(1, 0, 0.9467);
		I1.set(1, 1, 0.9912);
		I1.set(1, 2, 1.0194);
		I1.set(1, 3, 0.9668);
		I1.set(1, 4, 1.3536);
		I1.set(1, 5, 1.3144);
		I1.set(1, 6, 1.3229);
		I1.set(1, 7, 1.3941);
		I1.set(1, 8, 1.5516);
		I1.set(1, 9, 0.8254);
		
		I1.set(2, 0, 1);
		I1.set(2, 1, 1);
		I1.set(2, 2, 1);
		I1.set(2, 3, 1);
		I1.set(2, 4, 1);
		I1.set(2, 5, 1);
		I1.set(2, 6, 1);
		I1.set(2, 7, 1);
		I1.set(2, 8, 1);
		I1.set(2, 9, 1);
		
		SimpleMatrix I2 = new SimpleMatrix(3,10);
		I2.set(0, 0, 1.5649);
		I2.set(0, 1, 1.3777);
		I2.set(0, 2, 1.1194);
		I2.set(0, 3, 1.2048);
		I2.set(0, 4, 1.6684);
		I2.set(0, 5, 1.4543);
		I2.set(0, 6, 1.1681);
		I2.set(0, 7, 1.2816);
		I2.set(0, 8, 1.4221);
		I2.set(0, 9, 1.2649);
		
		I2.set(1, 0, 0.9467);
		I2.set(1, 1, 0.9912);
		I2.set(1, 2, 1.0194);
		I2.set(1, 3, 0.9668);
		I2.set(1, 4, 1.3536);
		I2.set(1, 5, 1.3144);
		I2.set(1, 6, 1.3229);
		I2.set(1, 7, 1.3941);
		I2.set(1, 8, 1.5516);
		I2.set(1, 9, 0.8254);		
		
		I2.set(2, 0, 1);
		I2.set(2, 1, 1);
		I2.set(2, 2, 1);
		I2.set(2, 3, 1);
		I2.set(2, 4, 1);
		I2.set(2, 5, 1);
		I2.set(2, 6, 1);
		I2.set(2, 7, 1);
		I2.set(2, 8, 1);
		I2.set(2, 9, 1);
		
		SimpleMatrix K = new SimpleMatrix(3,3);
		K.set(0, 0, 1);
		K.set(0, 1, 0);
		K.set(0, 2, 1);
		K.set(1, 0, 0);
		K.set(1, 1, 1);
		K.set(1, 2, 1);
		K.set(2, 0, 0);
		K.set(2, 1, 0);
		K.set(2, 2, 1);
		
		// TEST VALUES
		SimpleMatrix E_true = new SimpleMatrix(3,3);
		E_true.set(0, 0, 0);
		E_true.set(0, 1, 0);
		E_true.set(0, 2, 0);
		E_true.set(1, 0, 0);
		E_true.set(1, 1, 0);
		E_true.set(1, 2, -0.7071);
		E_true.set(2, 0, 0);
		E_true.set(2, 1, 0.7071);
		E_true.set(2, 2, 0);
		
		SimpleMatrix t_true = new SimpleMatrix(3,1);
		t_true.set(0,0,1);
		t_true.set(1,0,0);
		t_true.set(2,0,0);
		
		SimpleMatrix R_true = new SimpleMatrix(3,3);
		R_true.set(0, 0, 1);
		R_true.set(0, 1, 0);
		R_true.set(0, 2, 0);
		R_true.set(1, 0, 0);
		R_true.set(1, 1, 1);
		R_true.set(1, 2, 0);
		R_true.set(2, 0, 0);
		R_true.set(2, 1, 0);
		R_true.set(2, 2, 1);
		
		
		
		double[][] correspondenceMatrix = new double[I1.numCols()][9];
		List<Correspondence> correspondences = new LinkedList<Correspondence>();		
		
		for(int i = 0; i < I1.numCols(); i++){
			KeyPoint keypoint1 = new KeyPoint();
			KeyPoint keypoint2 = new KeyPoint();
			keypoint1.pt.x = I1.get(0, i);
			keypoint1.pt.y = I1.get(1, i);
			
			keypoint2.pt.x = I2.get(0, i);
			keypoint2.pt.y = I2.get(1, i);
			
			Correspondence corr = new Correspondence(keypoint1, keypoint2, Algorithm.EIGHT_POINT, K);
			correspondences.add(corr);
			correspondenceMatrix[i] = corr.correspondence;
		}
		
		SimpleMatrix A = new SimpleMatrix(correspondenceMatrix);
		//TODO assert A as equal to the exampleTwo points in matrix or something
		
		MotionEstimation solver = new MotionEstimation();
		MotionEstimator estimate = solver.ransac(100, 8, correspondences, K);
		
		Assert.assertTrue(estimate.E.isIdentical(E_true, 0.001));
		Assert.assertTrue(estimate.rotation.isIdentical(R_true, 0.001));
		Assert.assertTrue(estimate.translation.isIdentical(t_true, 0.001));	
	}
	/*
	@Test
	public void exampleTwo() {
		//NOTE these matrix values are the same as the first example but multiplied by K^{-1}
		SimpleMatrix I1 = new SimpleMatrix(3,10);
		I1.set(0, 0, -0.209);
		I1.set(0, 1, -0.180);
		I1.set(0, 2, -0.455);
		I1.set(0, 3, -0.554);
		I1.set(0, 4, -0.132);
		I1.set(0, 5, -0.118);
		I1.set(0, 6, -0.420);
		I1.set(0, 7, -0.535);
		I1.set(0, 8, -0.272);
		I1.set(0, 9, -0.372);
		
		I1.set(1, 0, -0.053);
		I1.set(1, 1, -0.009);
		I1.set(1, 2, 0.019);
		I1.set(1, 3, -0.033);
		I1.set(1, 4, 0.354);
		I1.set(1, 5, 0.314);
		I1.set(1, 6, 0.323);
		I1.set(1, 7, 0.394);
		I1.set(1, 8, 0.552);
		I1.set(1, 9, -0.175);
		
		I1.set(2, 0, 1);
		I1.set(2, 1, 1);
		I1.set(2, 2, 1);
		I1.set(2, 3, 1);
		I1.set(2, 4, 1);
		I1.set(2, 5, 1);
		I1.set(2, 6, 1);
		I1.set(2, 7, 1);
		I1.set(2, 8, 1);
		I1.set(2, 9, 1);
		
		
		SimpleMatrix I2 = new SimpleMatrix(3,10);
		I2.set(0, 0, 0.565);
		I2.set(0, 1, 0.378);
		I2.set(0, 2, 0.119);
		I2.set(0, 3, 0.205);
		I2.set(0, 4, 0.668);
		I2.set(0, 5, 0.454);
		I2.set(0, 6, 0.168);
		I2.set(0, 7, 0.282);
		I2.set(0, 8, 0.422);
		I2.set(0, 9, 0.265);
		
		I2.set(1, 0, -0.053);
		I2.set(1, 1, -0.009);
		I2.set(1, 2, 0.019);
		I2.set(1, 3, -0.033);
		I2.set(1, 4, 0.354);
		I2.set(1, 5, 0.314);
		I2.set(1, 6, 0.323);
		I2.set(1, 7, 0.394);
		I2.set(1, 8, 0.552);
		I2.set(1, 9, -0.175);
		
		I2.set(2, 0, 1);
		I2.set(2, 1, 1);
		I2.set(2, 2, 1);
		I2.set(2, 3, 1);
		I2.set(2, 4, 1);
		I2.set(2, 5, 1);
		I2.set(2, 6, 1);
		I2.set(2, 7, 1);
		I2.set(2, 8, 1);
		I2.set(2, 9, 1);
		
		SimpleMatrix K = new SimpleMatrix(3,3);
		K.set(0, 0, 1);
		K.set(0, 1, 0);
		K.set(0, 2, 1);
		K.set(1, 0, 0);
		K.set(1, 1, 1);
		K.set(1, 2, 1);
		K.set(2, 0, 0);
		K.set(2, 1, 0);
		K.set(2, 2, 1);
		

		SimpleMatrix E_true = new SimpleMatrix(3,3);
		E_true.set(0, 0, 0);
		E_true.set(0, 1, 0);
		E_true.set(0, 2, 0);
		E_true.set(1, 0, 0);
		E_true.set(1, 1, 0);
		E_true.set(1, 2, -0.7071);
		E_true.set(2, 0, 0);
		E_true.set(2, 1, 0.7071);
		E_true.set(2, 2, 0);
		
		double[][] correspondenceMatrix = new double[I1.numCols()][9];
		List<Correspondence> correspondences = new LinkedList<Correspondence>();
		for(int i = 0; i < I1.numCols(); i++){
			KeyPoint keypoint1 = new KeyPoint();
			KeyPoint keypoint2 = new KeyPoint();
			keypoint1.pt.x = I1.get(0, i);
			keypoint1.pt.y = I1.get(1, i);
			
			keypoint2.pt.x = I2.get(0, i);
			keypoint2.pt.y = I2.get(1, i);
			
			Correspondence corr = new Correspondence(keypoint1, keypoint2);
			correspondences.add(corr);
			correspondenceMatrix[i] = corr.correspondence;
		}
		
		SimpleMatrix A = new SimpleMatrix(correspondenceMatrix);
		
		
		
		SimpleMatrix t_true = new SimpleMatrix(3,1);
		assertEquals(0,0);
	}
	*/
}
