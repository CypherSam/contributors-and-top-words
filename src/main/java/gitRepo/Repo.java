package gitRepo;

import java.util.List;

public class Repo {
	String name;
	List<String> contributors;
	List<String> topTenWords;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getContributors() {
		return contributors;
	}
	public void setContributors(List<String> contributors) {
		this.contributors = contributors;
	}
	public List<String> getTopTenWords() {
		return topTenWords;
	}
	public void setTopTenWords(List<String> topTenWords) {
		this.topTenWords = topTenWords;
	}
	
	
	
}
