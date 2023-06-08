package Network.JAVA;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import OCR.JAVA.Film;

public class GetEdges {
	protected static final String EDGES_ROOT = "Network/csv/edges";
	private static final boolean USE_WEIGHT = true, RUN_ALL_ONCE = false, RUN_YEAR_RANGE = true;
	private static boolean DIR_ACT_ONLY = false; //Only works for run all once with weight

	private static ArrayList<Film> films;

	public static void main(String[] args) {
		try {
			films = Film.initAllFilms();
			if(!RUN_ALL_ONCE) {
				if(!RUN_YEAR_RANGE) {
					for(int year = 1949; year <= 1966; year++) {
						getAllEdgesInYear(year);
						System.out.println(year + " done.");
					}
				} else {
					getAllEdgesInYear(1964, 1966);
				}
			} else {
				getAllEdges();
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static void getAllEdges() throws IOException {
		if(USE_WEIGHT)
			getAllWeightedEdges();
		else
			getAllUnweightedEdges();
	}

	private static void getAllEdgesInYear(int year) throws IOException {
		getAllEdgesInYear(year, year);
	}

	private static void getAllEdgesInYear(int startYear, int endYear) throws IOException {
		if(USE_WEIGHT)
			getAllWeightedEdgesInYear(startYear, endYear);
		else
			getAllUnweightedEdgesInYear(startYear, endYear);
	}

	private static void getAllWeightedEdgesInYear(int startYear, int endYear) throws IOException {
		if(startYear > endYear) {
			System.err.println("Start year must be smaller than end year!");
			System.exit(1);
		}
		String tag = startYear == endYear ? Integer.toString(startYear) : startYear + "-" + endYear;
		File nodeFile = new File("Network/csv/nodes/nodes-" + tag + ".csv");
		if(!nodeFile.exists()) {
			System.err.println("Node file does not exist for year " + tag + "!");
			System.exit(1);
		}

		ArrayList<Film> filmsInYear = new ArrayList<Film>();
		HashMap<String, Integer> estimatedEdges = new HashMap<String, Integer>();
		//Use a special format to represent an edge: "A->B"

		for (Film film : films) {
			if(film.year >= startYear && film.year <= endYear) {
				filmsInYear.add(film);
			}
		}

		int totalWeight = 0;
		int maxWeight = 1;
		String maxWeightRep = "";
		int n = 0;

		for (Film film : filmsInYear) {
			String[] allNames = film.getAllNamesArrayWithoutDuplication();
			ArrayList<String> estimatedEdgesInEntry = new ArrayList<String>();

			for (String name : allNames) {
				for (String name2 : allNames) {
					if(Film.isOrganisation(name) || Film.isOrganisation(name2)) continue; //Skip organisations
					if(name.equals(name2)) continue; //Skip the same node

					String rep = name + "->" + name2;
					String rev = name2 + "->" + name;
					//Use this to count the paired appearance of two nodes only once for one film
					if(!estimatedEdgesInEntry.contains(rep) && !estimatedEdgesInEntry.contains(rev)) {
						estimatedEdgesInEntry.add(rep);
						
						//The network is undirected, so we only need to add one edge for a pair of nodes
						if(!estimatedEdges.containsKey(rep) && !estimatedEdges.containsKey(rev)) {
							estimatedEdges.put(rep, 1);
							totalWeight++;
						} else {
							totalWeight++;
							int weight;
							//If the edge already exists, we need to increase the weight
							if(estimatedEdges.containsKey(rep)) {
								weight = estimatedEdges.get(rep) + 1;
								estimatedEdges.put(rep, weight);
							} else if(estimatedEdges.containsKey(rev)) {
								weight = estimatedEdges.get(rev) + 1;
								estimatedEdges.put(rev, weight);
							} else {
								throw new IOException("Edge: " + rep + " or " + rev + " not found!");
							}
							if(weight > maxWeight) {
								maxWeight = weight;
								maxWeightRep = rep;
							} 
						}
					}
				}
			}
		}

		ArrayList<WeightedEdge> edges = new ArrayList<WeightedEdge>();
		for (String edge : estimatedEdges.keySet()) {
			String[] names = edge.split("->");
			edges.add(new WeightedEdge(nameToId(names[0], tag), nameToId(names[1], tag), estimatedEdges.get(edge)));
			n++;
			System.out.println(n + "/" + estimatedEdges.size());
		}

		if(startYear == endYear) {
			int year = startYear;
			System.out.println("Year " + year + " done. " + edges.size() + " edges found.");
		} else {
			System.out.println("Years " + startYear + " to " + endYear + " done. " + edges.size() + " edges found.");
		}
		
		System.out.println("Total weight: " + totalWeight);
		System.out.println("Max weight: " + maxWeight + " (" + maxWeightRep + ")");
	
		if(startYear == endYear) {
			int year = startYear;
			writeAllWeightedEdges(edges, Integer.toString(year));
		} else {
			writeAllWeightedEdges(edges, startYear + "-" + endYear);
		}
	}

	private static void getAllUnweightedEdges() throws IOException {
		ArrayList<Edge> edges = new ArrayList<Edge>();
		int n = 0;

		for (Film film : films) {
			String type = film.getFilmType();
			String category = formatCategories(film.getCategory());
			String production = film.productionToString();
			String key = film.key;
			String[] allNames = film.getAllNamesArrayWithoutDuplication();
			ArrayList<String> estimatedEdges = new ArrayList<String>();
			int year = film.year;
			
			for (String name : allNames) {
				for (String name2 : allNames) {
					if(Film.isOrganisation(name) || Film.isOrganisation(name2)) continue; //Skip organisations
					if(name.equals(name2)) continue; //Skip the same node

					String rep = name + "->" + name2;
					String rev = name2 + "->" + name;
					if(!estimatedEdges.contains(rep) && !estimatedEdges.contains(rev)) {
						//The network is undirected, so we only need to add one edge for a pair of nodes
						estimatedEdges.add(rep);
					}
				}
			}

			for (String edge : estimatedEdges) {
				String[] names = edge.split("->");
				edges.add(new Edge(nameToAllYearId(names[0]), nameToAllYearId(names[1]), year, production, category, type, key));
			}
			n++;
			System.out.println("Film (" + n + "/" + films.size() + ") " + film.title + " done. " + estimatedEdges.size() + " edges found.");
		}

		writeAllEdges(edges, "all-unweighted");
	}

	private static void getAllWeightedEdges() throws IOException {
		HashMap<String, Integer> estimatedEdges = new HashMap<String, Integer>();

		int totalWeight = 0;
		int maxWeight = 1;
		String maxWeightRep = "";

		int n = 0;

		for (Film film : films) {
			String[] allNames = film.getAllNamesArrayWithoutDuplication();
			ArrayList<String> estimatedEdgesInEntry = new ArrayList<String>();

			for (String name : allNames) {
				for (String name2 : allNames) {
					if(Film.isOrganisation(name) || Film.isOrganisation(name2)) continue; //Skip organisations
					if(name.equals(name2)) continue; //Skip the same node

					String rep = name + "->" + name2;
					String rev = name2 + "->" + name;
					//Use this to count the paired appearance of two nodes only once for one film
					if(!estimatedEdgesInEntry.contains(rep) && !estimatedEdgesInEntry.contains(rev)) {
						estimatedEdgesInEntry.add(rep);
						
						//The network is undirected, so we only need to add one edge for a pair of nodes
						if(!estimatedEdges.containsKey(rep) && !estimatedEdges.containsKey(rev)) {
							estimatedEdges.put(rep, 1);
							totalWeight++;
						} else {
							totalWeight++;
							int weight;
							//If the edge already exists, we need to increase the weight
							if(estimatedEdges.containsKey(rep)) {
								weight = estimatedEdges.get(rep) + 1;
								estimatedEdges.put(rep, weight);
							} else if(estimatedEdges.containsKey(rev)) {
								weight = estimatedEdges.get(rev) + 1;
								estimatedEdges.put(rev, weight);
							} else {
								throw new IOException("Edge: " + rep + " or " + rev + " not found!");
							}
							if(weight > maxWeight) {
								maxWeight = weight;
								maxWeightRep = rep;
							} 
						}
					}
				}
			}
			n++;
			System.out.println("Filtering " + n + "/" + films.size());
		}

		n = 0;
		ArrayList<WeightedEdge> edges = new ArrayList<WeightedEdge>();
		for (String edge : estimatedEdges.keySet()) {
			String[] names = edge.split("->");
			if(!DIR_ACT_ONLY) {
				edges.add(new WeightedEdge(nameToAllYearId(names[0]), nameToAllYearId(names[1]), estimatedEdges.get(edge)));
				n++;
				System.out.println("Converting " + n + "/" + estimatedEdges.size());
			}else{
				int id0 = getDirectorAndActorOnlyId(names[0]);
				int id1 = getDirectorAndActorOnlyId(names[1]);

				n++;
				if(id0 != -1 && id1 != -1) {
					edges.add(new WeightedEdge(id0, id1, estimatedEdges.get(edge)));
					System.out.println("Converting " + n + "/" + estimatedEdges.size());
				}
			}
		}

		System.out.println("Year 1949-1966 all done. " + edges.size() + " edges found.");
		System.out.println("Total weight: " + totalWeight);
		System.out.println("Max weight: " + maxWeight + " (" + maxWeightRep + ")");
	
		writeAllWeightedEdges(edges, "all" + (DIR_ACT_ONLY ? "-dir_act" : ""));
	}

	@Deprecated
	private static void getAllUnweightedEdgesInYear(int startYear, int endYear) throws IOException {
		if(startYear > endYear) {
			throw new IOException("Start year must be smaller than end year!");
		}
		String tag = startYear == endYear ? Integer.toString(startYear) : startYear + "-" + endYear;
		File nodeFile = new File("Network/csv/nodes/nodes-" + tag + ".csv");
		if(!nodeFile.exists()) {
			System.err.println("Node file does not exist for year " + tag + "!");
			System.exit(1);
		}

		ArrayList<Film> filmsInYear = new ArrayList<Film>();
		ArrayList<Edge> edges = new ArrayList<Edge>();

		for (Film film : films) {
			if(film.year >= startYear && film.year <= endYear) {
				filmsInYear.add(film);
			}
		}

		for (Film film : filmsInYear) {
			String type = film.getFilmType();
			String category = formatCategories(film.getCategory());
			String production = film.productionToString();
			String key = film.key;
			String[] allNames = film.getAllNamesArrayWithoutDuplication();
			ArrayList<String> estimatedEdges = new ArrayList<String>();
			
			for (String name : allNames) {
				for (String name2 : allNames) {
					if(Film.isOrganisation(name) || Film.isOrganisation(name2)) continue; //Skip organisations
					if(name.equals(name2)) continue; //Skip the same node

					String rep = name + "->" + name2;
					String rev = name2 + "->" + name;
					if(!estimatedEdges.contains(rep) && !estimatedEdges.contains(rev)) {
						//The network is undirected, so we only need to add one edge for a pair of nodes
						estimatedEdges.add(rep);
					}
				}
			}

			for (String edge : estimatedEdges) {
				String[] names = edge.split("->");
				edges.add(new Edge(nameToId(names[0], tag), nameToId(names[1], tag), film.year, production, category, type, key));
			}
		}

		if(startYear == endYear) {
			int year = startYear;
			System.out.println("Year " + year + " done. " + edges.size() + " edges found.");
			writeAllEdges(edges, Integer.toString(year));
		} else {
			System.out.println("Years " + startYear + "-" + endYear + " done. " + edges.size() + " edges found.");
			writeAllEdges(edges, startYear + "-" + endYear);
		}
	}

	private static String formatCategories(String[] categories) {
		String result = "";
		for (String category : categories) {
			result += category + " / ";
		}
		return result.substring(0, result.length() - 3);
	}

	private static void writeAllEdges(ArrayList<Edge> edges, String tag) throws IOException {
		File file = new File(EDGES_ROOT + "/edges-" + tag + ".csv");
		FileWriter fWriter;
		if (file.exists()) {
			fWriter = new FileWriter(file, false);
		} else {
			file.createNewFile();
			fWriter = new FileWriter(file);
		}
		BufferedWriter writer = new BufferedWriter(fWriter);
		writer.append("Source,Target,Label,Year,Production,Category,Film Type\n");
		for (Edge edge : edges) {
			writer.append(edge.toString() + "\n");
		}
		writer.close();
	}

	private static void writeAllWeightedEdges(ArrayList<WeightedEdge> edges, String tag) throws IOException {
		File file = new File(EDGES_ROOT + "/edges-" + tag + ".csv");
		FileWriter fWriter;
		if (file.exists()) {
			fWriter = new FileWriter(file, false);
		} else {
			file.createNewFile();
			fWriter = new FileWriter(file);
		}
		BufferedWriter writer = new BufferedWriter(fWriter);
		writer.append("Source,Target,Weight\n");
		for (WeightedEdge edge : edges) {
			writer.append(edge.toString() + "\n");
		}
		writer.close();
	}

	private static int nameToId(String name, String tag) throws IOException {
		File nodeFile = new File(GetNodes.NODES_ROOT + "/nodes-" + tag + ".csv");
		BufferedReader br = new BufferedReader(new FileReader(nodeFile));
		String line;
		while((line = br.readLine()) != null) {
			String[] parts = line.split(",");
			if(parts[1].replaceAll("\"", "").equals(name)) {
				br.close();
				return Integer.parseInt(parts[0]);
			}
		}
		br.close();
		throw new IOException("Node not found: " + name + " in year " + tag + ".");
	}

	private static int nameToAllYearId(String name) throws IOException {
		File nodeFile = new File(GetNodes.NODES_ROOT + "/nodes-all.csv");
		BufferedReader br = new BufferedReader(new FileReader(nodeFile));
		String line;
		while((line = br.readLine()) != null) {
			String[] parts = line.split(",");
			if(parts[1].replaceAll("\"", "").equals(name)) {
				br.close();
				return Integer.parseInt(parts[0]);
			}
		}
		br.close();
		throw new IOException("Node not found: " + name + " in all-year nodes.");
	}

	private static int getDirectorAndActorOnlyId(String name) throws IOException {
		File nodeFile = new File(GetNodes.NODES_ROOT + "/nodes-all-dir_act.csv");
		BufferedReader br = new BufferedReader(new FileReader(nodeFile));
		String line;
		while((line = br.readLine()) != null) {
			String[] parts = line.split(",");
			if(parts[1].replaceAll("\"", "").equals(name)) {
				br.close();
				return Integer.parseInt(parts[0]);
			}
		}
		br.close();
		return -1;
	}
	
}
