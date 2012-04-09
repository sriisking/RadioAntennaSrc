package com.tcs.antenna;

import java.io.*;
import java.util.StringTokenizer;

public class LatlongtoXY {

	private static int totalantenna = 120;
	private static int    EARTH_RADIUS    = 6371000;
	private static double PI = 3.14159;
	private static double[] latitude = new double[totalantenna];
	private static double[] longitude = new double[totalantenna];
	private static double[] x = new double[totalantenna];
	private static double[] y = new double[totalantenna];
	private static double[] z = new double[totalantenna];
	public static String commonfilepath = "C:\\antenna\\radio\\src\\com\\tcs\\antenna\\";

	public static void main(String[] a) throws IOException
	{
		String lat_long_filePath = commonfilepath + "Coordinates.txt";
		FileReader lat_long_reader = new FileReader(lat_long_filePath);  
		BufferedReader lat_long_buffered = new BufferedReader(lat_long_reader);
		String tempstring = "";
		int count = 0;

		while((tempstring = lat_long_buffered.readLine()) != null)
		{

			StringTokenizer st1 = new StringTokenizer(tempstring," ", true);
			longitude[count] = Double.parseDouble(st1.nextToken());
			st1.nextToken();
			latitude[count] = Double.parseDouble(st1.nextToken());
			count++;
		}

		lat_long_buffered.close();
		lat_long_reader.close();

		String inputAntennaXY = commonfilepath + "inputAntennaXY.txt";
		FileWriter fw = new FileWriter(inputAntennaXY);
		BufferedWriter bw = new BufferedWriter(fw);

		bw.write("Number_of_antenna " + count + "\n" + "#x(km)	y(km)	z(km)" + "\n");

		for(int i = 0; i < totalantenna;i++)
		{
			longitude[i] = ((longitude[i]*PI)/180);
			latitude[i] = ((latitude[i]*PI)/180);
			x[i] = EARTH_RADIUS * Math.cos(latitude[i]) * Math.cos(longitude[i]);
			y[i] = EARTH_RADIUS * Math.cos(latitude[i]) * Math.sin(longitude[i]);
			z[i] = EARTH_RADIUS * Math.sin(latitude[i]);

			bw.write(x[i] + "\t" + y[i] + "\t" + z[i] + "\n");
		}

		bw.close();
		fw.close();

	}
}
