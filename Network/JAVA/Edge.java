package Network.JAVA;

public class Edge {
	public int year;
	public String production, category, type;
	public String filmKey; //Preserved for Gephi labelling
	public int source, target; //Use Node ID instead of name

	public Edge(int source, int target, int year, String production, String category, String type, String filmKey) {
		this.year = year;
		this.production = production;
		this.category = category;
		this.type = type;
		this.filmKey = filmKey;

		this.source = source;
		this.target = target;
	}

	@Override
	public String toString() {
		return source + "," + target + ",\"" + filmKey + "\"," + year + ",\"" + production + "\",\"" + category + "\",\"" + type + "\"";
	}
	
}

class Temp_WeightedEdge {
	public String relation; //"A->B"
	public int weight;

	public Temp_WeightedEdge(String relation, int weight) {
		this.relation = relation;
		this.weight = weight;
	}
}

class WeightedEdge {
	public int source, target; //Use Node ID instead of name
	public int weight; //How many times the nodes of this edge appears together in the network

	public WeightedEdge(int source, int target, int weight) {
		this.source = source;
		this.target = target;
		this.weight = weight;
	}

	@Override
	public String toString() {
		return source + "," + target + "," + weight;
	}
}