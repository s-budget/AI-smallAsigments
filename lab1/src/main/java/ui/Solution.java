package ui;

import java.util.*;

public class Solution {

	public static void main(String ... args) {
		System.out.println("Ovime kreće Vaš program.");
		for(String arg : args) {
			System.out.printf("Predan argument %s%n", arg);
		}
	}

	public static Kvintet runBFS(String first, ArrayList<String> finals, HashMap<String,ArrayList<Pair<String,Integer>>>prijelazi)
	{
		Node pocetni=new Node(first,null,0,0);
		LinkedList<String> path=new LinkedList<>();
		HashSet<String> visited=new HashSet<>();
		 if(finals.contains(pocetni.getName()))
		 {
			 path.add(pocetni.getName());
			 return new Kvintet(true,1,0,0,path);
		 }
		 Deque<Node> red=new ArrayDeque<>();
		 red.addLast(pocetni);
		 Node solution=null;
		 while(!red.isEmpty())
		 {
			 Node prvi=red.removeFirst();
			 visited.add(prvi.getName());
			for(Pair<String,Integer> par : prijelazi.get(prvi.getName()))
			{
				     if(finals.contains(par.first))
					 {
						    solution=new Node(par.first,prvi,prvi.getTotalCost()+par.second,prvi.getDepth()+1);
					 		visited.add(par.first);
					 		red.clear();
							break;
					 }
					 else
					 {
						     if(!visited.contains(par.first))
							 {
								 red.addLast(new Node(par.first,prvi,prvi.getTotalCost()+par.second,prvi.getDepth()+1));
							 }
					 }
			}
		 }
		 if(solution==null)
		 {
			 return new Kvintet(false,visited.size(),-1,-1,null);
		 }
		else {
			 Node temp = solution;
			 while (temp.getParent() != null)
			 {
				 path.add(temp.getName());
				 temp=temp.getParent();
			 }
			 return new Kvintet(true, visited.size(), solution.getDepth(), solution.getTotalCost(), path);
		 }

	}

}



class Node
{
	String name;
	Node Parent;
	float TotalCost;
	int depth;

	public Node(String name, Node parent, float TotalCOst, int Depth) {
		this.name = name;
		Parent = parent;
		this.TotalCost = TotalCOst;
		this.depth = Depth;
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


