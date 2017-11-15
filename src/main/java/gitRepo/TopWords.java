package gitRepo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class TopWords {
	
	public static String clean(String word) {
		word = word.replaceAll("\\n", "");
		word = word.replaceAll("\\r", "");
		word = word.replaceAll("\\\\", "");
		word = word.replaceAll("(#[0-9]+)", "");
		word = word.replaceAll("\\*", "");
		word = word.replaceAll("\\(\\)", "");
		return word;
	}
	
	// Collect all contributors for a Repo
	public static List<String> getContributors(String url) throws IOException {
		Set<String> contributors = new HashSet<>();
		JsonNode contributorNode = getNode(url);
		
		Iterator<JsonNode> nodeIter = contributorNode.iterator();
		while(nodeIter.hasNext()) {
			JsonNode node = nodeIter.next();
			contributors.add(node.get("login").asText());
		}
		
		return new ArrayList<>(contributors);
	}

	// Collect all words from commit messages and return only top 10
	public static List<String> topTenWords(String url) throws IOException {
		HashMap<String,Integer> wordCount = new HashMap<>();
		JsonNode commitNode = getNode(url);

		Iterator<JsonNode> commitNodes = commitNode.iterator();
		while(commitNodes.hasNext()) {
			JsonNode node = commitNodes.next();

			String[] words = node.path("commit").get("message").asText().split("\\s+");
			for(int i = 0; i<words.length; i++) {
				words[i] = clean(words[i]);
				if(words[i].length()>0) {
					wordCount.putIfAbsent(words[i], 0);
					wordCount.put(words[i], wordCount.get(words[i]) + 1);
				}
			}			
		}
				
		Map<String,Integer> sortedMap = sortMapByValue(wordCount);
		
		int count = 0;
		List<String> res = new ArrayList<>();
		for(String word : sortedMap.keySet()) {
			res.add(word);
			if(count++ >= 9) break;
		}
		
		//System.out.println(res);
		
		return res;
	}
	
	public static TreeMap<String, Integer> sortMapByValue(HashMap<String, Integer> map){
		Comparator<String> comparator = new ValueComparator(map);
		//TreeMap is a map sorted by its keys. 
		//The comparator is used to sort the TreeMap by keys. 
		TreeMap<String, Integer> result = new TreeMap<String, Integer>(comparator);
		result.putAll(map);
		return result;
	}

	public static JsonNode getNode(String urlString) throws IOException {
		URL url = new URL(urlString);
		HttpURLConnection myURLConnection = (HttpURLConnection) url.openConnection();

		myURLConnection.setRequestProperty("Authorization", "token 3364264f4b0976076686f6372a08b30cf21b03e4");
		InputStream is = myURLConnection.getInputStream();

		BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
		String jsonText = readAll(rd);
		JsonNode node = new ObjectMapper().readTree(jsonText);
		return node;
	}

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		List<Repo> repoList = new ArrayList<Repo>();

		try {
			JsonNode golangNode = getNode("https://api.github.com/users/golang");
			JsonNode reposNode = getNode(golangNode.get("repos_url").asText());

			Iterator<JsonNode> repoNodes = reposNode.iterator();
			while (repoNodes.hasNext()) {
				Repo r = new Repo();
				
				JsonNode node = repoNodes.next();
				r.setName(node.get("name").asText());
				String commitURL = node.get("commits_url").asText().replaceAll("\\{/sha\\}", "");
				r.setTopTenWords(topTenWords(commitURL));
				
				String contributorURL = node.get("contributors_url").asText();
				r.setContributors(getContributors(contributorURL));
				
				repoList.add(r);
				
				System.out.printf("Repo : %s \n Contributors : %s\n Top Ten Words : %s\n\n",r.name, r.contributors,r.topTenWords);
				
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		

	}

}
