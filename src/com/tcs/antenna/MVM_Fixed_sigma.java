package com.tcs.antenna;

import com.tcs.antenna.UV;
import com.tcs.antenna.Antenna;
import java.lang.Math;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;

public class MVM_Fixed_sigma {

	private static String commonfilepath = "C:\\antenna\\radio\\src\\com\\tcs\\antenna\\Fixed_flat\\";
	private static int remove_pts = 60;
	private static int number_of_antenna;
	private static int number_of_uvpoints;
	private static int number_of_ellipses;
	private static int total_sectors = 4;
	
	private static int remove(UV[] uvpts, Antenna[] antennas, double[] tot)
	{
		int total_regions = number_of_ellipses*total_sectors;
		int remant = 0;
		double sum = 0;
		double nsum = 2147483647;
		int[] wt = new int[total_regions];

		for (int i = 0; i< number_of_antenna; i++)
		{
			sum = 0;
			if(antennas[i].flagxy==false)
			{
				for (int j = 0; j<number_of_ellipses;j++)
				{
					for (int n = 0; n<total_sectors;n++)
					{
						wt[(j*total_sectors) + n] = 0;	
					}
				}

				for(int k =0; k< number_of_uvpoints; k++)
				{
					if(uvpts[k].flaguv==false)
					{
						if((uvpts[k].ant1==i)||(uvpts[k].ant1==i))
						{
							wt[(uvpts[k].elliptical_region_no*total_sectors) + uvpts[k].sector] = wt[(uvpts[k].elliptical_region_no*total_sectors) + uvpts[k].sector] + 1; 
						}
					}
				}

				for(int t =0;t<number_of_ellipses;t++)
				{

					for(int m = 0; m<total_sectors;m++)
					{
						double value = wt[(t*total_sectors) + m] - (0.25*tot[t]); //ideally each elliptical region should have tot less uv pts
						value = value * value; //squaring just to negate the negative number of antennas i.e. variance
						sum = sum + value;
					}
				}

				if (sum<nsum)
				{
					nsum = sum;
					remant = i;
				}
			}
		}
		return remant;
	}

	//dividing the the UV plane into 4 sectors (90 degrees)
	private static int getsector(double u, double v)
	{
		int sector = 0;

		if((u==0)&&(v==0)) //origin belongs to sector 0
		{
			sector = 0; 
		}
		//Sector dividing lines belong to the previous sector
		if ((u>=0)&&(v>0)) //Quadrant 1
		{
			sector = 0;
		}
		else if ((u<0)&&(v>=0)) //Quadrant 2
		{
			sector = 1;
		}
		else if ((u<=0)&&(v<0)) //Quadrant 3
		{
			sector = 2;
		}
		else if ((u>0)&&(v<=0)) //Quadrant 4
		{
			sector = 3;
		}
		return sector;
	}

//	Main code
	public static void main(String[] a) throws IOException 
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

		String int_ant_detail_filepath = commonfilepath + "inant_MVM.txt";
		FileWriter int_ant_writer = new FileWriter(int_ant_detail_filepath);  
		BufferedWriter int_ant_buffered_writer = new BufferedWriter(int_ant_writer);

		int_ant_buffered_writer.write("#" +"\t" + "x" + "\t" + "y" + "\t" + "z" + "\n");

		tempstring = int_ant_buffered_reader.readLine();
		StringTokenizer st2 = new StringTokenizer(tempstring," ", true);
		st2.nextToken(); //to skip number_of_antenna string
		st2.nextToken();
		tempstring = st2.nextToken();
		number_of_antenna = Integer.parseInt(tempstring);

		int_ant_buffered_reader.readLine();

		//initializing variables for antenna co-ordinates
		String x_coordinate = "";
		String y_coordinate = "";
		String z_coordinate = "";

		Antenna[] antennas = new Antenna[number_of_antenna];

		for(int i = 0; i < number_of_antenna; i++)
		{
			antennas[i] = new Antenna();
			tempstring = int_ant_buffered_reader.readLine();
			st2 = new StringTokenizer(tempstring,"\t",true);
			antennas[i].no = i;
			antennas[i].flagxy = false;
			int_ant_buffered_writer.write(antennas[i].no + "\t");

			x_coordinate = st2.nextToken();
			antennas[i].x = Double.parseDouble(x_coordinate);
			int_ant_buffered_writer.write(antennas[i].x + "\t");
			st2.nextToken();

			y_coordinate = st2.nextToken();
			antennas[i].y = Double.parseDouble(y_coordinate);
			int_ant_buffered_writer.write(antennas[i].y + "\t");
			st2.nextToken();

			z_coordinate = st2.nextToken();
			antennas[i].z = Double.parseDouble(z_coordinate);
			int_ant_buffered_writer.write(antennas[i].z + "\n");
		}

		int_ant_buffered_reader.close();
		int_ant_reader.close();
		int_ant_buffered_writer.close();
		int_ant_writer.close();


//		Calculating UV points (Part 3)
		number_of_uvpoints = 0;
		number_of_uvpoints = number_of_antenna*(number_of_antenna -1);
		double start_hr = min_h;
		int n_h = (int) ((max_h - min_h + 1)/interval);  //currently implemented for snapshot n_h = 1

		double sind = Math.sin( Math.PI * decl/180);	
		double cosd = Math.cos( Math.PI * decl/180);
		double hr, lx, ly, lz, sinh, cosh;

		UV[] uvpts = new UV[number_of_uvpoints];
		int current_uv_count = 0;

		for(int snapshot_positions=0; snapshot_positions < n_h; snapshot_positions ++)
		{
			hr = start_hr + (interval*snapshot_positions);
			for(int i = 0; i < number_of_antenna; i++)
			{
				for (int j = 0; j <number_of_antenna; j++)
				{
					if(i!=j)
					{
						uvpts[current_uv_count] = new UV();

						lx = antennas[i].x - antennas[j].x; 
						ly = antennas[i].y - antennas[j].y;
						lz = antennas[i].z - antennas[j].z;
						sinh = Math.sin(Math.PI * hr/180);
						cosh = Math.cos(Math.PI * hr/180);

						uvpts[current_uv_count].u = ((sinh * lx) + (cosh * ly))/wavelenght;
						uvpts[current_uv_count].v = ((-sind * cosh * lx) + (sind * sinh * ly) + (cosd * lz))/wavelenght;
						uvpts[current_uv_count].w = ((cosd * cosh * lx) - (cosd * sinh * ly) + (sind * lz))/wavelenght;
						uvpts[current_uv_count].ant1 = i;
						uvpts[current_uv_count].ant2 = j;
						uvpts[current_uv_count].flaguv = false;

						//Used this distance score for ellipses
						uvpts[current_uv_count].dist = Math.pow(uvpts[current_uv_count].u, 2) + ((Math.pow(uvpts[current_uv_count].v, 2))/ (Math.pow(sind, 2)));
						uvpts[current_uv_count].elliptical_region_no = 0;
						uvpts[current_uv_count].sector = getsector(uvpts[current_uv_count].u, uvpts[current_uv_count].v);

						current_uv_count++;
	
					}
				}
			}
		}

//		Sorting uv points, assigning elliptical regions (Part 4)
//		Sorting UV points based on distance
		UV tempuv = new UV();
		for(int i=0; i<(number_of_uvpoints-1);i++)
		{
			for(int j=i;j<number_of_uvpoints;j++)
			{
				if(uvpts[i].dist>uvpts[j].dist)
				{
					tempuv = uvpts[i];
					uvpts[i] = uvpts[j];
					uvpts[j] = tempuv;
				}
			}
		}

		int antennasremoved = 0;	
		number_of_ellipses = 15;
		int current_region = 0;
		
//		Assigning regions - this will change completely
		
		//find thew highest distance
		double distance_from_centre = 0; 
		
		for(int i=0;i<number_of_uvpoints;i++)
		{
			if(distance_from_centre<= uvpts[i].dist)
			{
				distance_from_centre = uvpts[i].dist;
			}
		}
		
		distance_from_centre = distance_from_centre + 1; //adjustment to bring all points inside the largest ellipse
	//	double sigma = distance_from_centre/3; //then give the number to be 3 sigma for the gaussian 
		double ellipse_width = distance_from_centre/number_of_ellipses; 
		int[] region_density = new int[number_of_ellipses];
		
		//assign regions based on this fact		
		for(int i=0;i<number_of_uvpoints;i++)
		{
			if((ellipse_width + (current_region*ellipse_width))< uvpts[i].dist)
			{
				current_region++;	
			}
			uvpts[i].elliptical_region_no = current_region;
			region_density[current_region]++; 
		}
		
		for(int i=0; i<number_of_ellipses;i++)
		{
			System.out.println("Number of uv pts left " + i + " = " + region_density[i]);
		}
		
//		Write to file the initial set of UV points
		String int_uv_filepath = commonfilepath + "inuv_MVM.txt";
		FileWriter int_uv_writer = new FileWriter(int_uv_filepath);  
		BufferedWriter int_uv_buffered = new BufferedWriter(int_uv_writer);
		int_uv_buffered.write("#" + "\t" + "u" + "\t" + "v" +"\t" + "w" + "\t" + "ant1"  + "\t" + "ant2" + "\t" + "distance" + "\t" + "region" + "\t" + "sector" + "\t" + "flag" + "\n");

		for(int i=0;i<number_of_uvpoints;i++)
		{
 			uvpts[i].no = i; 
			int_uv_buffered.write(uvpts[i].no + "\t" + uvpts[i].u + "\t" + uvpts[i].v + "\t" + uvpts[i].w + "\t" + uvpts[i].ant1 + "\t" + uvpts[i].ant2 + "\t" + uvpts[i].dist + "\t" + uvpts[i].elliptical_region_no + "\t" + uvpts[i].sector + "\t" + uvpts[i].flaguv + "\n");			
		}

		int_uv_buffered.close();
		int_uv_writer.close();
		
		
//		Remove antennas (Part 6)	
		
		if(remove_pts<=number_of_antenna)
		{
			for (int i = 1; i<= remove_pts;i++)
			{
				//UV points per region
				double[] expected_region_density = new double[number_of_ellipses];
				double sigma = 3.75; //15 regions within 4 sigma. Hence 15/4 = 3.75
				double interval_size = 1;
				double numuv = (number_of_antenna- antennasremoved -1)*(number_of_antenna - antennasremoved-2);

				for(int k=0;k<number_of_ellipses;k++)
				{
					double multiplier = (numuv*2)*(1/(sigma*Math.sqrt(2*Math.PI)));
					double e_power = ((-1*Math.pow(((k+0.5)*interval_size), 2))/(2*Math.pow(sigma, 2)));
					expected_region_density[k] = multiplier * (Math.pow(Math.E, e_power));
				}	
				
				
				//double tot = (number_of_antenna - antennasremoved)/(number_of_ellipses * total_sectors);
				//UV points affected by removal of one antennae per region is calculated for every ellipse
				
				int antenna_number = remove(uvpts, antennas, expected_region_density);
				antennas[antenna_number].flagxy = true;
				for (int l = 0; l < number_of_uvpoints; l++)
				{
					if((uvpts[l].ant1==antenna_number)||(uvpts[l].ant2==antenna_number))
					{
						uvpts[l].flaguv = true;
					}
				}
				antennasremoved++;
			}
		}
		else
		{
			System.out.println("Cant remove more or equal amount of antennae than the existing ones");
		}

		int[] actual_region_density = new int[number_of_ellipses];
		current_region = 0;
		//assign regions based on this fact		
		for(int i=0;i<number_of_uvpoints;i++)
		{
			if(uvpts[i].flaguv==false)
			{
				actual_region_density[uvpts[i].elliptical_region_no]++;			
			}
		}

		for(int i=0;i<number_of_ellipses;i++)
		{
			System.out.println("Actual number of uv pts left " + i + " = " + actual_region_density[i]);
		}
		
//		Print results (Part 7)
		String out_ant_detail_filepath = commonfilepath + "outant_MVM.txt";
		FileWriter out_ant_writer = new FileWriter(out_ant_detail_filepath);  
		BufferedWriter out_ant_buffered_writer = new BufferedWriter(out_ant_writer);
		out_ant_buffered_writer.write("#" +"\t" + "x" + "\t" + "y" + "\t" + "z" + "\n");
		for(int i=0;i<number_of_antenna;i++)
		{
			if(antennas[i].flagxy==false)
			{
				out_ant_buffered_writer.write(antennas[i].no + "\t" + antennas[i].x + "\t" + antennas[i].y + "\t" + antennas[i].z + "\t" + antennas[i].flagxy + "\n");
			}
		}
		out_ant_buffered_writer.close();
		out_ant_writer.close();
		
		String out_uv_filepath = commonfilepath + "outuv_MVM.txt";
		FileWriter out_uv_writer = new FileWriter(out_uv_filepath);  
		BufferedWriter out_uv_buffered = new BufferedWriter(out_uv_writer);
		out_uv_buffered.write("#" + "\t" + "u" + "\t" + "v" +"\t" + "w" + "\t" + "ant1"  + "\t" + "ant2" + "\t" + "distance" + "\t" + "region" + "\t" + "sector" + "\t" + "flag" + "\n");

		for(int i=0;i<number_of_uvpoints;i++)
		{
			if(uvpts[i].flaguv==false)
			{
				out_uv_buffered.write(uvpts[i].no + "\t" + uvpts[i].u + "\t" + uvpts[i].v + "\t" + uvpts[i].w + "\t" + uvpts[i].ant1 + "\t" + uvpts[i].ant2 + "\t" + uvpts[i].dist + "\t" + uvpts[i].elliptical_region_no + "\t" + uvpts[i].sector + "\t" + uvpts[i].flaguv + "\n");				
			}
		}

		out_uv_buffered.close();
		out_uv_writer.close();
	}
}	