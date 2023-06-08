package Network.JAVA;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import OCR.JAVA.Studio;

public class Node {
	/*public enum NodeType {
		DIRECTOR, SCRIPTWRITER, ACTOR_OR_ACTRESS, OTHER_STAFF
	}*/

	public String name; //For a node representing a filmmaker/actor-or-actress, name is the ID to identify the person, as duplicate names are extremely rare.
	public ArrayList<Studio> allAffiliated; //This represents all studios with which the person had made films (in a specified period of time; may duplicate if this person collaborated with a studio for several times).
	public HashMap<Studio, Integer> allAffiliatedCount; //This represents the number of films the person had made with each studio (in a specified period of time).
	public ArrayList<String> mainAffiliatedCategory; //This represents the categroy of the studios with which the person had made most films (in a specified period of time).
	
	public NodeAppearance firstAppearance; //This represents the info about the first film the person had appeared in (in a specified period of time).
	//public NodeType type;
	
	public ArrayList<String> roles; //This represents the jobs the person had played in the films (in a specified period of time).
	public HashMap<String, Integer> rolesCount; //This represents the number of films the person had played in each job (in a specified period of time).
	public ArrayList<String> mainRoles; //This represents the jobs the person had played in most films (in a specified period of time).

	public int locInList; //Preserved for Gephi data import.

	public Node(String name) {
		this.name = name;
		this.allAffiliated = new ArrayList<Studio>();
		allAffiliatedCount = new HashMap<Studio, Integer>();
		this.mainAffiliatedCategory = new ArrayList<String>();

		this.roles = new ArrayList<String>();
		rolesCount = new HashMap<String, Integer>();
		this.mainRoles = new ArrayList<String>();
	}

	private void addAffiliationCount(Studio studio) {
		if (allAffiliatedCount.containsKey(studio)) {
			allAffiliatedCount.put(studio, allAffiliatedCount.get(studio) + 1);
		} else {
			allAffiliatedCount.put(studio, 1);
		}
	}

	public void addAffiliation(Studio studio) {
		if(!allAffiliated.contains(studio)) allAffiliated.add(studio);
		addAffiliationCount(studio);
	}

	public void addAffiliation(Studio[] studio) {
		for (Studio s : studio) {
			if(!allAffiliated.contains(s)) allAffiliated.add(s);
			addAffiliationCount(s);
		}
	}

	private void addRoleCount(String role) {
		if (rolesCount.containsKey(role)) {
			rolesCount.put(role, rolesCount.get(role) + 1);
		} else {
			rolesCount.put(role, 1);
		}
	}

	public void addRole(String role) {
		if(!roles.contains(role)) roles.add(role);
		addRoleCount(role);
	}

	public void assignLocationInList(int loc) {
		locInList = loc;
	}

	public void getMainAffiliationStudio() {
		ArrayList<Studio> mostFrequent = mostFrequentStudio();
		if(mostFrequent.size() > 1) {
			System.out.println("Warning: " + name + " has more than one most frequent studio: ");
			for (Studio studio : allAffiliated) {
				System.out.println(studio.name);
			}
			System.out.println();
		}
	}

	public void getMainAffiliatedCategory() {
		ArrayList<String> mostFrequent = mostFrequentCategory();
		if(mostFrequent.size() > 1) {
			System.out.println("Warning: " + name + " has more than one most frequent categories: ");
			for (Studio studio : allAffiliated) {
				System.out.println(studio.category);
			}
			System.out.println();
		}
		mainAffiliatedCategory = mostFrequent;
	}

	public void getMainRole() {
		ArrayList<String> mostFrequent = mostFrequentRole();
		if(mostFrequent.size() > 1) {
			System.out.println("Warning: " + name + " has more than one most frequent jobs: ");
			for (String role : roles) {
				System.out.println(role);
			}
			System.out.println();
		}
		mainRoles = mostFrequent;
	}

	//Format the Node according to a docunment of Gephi:
	//https://seinecle.github.io/gephi-tutorials/generated-html/importing-csv-data-in-gephi-en.html
	@Override
	public String toString() {
		String s = locInList + ",\"" + name + "\",\"";
		for (int i=0; i<mainAffiliatedCategory.size(); i++) {
			s += mainAffiliatedCategory.get(i) + ((i < mainAffiliatedCategory.size() - 1) ? " / " : "");
		}
		s += "\",\"";
		for (int i=0; i<allAffiliated.size(); i++) {
			s += allAffiliated.get(i).name + " (" + allAffiliatedCount.get(allAffiliated.get(i)) + ")" + ((i < allAffiliated.size() - 1) ? " & " : "");
		}
		s += "\",\"";
		for (int i=0; i<mainRoles.size(); i++) {
			s += mainRoles.get(i) + ((i < mainRoles.size() - 1) ? " | " : "");
		}
		s += "\",\"";
		for (int i=0; i<roles.size(); i++) {
			s += roles.get(i) + " (" + rolesCount.get(roles.get(i)) + ")" + ((i < roles.size() - 1) ? " & " : "");
		}
		s += "\"";
		if(firstAppearance != null) {
			s += "," + firstAppearance.toString();
		}
		return s;
	}

	private ArrayList<String> mostFrequentRole() {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		int freq = 0;
		String[] roles = this.roles.toArray(new String[this.roles.size()]);
 
		for (int i = 0; i < roles.length; i++) {
			int newFreq = 0;
			if (map.containsKey(roles[i])) {
				newFreq = map.get(roles[i]) + this.rolesCount.get(roles[i]);
				throw new RuntimeException("Unexpected role duplication!");
			} else {
				newFreq = this.rolesCount.get(roles[i]);
			}
			map.put(roles[i], newFreq);
			freq = Math.max(newFreq, freq);
		}
 
        Set<Map.Entry<String, Integer> > set = map.entrySet();
        ArrayList<String> keys = new ArrayList<String>();
		for (Map.Entry<String, Integer> me : set) {
			if (me.getValue() == freq) {
				keys.add(me.getKey());
			}
		}

		return keys;
	}

	private ArrayList<Studio> mostFrequentStudio() {
		HashMap<Studio, Integer> hs = new HashMap<Studio, Integer>();
		int freq = 0;
		Studio[] studios = allAffiliated.toArray(new Studio[allAffiliated.size()]);
 
		for (int i = 0; i < studios.length; i++) {
			int newFreq = 0;
			if (hs.containsKey(studios[i])) {
				newFreq = hs.get(studios[i]) + this.allAffiliatedCount.get(studios[i]);
				throw new RuntimeException("Unexpected studio duplication!");
			} else {
				newFreq = this.allAffiliatedCount.get(studios[i]);
			}
			hs.put(studios[i], newFreq);
			freq = Math.max(newFreq, freq);
		}
 
        Set<Map.Entry<Studio, Integer> > set = hs.entrySet();
        ArrayList<Studio> keys = new ArrayList<Studio>();
		for (Map.Entry<Studio, Integer> me : set) {
			if (me.getValue() == freq) {
				keys.add(me.getKey());
			}
		}

		return keys;
	}

	private ArrayList<String> mostFrequentCategory() {
		Studio[] studios = allAffiliated.toArray(new Studio[allAffiliated.size()]);
		String[] allCats = new String[studios.length];
		for (int i=0; i<studios.length; i++) {
			allCats[i] = studios[i].category;
		}

		HashMap<String, Integer> map = new HashMap<String, Integer>();
		int freq = 0;
 
		for (int i = 0; i < studios.length; i++) {
			int newFreq = 0;
			if (map.containsKey(studios[i].category)) {
				newFreq = map.get(studios[i].category) + this.allAffiliatedCount.get(studios[i]);
			} else {
				newFreq = this.allAffiliatedCount.get(studios[i]);
			}
			map.put(studios[i].category, newFreq);
			freq = Math.max(newFreq, freq);
		}
 
        Set<Map.Entry<String, Integer> > set = map.entrySet();
        ArrayList<String> keys = new ArrayList<String>();
		for (Map.Entry<String, Integer> me : set) {
			if (me.getValue() == freq) {
				keys.add(me.getKey());
			}
		}

		return keys;
	}

	public static class NodeAppearance {
		public int year;
		public String category;
		public boolean firstAppearInPrivate;

		public NodeAppearance(int year, String category, boolean firstAppearInPrivate) {
			this.year = year;
			this.category = category;
			this.firstAppearInPrivate = firstAppearInPrivate;
		}

		@Override
		public String toString() {
			return year + ",\"" + category + "\",\"" + (firstAppearInPrivate ? "True"	: "False") + "\"";
		}
	}

	public void getFirstAppearanceCategory() throws IOException {
		File nodesDir = new File(GetNodes.NODES_ROOT);
		
		for(int year=1949; year<1967; year++) {
			File yearNodes = new File(nodesDir, "nodes-" + year + ".csv");
			BufferedReader reader = new BufferedReader(new FileReader(yearNodes));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(",");
				if(parts[1].replaceAll("\"", "").equals(name)) {
					reader.close();
					NodeAppearance na = new NodeAppearance(year, parts[2].replaceAll("\"", ""), parts[2].contains("Shanghai (private)"));
					this.firstAppearance = na;
					return;
				}
			}
			reader.close();
		}
		throw new IOException("Could not find first appearance of " + name + " in nodes files!");
	}

}
