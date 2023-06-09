package ui;


import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
//consulted with: Leona Salihović,Borna Petak,Karlo Boroš

public class Solution {

	public static void main(String ... args)
	{
		int outcomeDimension=1;

		String trainFile=null;
		String testFile=null;
		String neuralNetworkDimension=null;
		int elitism = 0,populationSize = 0,iter = 0;
		double k = 0,p = 0;
		int parameterCounter=0;
		for(int i=0;i< args.length;i++)
		{
			if(args[i].equals("--train"))
			{
				parameterCounter++;
				i++;
				trainFile=args[i];
			}else if(args[i].equals("--test"))
			{
				parameterCounter++;
				i++;
				testFile=args[i];
			}else if(args[i].equals("--elitism"))
			{
				parameterCounter++;
				i++;
				elitism= Integer.parseInt(args[i]);
			}else if(args[i].equals("--nn"))
			{
				parameterCounter++;
				i++;
				neuralNetworkDimension=args[i];
			}else //noinspection SpellCheckingInspection
				if(args[i].equals("--popsize"))
			{
				parameterCounter++;
				i++;
				populationSize=Integer.parseInt(args[i]);
			}else if(args[i].equals("--p"))
			{
				parameterCounter++;
				i++;
				p= Double.parseDouble(args[i]);
			}else if(args[i].equals("--K"))
			{
				parameterCounter++;
				i++;
				k= Double.parseDouble(args[i]);
			}else if(args[i].equals("--iter"))
			{
				parameterCounter++;
				i++;
				iter= Integer.parseInt(args[i]);
			}

		}
		if(parameterCounter!=8)
		{
			System.err.println("Not enough parameters");
			System.exit(0);
		}
		assert neuralNetworkDimension != null;
		ArrayList<Integer>HiddenLayerDimensions=parsLayers(neuralNetworkDimension);
		ArrayList<DataRow> trainData =extractData(trainFile,outcomeDimension);

		ArrayList<DataRow> testData =extractData(testFile,outcomeDimension);



		NetworkTrainer networkTrainer=new NetworkTrainer(populationSize,elitism,p,k,iter);


		Network b=networkTrainer.trainNetwork(trainData,trainData.get(0).getFeatures().size(),outcomeDimension,HiddenLayerDimensions);
		b.evaluate(testData);
		System.out.println("[Test error]: "+String.format("%.6f",-b.getFitness()));




		


	}

	private static ArrayList<DataRow> extractData(String file,int outcomeDimension)
	{
		ArrayList<DataRow> data=new ArrayList<>();
		Charset charset = StandardCharsets.UTF_8;
		Path filePath = Paths.get(file);
		try {
			List<String> lines = Files.readAllLines(filePath, charset);
			lines.removeIf(str -> str.startsWith("#") || str.length() == 0);
			for (int i = 1; i < lines.size(); i++) {
				data.add(new DataRow(outcomeDimension,lines.get(i)));
			}
		} catch (IOException ex) {
			System.out.format("I/O error: %s%n", ex);
		}
		return data;
	}

	private static ArrayList<Integer> parsLayers(String neuralNetworkDimension)
	{
		ArrayList<Integer> numeric=new ArrayList<>();
		for(String num :neuralNetworkDimension.split("s"))
		{
			numeric.add(Integer.parseInt(num));
		}
		return numeric;
	}

}


