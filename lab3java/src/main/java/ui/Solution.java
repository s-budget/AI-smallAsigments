package ui;

import java.io.IOException;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import  java.lang.Math.*;
//Consulted with: Borna Petak
//                : Karlo Boroš
//                : Leona Salihović
public class Solution {

	public static void main(String ... args)
	{
		int depth = -1;
		String pathTrain;
		String pathTest ;
		pathTrain = args[0];
		pathTest = args[1];
		if(args.length==3)
		{
			depth=Integer.parseInt(args[2]);
		}
		//System.out.println(pathTest+" "+pathTrain+" "+depth);
		Charset charset = StandardCharsets.UTF_8;
		Path filePath = Paths.get(pathTrain);
		ArrayList<params> parametri=new ArrayList<>();
		try {
			List<String> lines = Files.readAllLines(filePath, charset);
			lines.removeIf(str -> str.startsWith("#") || str.length() == 0);
			String[] imena= lines.get(0).split(",");
			for (int i = 1; i < lines.size(); i++) {
				parametri.add(new params(imena,lines.get(i).split(",")));
			}
		} catch (IOException ex) {
			System.out.format("I/O error: %s%n", ex);
		}

		MLAlgorithm ML =new MLAlgorithm(depth);
		ML.fit(parametri);
		filePath = Paths.get(pathTest);
		ArrayList<params> test=new ArrayList<>();
		try {
			List<String> lines = Files.readAllLines(filePath, charset);
			lines.removeIf(str -> str.startsWith("#") || str.length() == 0);
			String[] imena= lines.get(0).split(",");
			for (int i = 1; i < lines.size(); i++) {
				test.add(new params(imena,lines.get(i).split(",")));
			}
		} catch (IOException ex) {
			System.out.format("I/O error: %s%n", ex);
		}
		ML.predict(test);


	}

}
class MLAlgorithm
{
	int depth;
	Node treeStart;
	SortedSet<String> labels=new TreeSet<>();
	HashMap<String,HashSet<String>>kardinalitet=new HashMap<>();
	public MLAlgorithm(int depth)
	{
		this.depth=depth;
	}

	public void fit(ArrayList<params> parametri)
	{
		for(params p : parametri)
		{
			labels.add(p.getLabel());
			for(String s :parametri.get(0).getParram().keySet())
			{
				if(!kardinalitet.containsKey(s))
				{
					kardinalitet.put(s,new HashSet<>());
				}
				HashSet<String>temp=kardinalitet.get(s);
				temp.add(p.getParram().get(s));
				kardinalitet.put(s,temp);
			}

		}
		System.out.println("[BRANCHES]:");
		this.treeStart=buildNode(parametri,null,parametri.get(0).getParram().keySet(),0,"","suck");
	}

	private Node buildNode(ArrayList<params> parametri,ArrayList<params> parametriRoditelj , Set<String> unusedNames, int currentDepth,String currentPath,String parrentChoice)
	{
		if(parametri.isEmpty())
		{
			Node povratni=new Node();
			povratni.setLeaf(true);
			povratni.setLabel(parrentChoice);
			povratni.setBranch(currentPath+parrentChoice);
			System.out.println(povratni.getBranch());
			return povratni;
		}
		if(depth==currentDepth || unusedNames.isEmpty())
		{ String labelC = null;
			int max=0;
			HashMap<String,Integer>temp=new HashMap<>();
			for(params p : parametri)
			{
				if(temp.containsKey(p.getLabel()))
				{
					temp.put(p.getLabel(),temp.get(p.getLabel())+1);
				}
				else
				{
					temp.put(p.getLabel(),1);
				}
				if(temp.get(p.getLabel())>max)
				{
					max=temp.get(p.getLabel());
				}
			}
			SortedSet<String> keys= new TreeSet<>();
			keys.addAll(temp.keySet());
			for(String s:keys)
			{
				if(temp.get(s)>=max)
				{
					labelC=s;
					break;
				}
			}
			Node povratni=new Node();
			povratni.setLeaf(true);
			povratni.setLabel(labelC);
			povratni.setBranch(currentPath+labelC);

			System.out.println(povratni.getBranch());
			return povratni;
		}
		if(parametri.stream().allMatch(p-> Objects.equals(p.getLabel(), parametri.get(0).getLabel())))
		{
			Node povratni=new Node();
			povratni.setLeaf(true);
			povratni.setLabel(parametri.get(0).getLabel());
			povratni.setBranch(currentPath+parametri.get(0).getLabel());
			System.out.println(povratni.getBranch());

			return povratni;
		}

		String currentName="z";
		double maks=0;
		for(String ime : unusedNames)
		{
			HashMap<String,Integer>temp=new HashMap<>();
			HashMap<String,HashMap<String,Integer>>temp2=new HashMap<>();
			for(params p : parametri)
			{
				if(temp.containsKey(p.getLabel()))
				{
					temp.put(p.getLabel(),temp.get(p.getLabel())+1);
				}
				else
				{
					temp.put(p.getLabel(),1);
				}
				if(!temp2.containsKey(p.getParram().get(ime)))
				{
					temp2.put(p.getParram().get(ime),new HashMap<>());
				}

				if(temp2.get(p.getParram().get(ime)).containsKey(p.getLabel()))
				{
					temp2.get(p.getParram().get(ime)).put(p.getLabel(),temp2.get(p.getParram().get(ime)).get(p.getLabel())+1);
				}
				else
				{
					temp2.get(p.getParram().get(ime)).put(p.getLabel(),1);
				}

			}

			double entropija= 0;
			double sum = 0.;
			for (int f : temp.values()) {
				sum += f;
			}
			for (int f : temp.values()) {
				entropija -= (f/sum)*(Math.log(f/sum)/Math.log(2));
			}

			double ig=entropija;
			for (String s : temp2.keySet())
			{
				double entropijaUnutarnja=0;
				double sum1=0;
				for (int f : temp2.get(s).values())
				{
					sum1 += f;
				}
				for(int f : temp2.get(s).values())
				{
					entropijaUnutarnja -= (f/sum1)*(Math.log(f/sum1)/Math.log(2));
				}
				ig-=(sum1/sum)*entropijaUnutarnja;
			}
			if(ig>maks || ig==maks && ime.compareTo(currentName)<0)
			{
				maks=ig;
				currentName=ime;
			}


		}

		Node povratni=new Node();
		povratni.setLeaf(false);
		povratni.setBranch(currentPath);
		povratni.setEvidenceName(currentName);

		for(String uvjet :kardinalitet.get(currentName))
		{
			ArrayList<params> parDjete=new ArrayList<>();
			parDjete.addAll(parametri);
			String finalCurrentName = currentName;
			parDjete.removeIf(p -> !p.getParram().get(finalCurrentName).equals(uvjet));
			Set<String> newNames=new HashSet<>();
			newNames.addAll(unusedNames);
			newNames.removeIf(n->n.equals(finalCurrentName));

			String labelC = null;
			int max=0;
			HashMap<String,Integer>temp1=new HashMap<>();
			for(params p : parametri)
			{
				if(temp1.containsKey(p.getLabel()))
				{
					temp1.put(p.getLabel(),temp1.get(p.getLabel())+1);
				}
				else
				{
					temp1.put(p.getLabel(),1);
				}
				if(temp1.get(p.getLabel())>max)
				{
					max=temp1.get(p.getLabel());
				}
			}
			SortedSet<String> keys= new TreeSet<>();
			keys.addAll(temp1.keySet());
			for(String s:keys)
			{
				if(temp1.get(s)>=max)
				{
					labelC=s;
					break;
				}
			}
			povratni.setCommon(labelC);
			Node djete=buildNode(parDjete,parametri,newNames,currentDepth+1,currentPath+(currentDepth+1)+":"+currentName+"="+uvjet+" ",labelC);
			povratni.getChildren().put(uvjet,djete);
		}
		return povratni;
	}

	public void predict(ArrayList<params> test) {
		System.out.print("[PREDICTIONS]:");
		HashMap<String,HashMap<String,Integer>>matrica=new HashMap<>();
		double correct=0;
		double total=0;
		for(params p : test)
		{
			Node trenutni=new Node();
			trenutni.setLeaf(treeStart.isLeaf());
			trenutni.setBranch(treeStart.getBranch());
			trenutni.setChildren(treeStart.getChildren());
			trenutni.setEvidenceName(treeStart.getEvidenceName());
			trenutni.setLabel(treeStart.getLabel());
			trenutni.setCommon(treeStart.getCommon());
			while(!trenutni.isLeaf())
			{
				if(trenutni.getChildren().get(p.getParram().get(trenutni.getEvidenceName()))==null)
				{
					String trenutniCommon= trenutni.getCommon();
					trenutni=new Node();
					trenutni.setLeaf(true);
					trenutni.setLabel(trenutniCommon);
					break;
				}
				trenutni=trenutni.getChildren().get(p.getParram().get(trenutni.getEvidenceName()));
			}
			System.out.print(" "+trenutni.getLabel());
			total++;

			if(trenutni.getLabel().equals(p.getLabel()))
			{
				correct++;
			}
			if(!matrica.containsKey(p.getLabel()))
			{
				matrica.put(p.getLabel(),new HashMap<>());
			}
			if(!matrica.get(p.getLabel()).containsKey(trenutni.getLabel()))
			{
				matrica.get(p.getLabel()).put(trenutni.getLabel(),0);
			}
			matrica.get(p.getLabel()).put(trenutni.getLabel(),matrica.get(p.getLabel()).get(trenutni.getLabel())+1);
		}
		System.out.println();
		double acc=correct/total;
		System.out.println("[ACCURACY]: "+String.format("%.5f", acc));
		System.out.println("[CONFUSION_MATRIX]:");
		for(String s:labels)
		{
			for(String z:labels)
			{
				if(matrica.get(s).get(z)==null)
				{
					System.out.print(0+" ");
				}
				else
				{
					System.out.print(matrica.get(s).get(z)+" ");
				}
			}
			System.out.println();
		}
	}
}
class Node
{
	boolean leaf;
	String label;
	String branch;
	String evidenceName;
	HashMap<String,Node> children =new HashMap<>();
	String common;

	public String getCommon() {
		return common;
	}

	public void setCommon(String common) {
		this.common = common;
	}

	public boolean isLeaf() {
		return leaf;
	}

	public void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}


	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public String getEvidenceName() {
		return evidenceName;
	}

	public void setEvidenceName(String evidenceName) {
		this.evidenceName = evidenceName;
	}

	public HashMap<String, Node> getChildren() {
		return children;
	}

	public void setChildren(HashMap<String, Node> children) {
		this.children = children;
	}
}

class params
{
	HashMap<String,String> parram=new HashMap<>();
	String label;

	public params(String[] imena, String[] values) {
		for(int i=0;i< imena.length-1;i++)
		{
			parram.put(imena[i],values[i]);
		}
		label=values[imena.length-1];
	}

	public HashMap<String, String> getParram() {
		return parram;
	}

	public void setParram(HashMap<String, String> parram) {
		this.parram = parram;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
