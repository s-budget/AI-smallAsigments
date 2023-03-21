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
		for(int i=0;i< args.length;i++)
		{
			if(args[i].equals("--alg"))
			{
				i++;
				if (args[i].equals("bfs"))
					mode = 0;
				else if(args[i].equals("ucs"))
					mode = 1;
				else
					mode = 2;
			}
			else if(args[i].equals("--ss"))
			{
				i++;
				pathTree = args[i];
			}
			else if(args[i].equals("--h"))
			{
				i++;
				pathHeuristic = args[i];
			}
			else if(args[i].equals("--check-optimistic"))
			{
				optimistic = true;
			}
			else if(args[i].equals("--check-consistent"))
			{
				konsistent = true;
			}
		}

		Charset charset = StandardCharsets.UTF_8;
		HashMap<String,Float> heauristika=null;
		if (pathHeuristic!=null)
		{
			heauristika=new HashMap<>();
			Path filePath = Paths.get(pathHeuristic);
			try
			{
				List<String> lines = Files.readAllLines(filePath, charset);
				lines.removeIf(str -> str.startsWith("#"));
				for(String pair : lines)
					heauristika.put(pair.split(": ")[0], Float.parseFloat(pair.split(": ")[1]));
			}
			catch (IOException ex)
			{
				System.out.format("I/O error: %s%n", ex);
			}
		}

		PriorityQueue<ConsistentState> consistentStates=new PriorityQueue<>(new ConsistentStateComparator());
		String first= "";
		ArrayList<String> finals=new ArrayList<>();
		HashMap<String,SortedSet <Pair<String,Float>>>prijelazi=new HashMap<>();
		HashMap<String,SortedSet<Pair<String,Float>>>reversePrijelazi=new HashMap<>();
		assert pathTree != null;
		Path filePath = Paths.get(pathTree);
		try
		{
			List<String> lines = Files.readAllLines(filePath, charset);
			lines.removeIf(str -> str.startsWith("#"));
			first = lines.get(0);
			finals =new ArrayList<>(Arrays.asList( lines.get(1).split(" ")));
			for(int i=2;i< lines.size();i++)
			{
				String start= lines.get(i).split(": ")[0];
				SortedSet<Pair<String,Float>> destinations=new TreeSet<>(new PairComparator());
				if(lines.get(i).split(": ").length>1)
				{
					String[] pairs = lines.get(i).split(": ")[1].split(" ");
					for (String pair : pairs)
					{
						String temppName=pair.split(",")[0];
						Float temppValue=Float.parseFloat(pair.split(",")[1]);
						if(konsistent)
							consistentStates.add(new ConsistentState(start,temppName,temppValue, Objects.requireNonNull(heauristika).get(start),heauristika.get(temppName)));

						reversePrijelazi.putIfAbsent(temppName, new TreeSet<Pair<String, Float>>(new PairComparator()));
						SortedSet<Pair<String,Float>>tempDestinations=reversePrijelazi.get(temppName);
						tempDestinations.add(new Pair<>(start,temppValue));
						reversePrijelazi.put(temppName,tempDestinations);

						destinations.add(new Pair<>(temppName,temppValue));
					}
						prijelazi.put(start, destinations);
				}
			}
		}
		catch (IOException ex)
		{
			System.out.format("I/O error: %s%n", ex);
		}



		if(mode!=-1)
		{
			if (mode==0)
				System.out.println("# BFS");
			else if(mode==1)
				System.out.println("# UCS");
			else
				System.out.println("# A-STAR "+filePath.getFileName());
			Kvintet i=SearchAlgorithm(first,finals,heauristika,prijelazi,mode);
			System.out.print(i);
		}
		else if(optimistic)
		{
			System.out.println("# HEURISTIC-OPTIMISTIC "+filePath.getFileName());
			PriorityQueue<OptimisticState> states=CheckOptimistic(finals,heauristika,reversePrijelazi);
			boolean totalState=true;
			while(!states.isEmpty())
			{
				OptimisticState tempState=states.remove();
				System.out.println(tempState);
				totalState=(totalState && tempState.isStatus());
			}
			System.out.println(formatStatus(totalState,"optimistic"));
				

		}
		else if (konsistent)
		{
			System.out.println("# HEURISTIC-CONSISTENT "+filePath.getFileName());
			boolean totalState=true;
			while(!consistentStates.isEmpty())
			{
				ConsistentState tempState=consistentStates.remove();
				System.out.println(tempState);
				totalState=(totalState && tempState.isState());
			}
			System.out.println(formatStatus(totalState,"consistent"));

		}

	}

	private static PriorityQueue<OptimisticState> CheckOptimistic(ArrayList<String> starters, HashMap<String, Float> heauristika, HashMap<String, SortedSet<Pair<String, Float>>> prijelazi)
	{

		HashSet<String> visited=new HashSet<>();
		PriorityQueue<OptimisticState> calculatedStates=new PriorityQueue<>(new OptimisticStateComparator());


		Queue<Node> red = new PriorityQueue<>(new NodeComparatorForUCS());

		for(String first : starters)
		{
			Node pocetni=new Node(first,null,0,0,0);
			red.add(pocetni);
		}
		while(!red.isEmpty())
		{
			Node prvi=red.remove();
			if(visited.contains(prvi.getName()))
				continue;
			calculatedStates.add(new OptimisticState(prvi.getName(),heauristika.get(prvi.getName()),prvi.getTotalCost()));
			visited.add(prvi.getName());
			if(!prijelazi.containsKey(prvi.getName()))
				continue;
			for(Pair<String,Float> par : prijelazi.get(prvi.getName()))
			{
				if(!visited.contains(par.first))
				{
					red.add(new Node(par.first,prvi,prvi.getTotalCost()+par.second,0F,prvi.getDepth()+1));
				}
			}
		}
		return calculatedStates;
	}


	public static Kvintet SearchAlgorithm(String first, ArrayList<String> finals,HashMap<String,Float> Heauristika, HashMap<String,SortedSet<Pair<String,Float>>>prijelazi,int mode)
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
			if( finals.contains(prvi.getName()))
			{
				solution=new Node(prvi.getName(),prvi.getParent(),prvi.getTotalCost(),prvi.getTotalCost(),prvi.getDepth());
				break;
			}
			if(!prijelazi.containsKey(prvi.getName()))
				continue;
			for(Pair<String,Float> par : prijelazi.get(prvi.getName()))
			{		//BRZI BFS
				     /*if(finals.contains(par.first) && mode==0)
					 {
						    solution=new Node(par.first,prvi,prvi.getTotalCost()+par.second,0F,prvi.getDepth()+1);
					 		//visited.add(par.first);
					 		red.clear();
							break;
					 }*/
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
			 return new Kvintet(true, visited.size(), solution.getDepth()+1, solution.getTotalCost(), path);
		 }
	}
	public static String formatStatus(boolean status,String mode)
	{
		if(status)
			return "[CONCLUSION]: Heuristic is "+mode+".";
		return "[CONCLUSION]: Heuristic is not "+mode+".";
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
		 if(x==0)
			 return o1.getName().compareTo(o2.getName());
		 return (int) (Math.signum(x) == 1 ? Math.ceil(x) : Math.floor(x));
	 }

}
 class NodeComparatorForAStar implements Comparator<Node>
{
	@Override
	public int compare(Node o1, Node o2)
	{
		float x = o1.getAproximate()-o2.getAproximate();
		if(x==0)
			return o1.getName().compareTo(o2.getName());
		return (int) (Math.signum(x) == 1 ? Math.ceil(x) : Math.floor(x));
	}

}
class PairComparator implements Comparator<Pair<String,Float>>
{
	@Override
	public int compare(Pair<String,Float> o1, Pair<String,Float> o2)
	{
			return o1.getFirst().compareTo(o2.getFirst());
	}
}
class OptimisticStateComparator implements Comparator<OptimisticState>
{
	@Override
	public int compare(OptimisticState o1, OptimisticState o2)
	{
		return (o1.getName().compareTo(o2.getName()));
	}

}
class ConsistentStateComparator implements Comparator<ConsistentState>
{
	@Override
	public int compare(ConsistentState o1, ConsistentState o2)
	{
		return (o1.getParentName().compareTo(o2.getParentName()));
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

class OptimisticState
{
	String name;
	boolean status;
	Float heuristic;
	Float actualCost;


	@Override
	public String toString() {
		return "[CONDITION]: ["+formatStatus(this.status)+"] h("+this.name+") <= h*: "+this.heuristic+" <= "+this.actualCost;
	}

	private String formatStatus(boolean status)
	{
		if(status)
			return "OK";
		return "ERR";
	}


	public OptimisticState(String name, Float heuristic, Float actualCost) {
		this.name = name;
		this.status=(heuristic<=actualCost);
		this.heuristic = heuristic;
		this.actualCost = actualCost;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public Float getHeuristic() {
		return heuristic;
	}

	public void setHeuristic(Float heuristic) {
		this.heuristic = heuristic;
	}

	public Float getActualCost() {
		return actualCost;
	}

	public void setActualCost(Float actualCost) {
		this.actualCost = actualCost;
	}
}

class ConsistentState
{
	String parentName;
	String childName;

	boolean state;

	Float cost;
	Float estimateParent;
	Float estimateChild;

	public ConsistentState(String parentName, String childName, Float cost, Float estimateParent, Float estimateChild) {
		this.parentName = parentName;
		this.childName = childName;
		this.cost = cost;
		this.estimateParent = estimateParent;
		this.estimateChild = estimateChild;
		this.state=(estimateParent-estimateChild<=cost);
	}
	@Override
	public String toString() {
		return "[CONDITION]: ["+formatStatus(this.state)+"] h("+parentName+") <= h("+childName+") + c: "+estimateParent+" <= "+estimateChild+" + "+cost;
	}
	private String formatStatus(boolean status)
	{
		if(status)
			return "OK";
		return "ERR";
	}

	public String getParentName() {
		return parentName;
	}

	public boolean isState() {
		return state;
	}
}
