package ui;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Solution {

	public static void main(String ... args) {
		int mode=-1;
		String pathTree=null;
		String pathHeuristic=null;
		boolean optimistic=false;
		boolean konsistent=false;
		for(int i=0;i< args.length;i++) {
			if(args[i].equals("--alg")) {
				i++;
				if (args[i].equals("bfs"))
					mode=0;
				else if(args[i].equals("ucs"))
					mode=1;
				else
					mode=2;
			}
			else if(args[i].equals("--ss"))
			{
				i++;
				pathTree=args[i];
			}
			else if(args[i].equals("--h"))
			{
				i++;
				pathHeuristic=args[i];
			}
			else if(args[i].equals("--check-optimistic"))
			{
				optimistic=true;
			}
			else if(args[i].equals("--check-consistent"))
			{
				konsistent=true;
			}
		}

		String first=new String();
		ArrayList<String> finals=new ArrayList<>();
		HashMap<String,ArrayList<Pair<String,Float>>>prijelazi=new HashMap<>();
		Path filePath = Paths.get(pathTree);
		Charset charset = StandardCharsets.UTF_8;
		try {
			List<String> lines = Files.readAllLines(filePath, charset);
			first = lines.get(0);
			finals =new ArrayList<>(Arrays.asList( lines.get(1).split(" ")));
			for(int i=2;i< lines.size();i++)
			{
				String start= lines.get(i).split(": ")[0];
				ArrayList<Pair<String,Float>> destinations=new ArrayList<>();
				String[] pairs=lines.get(i).split(": ")[1].split(" ");
				for(String pair :pairs)
				{
					destinations.add(new Pair<String,Float>(pair.split(",")[0],Float.parseFloat(pair.split(",")[0])));
				}
				prijelazi.put(start,destinations);
			}
		} catch (IOException ex) {
			System.out.format("I/O error: %s%n", ex);
		}

		HashMap<String,Float> Heauristika=null;
		if (pathHeuristic!=null)
		{
			Heauristika=new HashMap<>();
			 filePath = Paths.get(pathTree);
			try {
				List<String> lines = Files.readAllLines(filePath, charset);
				for(String pair:lines)
				{
					Heauristika.put(pair.split(": ")[0],Float.parseFloat(pair.split(": ")[0]));
				}
			}catch (IOException ex) {
				System.out.format("I/O error: %s%n", ex);
			}

		}

		if(mode!=-1)
		{
			if (mode==0)
				System.out.print("# BFS");
			else if(mode==1)
				System.out.print("# UCS");
			else
				System.out.print("# A-STAR "+filePath.getFileName());

			Kvintet i=SearchAlgorithm(first,finals,Heauristika,prijelazi,mode);
			System.out.print(i);
		}
		else if(optimistic)
		{

		}
		else if (konsistent)
		{

		}

		/*finals.add("G");
		prijelazi.put("A", new ArrayList<> (Arrays.asList(new Pair<>("B", 1F), new Pair<>("C", 8F))));
		prijelazi.put("B", new ArrayList<> ( List.of(new Pair<>("D", 1F))));
		prijelazi.put("C",new ArrayList<> (  List.of(new Pair<>("E", 8F))));
		prijelazi.put("D",new ArrayList<> ( Arrays.asList(new Pair<>("E", 1F), new Pair<>("G", 8F))));
		prijelazi.put("E",new ArrayList<> ( Arrays.asList(new Pair<>("H", 1F), new Pair<>("D", 1F))));
		prijelazi.put("H",new ArrayList<> ( List.of(new Pair<>("G", 1F))));
		Kvintet i=SearchAlgorithm("A",finals,null,prijelazi,0);//BFS
		Kvintet u=SearchAlgorithm("A",finals,null,prijelazi,1);//UCS*/

	}

	public static Kvintet SearchAlgorithm(String first, ArrayList<String> finals,HashMap<String,Float> Heauristika, HashMap<String,ArrayList<Pair<String,Float>>>prijelazi,int mode)
	{
		float temp=0;
		if(mode==2)
		{
			temp=Heauristika.get(first);
		}
		Node pocetni=new Node(first,null,0,temp,0);
		LinkedList<String> path=new LinkedList<>();
		HashSet<String> visited=new HashSet<>();
		 if(finals.contains(pocetni.getName()))
		 {
			 path.add(pocetni.getName());
			 return new Kvintet(true,1,0,0,path);
		 }
		 Queue<Node> red;
		 if(mode==0)
			 red=new LinkedList<>();
		 else if(mode ==1)
			 red = new PriorityQueue<>(new NodeComparatorForUCS());
		else
			red = new PriorityQueue<>(new NodeComparatorForAStar());

		 red.add(pocetni);
		 Node solution=null;
		 while(!red.isEmpty())
		 {
			 Node prvi=red.remove();
			 visited.add(prvi.getName());
			 if((mode==2 || mode==1) && finals.contains(prvi.getName()))
			 {
				 solution=new Node(prvi.getName(),prvi.getParent(),prvi.getTotalCost(),prvi.getTotalCost(),prvi.getDepth());
				 break;
			 }
			for(Pair<String,Float> par : prijelazi.get(prvi.getName()))
			{
				     if(finals.contains(par.first) && mode==0)
					 {
						    solution=new Node(par.first,prvi,prvi.getTotalCost()+par.second,0F,prvi.getDepth()+1);
					 		//visited.add(par.first);
					 		red.clear();
							break;
					 }
					 else
					 {
						     if(!visited.contains(par.first))
							 {
								 temp=0F;
								 if(mode==2)
								 {
									 temp=prvi.getTotalCost()+par.second+Heauristika.get(par.first);
								 }
								 red.add(new Node(par.first,prvi,prvi.getTotalCost()+par.second,temp,prvi.getDepth()+1));
							 }
					 }
			}
		 }
		 if(solution==null)
		 {
			 return new Kvintet(false,visited.size(),-1,-1,null);
		 }
		else {
			 Node tempNode = solution;
			 while (tempNode.getParent() != null)
			 {
				 path.add(tempNode.getName());
				 tempNode=tempNode.getParent();
			 }
			 path.add(tempNode.getName());
			 return new Kvintet(true, visited.size(), solution.getDepth(), solution.getTotalCost(), path);
		 }
	}
}


class Node
{
	String name;
	Node Parent;
	float TotalCost;

	float Aproximate;
	int depth;

	public float getAproximate() {
		return Aproximate;
	}

	public void setAproximate(float aproximate) {
		Aproximate = aproximate;
	}

	public Node(String name, Node parent, float TotalCOst,float Aproximate, int Depth) {
		this.name = name;
		Parent = parent;
		this.TotalCost = TotalCOst;
		this.depth = Depth;
		this.Aproximate=Aproximate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Node getParent() {
		return Parent;
	}

	public void setParent(Node parent) {
		Parent = parent;
	}

	public float getTotalCost() {
		return TotalCost;
	}

	public void setTotalCost(float totalCost) {
		TotalCost = totalCost;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}
}
	 class NodeComparatorForUCS implements Comparator<Node>
	 {
		 @Override
		 public int compare(Node o1, Node o2)
		 {
			 float x = o1.getTotalCost()-o2.getTotalCost();
			 return (int) (Math.signum(x) == 1 ? Math.ceil(x) : Math.floor(x));
		 }

	 }
	class NodeComparatorForAStar implements Comparator<Node>
	{
		@Override
		public int compare(Node o1, Node o2)
		{
			float x = o1.getAproximate()-o2.getAproximate();
			return (int) (Math.signum(x) == 1 ? Math.ceil(x) : Math.floor(x));
		}

}

class Pair<K,T>
{
	K first;
	T second;

	public Pair(K first, T second) {
		this.first = first;
		this.second = second;
	}

	public K getFirst() {
		return first;
	}

	public void setFirst(K first) {
		this.first = first;
	}

	public T getSecond() {
		return second;
	}

	public void setSecond(T second) {
		this.second = second;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Pair)) return false;
		Pair<?, ?> pair = (Pair<?, ?>) o;
		return Objects.equals(getFirst(), pair.getFirst()) && Objects.equals(getSecond(), pair.getSecond());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getFirst(), getSecond());
	}
}

class Kvintet
{
	boolean foundSolution;
	int statesVisited;
	int pathLength;
	float totalCost;
	LinkedList<String> Path;

	@Override
	public String toString() {
		return "[FOUND_SOLUTION]: " +BoolToYN(foundSolution)+
				"\n[STATES_VISITED]: " + statesVisited +
				"\n[PATH_LENGTH]: " + pathLength +
				"\n[TOTAL_COST]: " + totalCost +
				"\n[PATH]: " + FormatPath(Path) +
				'\n';
	}
	public String BoolToYN(boolean y)
	{
		if(y)
			return "yes";
		return "no";
	}
	public String FormatPath(LinkedList<String> Path)
	{
		Collections.reverse(Path);
		StringBuilder out= new StringBuilder(Path.get(0));
		for(int i=1;i<Path.size();i++)
		{
			out.append(" => ").append(Path.get(i));
		}
		return out.toString();
	}

	public Kvintet(boolean foundSolution, int statesVisited, int pathLength, float totalCost, LinkedList<String> path) {
		this.foundSolution = foundSolution;
		this.statesVisited = statesVisited;
		this.pathLength = pathLength;
		this.totalCost = totalCost;
		Path = path;
	}

	public boolean isFoundSolution() {
		return foundSolution;
	}

	public void setFoundSolution(boolean foundSolution) {
		this.foundSolution = foundSolution;
	}

	public int getStatesVisited() {
		return statesVisited;
	}

	public void setStatesVisited(int statesVisited) {
		this.statesVisited = statesVisited;
	}

	public int getPathLength() {
		return pathLength;
	}

	public void setPathLength(int pathLength) {
		this.pathLength = pathLength;
	}

	public float getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(float totalCost) {
		this.totalCost = totalCost;
	}

	public LinkedList<String> getPath() {
		return Path;
	}

	public void setPath(LinkedList<String> path) {
		Path = path;
	}
}


