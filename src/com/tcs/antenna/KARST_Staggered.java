package com.tcs.antenna;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

public class KARST_Staggered {

	private static String commonfilepath = "C:\\antenna\\radio\\src\\com\\tcs\\antenna\\Karst_80_removed\\";
	private static double MAXINT = 2147483647;
	private static int numSites; //total sites - this can be found
	private static int numAntennae = 40; //final number of antennae left - this is configurable

	public static void main(String[] args) throws IOException 
	{
//		Reading the observation details (Part 1)		
		String observation_detail_filePath = commonfilepath + "obs_in.txt";
		FileReader obs_file_reader = new FileReader(observation_detail_filePath);  
		BufferedReader obs_file_buffered = new BufferedReader(obs_file_reader);
		String tempstring = "";

		obs_file_buffered.readLine();
		tempstring = obs_file_buffered.readLine();
		StringTokenizer st1 = new StringTokenizer(tempstring," ", true);

		//initialize observation parameters
		String token = st1.nextToken();
		double min_h = Double.parseDouble(token);
		st1.nextToken();

		token = st1.nextToken();
		double max_h = Double.parseDouble(token);
		st1.nextToken();

		token = st1.nextToken();
		double interval = Double.parseDouble(token);
		st1.nextToken();

		token = st1.nextToken();
		double decl = Double.parseDouble(token);
		st1.nextToken();

		token = st1.nextToken();
		double wavelenght = Double.parseDouble(token);
		obs_file_buffered.close();
		obs_file_reader.close();

//		Reading the co-ordinates of the antennas from input file (Part 2)		
		String int_ant_filepath = commonfilepath + "inputAntennaXY.txt";
		FileReader int_ant_reader = new FileReader(int_ant_filepath);  
		BufferedReader int_ant_buffered_reader = new BufferedReader(int_ant_reader);

		String int_ant_detail_filepath = commonfilepath + "inant_KARST.txt";
		FileWriter int_ant_writer = new FileWriter(int_ant_detail_filepath);  
		BufferedWriter int_ant_buffered_writer = new BufferedWriter(int_ant_writer);

		int_ant_buffered_writer.write("#" +"\t" + "x" + "\t" + "y" + "\t" + "z" + "\n");

		tempstring = int_ant_buffered_reader.readLine();
		StringTokenizer st2 = new StringTokenizer(tempstring," ", true);
		st2.nextToken(); //to skip number_of_antenna string
		st2.nextToken();
		tempstring = st2.nextToken();
		numSites = Integer.parseInt(tempstring);

		int_ant_buffered_reader.readLine();

		//initializing variables for antenna co-ordinates
		String x_coordinate = "";
		String y_coordinate = "";
		String z_coordinate = "";

		//Antenna[] antennas = new Antenna[numSites]; //replace with individual x, y and z arrays

		double[] x = new double[numSites]; 
		double[] y = new double[numSites]; 
		double[] z = new double[numSites];
		int[] correctionfactor = new int[numSites];
		double[] distSumSquare = new double[numSites];

		for (int i = 0; i < numSites; i++) 
		{
			correctionfactor[i] = 0;
			distSumSquare[i] = 0;
		}	

		for(int i = 0; i < numSites; i++)
		{
			tempstring = int_ant_buffered_reader.readLine();
			st2 = new StringTokenizer(tempstring,"\t",true);
			int_ant_buffered_writer.write(i + "\t");

			x_coordinate = st2.nextToken();	
			x[i] = Double.parseDouble(x_coordinate);
			int_ant_buffered_writer.write(x[i] + "\t");
			st2.nextToken();

			y_coordinate = st2.nextToken();
			y[i] = Double.parseDouble(y_coordinate);
			int_ant_buffered_writer.write(y[i] + "\t");
			st2.nextToken();

			z_coordinate = st2.nextToken();
			z[i] = Double.parseDouble(z_coordinate);
			int_ant_buffered_writer.write(z[i] + "\n");
		}

		int_ant_buffered_reader.close();
		int_ant_reader.close();
		int_ant_buffered_writer.close();
		int_ant_writer.close();

		// 2d (u,v) points
		double[][] u = new double[numSites][numSites];
		double[][] v = new double[numSites][numSites];
		double[][] w = new double[numSites][numSites];

		double start_hr = min_h;
		int n_h = (int) ((max_h - min_h + 1)/interval);  //currently implemented for snapshot n_h = 1


		double sind = Math.sin( Math.PI * decl/180);	
		double cosd = Math.cos( Math.PI * decl/180);
		double hr, lx, ly, lz, sinh, cosh;
		hr = 0;
		System.out.println(hr);

//		Write to file the initial set of UV points
		String int_uv_filepath = commonfilepath + "inuv_KARST.txt";
		FileWriter int_uv_writer = new FileWriter(int_uv_filepath);  
		BufferedWriter int_uv_buffered = new BufferedWriter(int_uv_writer);
		int_uv_buffered.write("#" + "\t" + "u" + "\t" + "v" +"\t" + "w" + "\t" + "ant1"  + "\t" + "ant2" + "\n");

		int count =0;

		//have a post correction to get absolute indexes
		for (int currentNumSites = numSites;currentNumSites > numAntennae; currentNumSites--) 
		{
			System.out.println("Current Number of Sites = " + currentNumSites);
			// Generate (u,v) points from (x,y) points

			int currentantennae = 0;
			for(int i = 0; i < currentNumSites; i++)
			{
				for (int j = 0; j <currentNumSites; j++)
				{
					if(i!=j)
					{
						lx = x[i] - x[j]; 
						ly = y[i] - y[j];
						lz = z[i] - z[j];

						sinh = Math.sin(Math.PI * hr/180);
						cosh = Math.cos(Math.PI * hr/180);

						u[i][j] = ((sinh * lx) + (cosh * ly))/wavelenght;
						v[i][j] = ((-sind * cosh * lx) + (sind * sinh * ly) + (cosd * lz))/wavelenght;
						w[i][j] = ((cosd * cosh * lx) - (cosd * sinh * ly) + (sind * lz))/wavelenght;

						if(currentNumSites == numSites)
						{
							int_uv_buffered.write(currentantennae + "\t" + u[i][j] + "\t" + v[i][j] + "\t" + w[i][j] + "\t" + i + "\t" + j + "\n");
							currentantennae++;	
						}

						//scaling down to avoid double overflow error 
						u[i][j] = u[i][j]/10000; 
						v[i][j] = v[i][j]/10000;
						w[i][j] = w[i][j]/10000;
						count++;
					}
				}
			}

			System.out.println(count);
			int_uv_buffered.close();
			int_uv_writer.close();			

			System.out.println("Nearest Neigbors of UV points");
			// COMPUTE NEAREST NEIGHBOR FOR EACH UV POINT			
			for (int i = 0; i < currentNumSites; i++)
			{
				for (int j = 0; j < currentNumSites; j++) 
				{
					if(i!=j)
					{
						int NNi = -1;
						int NNj = -1;
						double NNdistance = MAXINT;

						for (int k = 0; k < currentNumSites; k++)
						{
							for (int l = 0; l < k; l++) 
							{
								// This if statement is a bit controversial
								if (i != k && j != l && i != l && j != k) 
								{
									double newDist = (u[i][j] - u[k][l]) * (u[i][j] - u[k][l]) + (v[i][j] - v[k][l]) * (v[i][j] - v[k][l]);
									if (newDist < NNdistance) 
									{
										// NN of (i,j) is (k,l)
										NNi = k; NNj = l;
										NNdistance = newDist;
									}
								}
							}
						}
						// add NNdist for (i,j) to both i and j
						// while checking for overflows
						if (MAXINT - distSumSquare[i] >= NNdistance)
						{
							distSumSquare[i] += NNdistance;
						}
						else 
						{
							System.out.println("Double Overflow");
						}
						if (MAXINT - distSumSquare[j] >= NNdistance)
						{
							distSumSquare[j] += NNdistance;
						}
						else 
						{
							System.out.println("Double Overflow"); //This is uneccessary. Just didn't want to change the code provided by Prof. Dinesh a lot
						}
					}

				}
			}

			System.out.println("Sum of Nearest Neighbor Distances for each XY point");
			double minDistance = MAXINT;
			double totalMinDistance = 0;
			int minIndex = -1;
			for (int i = 0; i < currentNumSites; i++) 
			{
				System.out.println(i + ": " + distSumSquare[i]);
				totalMinDistance += distSumSquare[i];
				if (distSumSquare[i] < minDistance) 
				{
					minDistance = distSumSquare[i];
					minIndex = i;
				}
			}
			System.out.println("Average NN distance is " + totalMinDistance/(currentNumSites));
			System.out.println("Index of XY point to be eliminated is " + minIndex + "\n");



			// Delete site with smallest distSumSquare value
			for (int i = minIndex; i < currentNumSites - 1; i++) 
			{
				correctionfactor[i]++;
				correctionfactor[i] = correctionfactor[i+1];
				x[i] = x[i + 1];
				y[i] = y[i + 1];
			}
		}

		String rem_ant_detail_filepath = commonfilepath + "outant_KARST.txt";
		FileWriter rem_ant_writer = new FileWriter(rem_ant_detail_filepath);  
		BufferedWriter rem_ant_buffered_writer = new BufferedWriter(rem_ant_writer);
		rem_ant_buffered_writer.write("#" +"\t" + "x" + "\t" + "y" + "\t" + "z" + "\n");

		for(int i=0;i<numAntennae;i++)
		{
			rem_ant_buffered_writer.write(i +"\t" + x[i] + "\t" + y[i] + "\t" + z[i] + "\n");
		}

		rem_ant_buffered_writer.close();
		rem_ant_writer.close();


		String out_uv_filepath = commonfilepath + "outuv_KARST.txt";
		FileWriter out_uv_writer = new FileWriter(out_uv_filepath);  
		BufferedWriter out_uv_buffered = new BufferedWriter(out_uv_writer);
		out_uv_buffered.write("#" + "\t" + "u" + "\t" + "v" +"\t" + "w" + "\t" + "ant1"  + "\t" + "ant2" + "\n");

		int currentuv = 0;
		for(int i=0;i<numAntennae;i++)
		{
			for(int j = 0; j<numAntennae;j++)
			{
				if(i!=j)
				{
					out_uv_buffered.write(currentuv + "\t" + (u[i][j]*1000) + "\t" + (v[i][j]*1000) + "\t" + (w[i][j]*1000) + "\t" + i + "\t" + j + "\n");
					currentuv++;	
				}
			}
		}
		out_uv_buffered.close();
		out_uv_writer.close();

	}
}