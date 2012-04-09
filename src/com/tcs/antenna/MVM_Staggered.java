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

public class MVM_Staggered 
{

	private static String commonfilepath = "C:\\antenna\\radio\\src\\com\\tcs\\antenna\\Staggered_80_removed\\";
	private static int remove_pts = 80;
	private static int number_of_antenna;
	private static int number_of_uvpoints;
	private static int number_of_ellipses;
	private static int total_sectors =8;

	private static int remove(UV[] uvpts, Antenna[] antennas)
	{
		int total_regions = number_of_ellipses * total_sectors ;
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
						if((uvpts[k].ant1==i)||(uvpts[k].ant2==i))
						{
							wt[(uvpts[k].elliptical_region_no*total_sectors) + uvpts[k].sector] = wt[(uvpts[k].elliptical_region_no*total_sectors) + uvpts[k].sector] + 1; 
						}
					}
				}

				for(int t =0;t<number_of_ellipses;t++)
				{
					int ellipsewt = 0;
					for(int m = 0; m<total_sectors;m++)
					{
						ellipsewt = ellipsewt + wt[(t*total_sectors) + m];
					}

					int value = ellipsewt - 2; //ideally each elliptical region should have 2 less antenna
					value = value * value; //squaring just to negate the negative number of antennas i.e. variance
					sum = sum + value;
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

	private static int getnumberofellipses(int rempts)
	{
		int numberofellipses = 0;
		if ((number_of_antenna-rempts)%2!=0)
		{
			numberofellipses = number_of_antenna - rempts;	
		}
		else 
		{
			numberofellipses = number_of_antenna - rempts - 1;
		}
		return numberofellipses;
	}

	//dividing the the UV plane into 8 sectors (45 degrees)
	private static int getsector(double u, double v)
	{
		int sector = 0;

		if((u==0)&&(v==0)) //origin belongs to sector 1
		{
			sector = 0; 
		}

		//Sector dividing lines belong to the previous sector
		if ((u>=0)&&(v>0)) //Quadrant 1
		{
			if(u>=v)
			{
				sector = 0;
			}
			else
			{
				sector = 1;
			}
		}
		else if ((u<0)&&(v>=0)) //Quadrant 2
		{
			if((-u)<=v)
			{
				sector = 2;
			}
			else
			{
				sector = 3;
			}
		}
		else if ((u<=0)&&(v<0)) //Quadrant 3
		{
			if(u<=v)
			{
				sector = 4;
			}
			else
			{
				sector = 5;
			}
		}
		else if ((u>0)&&(v<=0)) //Quadrant 4
		{
			if(u<=(-v))
			{
				sector = 6;
			}
			else 
			{
				sector = 7;
			}
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
		number_of_uvpoints = (number_of_antenna)*(number_of_antenna -1);
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

//		Deciding on the number of ellipses required for the initial number of uv points 
		int antennasremoved = 0;	
		number_of_ellipses = getnumberofellipses(antennasremoved);

//		Assigning regions
		int current_region = 0;
		
		int pts_per_ellipse = number_of_uvpoints/number_of_ellipses;
		for (int i =0; i< number_of_uvpoints; i++)
		{
			if(((i%pts_per_ellipse)==0)&&(i!=0))
			{
				current_region = current_region + 1;
			}
			uvpts[i].elliptical_region_no = current_region; 
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


//		Remove antennas and redraw elliptical regions (Part 5)	

		int remaining_uvpts = 0;

		if(remove_pts<=number_of_antenna)
		{
			for (int i = 1; i<= remove_pts;i++)
			{
				int antenna_number = remove(uvpts, antennas);
				antennas[antenna_number].flagxy = true;
				for (int l = 0; l < number_of_uvpoints; l++)
				{
					if((uvpts[l].ant1==antenna_number)||(uvpts[l].ant2==antenna_number))
					{
						uvpts[l].flaguv = true;
					}
				}
				antennasremoved++;

				//redraw the ellipses and give new elliptical region
				number_of_ellipses = getnumberofellipses(antennasremoved);
				remaining_uvpts = (number_of_antenna-antennasremoved)*(number_of_antenna-antennasremoved-1);
				pts_per_ellipse = remaining_uvpts/number_of_ellipses;
				current_region = 0;
				int qualifieduvcount = 0;
				for (int g = 0; g < number_of_uvpoints; g++)
				{
					if(uvpts[g].flaguv == false)
					{
						if(((qualifieduvcount%pts_per_ellipse)==0)&&(qualifieduvcount!=0))
						{
							current_region = current_region + 1;
						}
						qualifieduvcount = qualifieduvcount + 1;
						uvpts[g].elliptical_region_no = current_region;
					}
				}
			}
		}
		else
		{
			System.out.println("Cant remove more or equal amount of antennae than the existing ones");
		}

//		Print results (Part 6)
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