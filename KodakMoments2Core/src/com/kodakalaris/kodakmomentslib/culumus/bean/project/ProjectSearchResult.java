package com.kodakalaris.kodakmomentslib.culumus.bean.project;

import java.io.Serializable;
import java.util.List;

public class ProjectSearchResult implements Serializable{

	private static final long serialVersionUID = 1L;
	
	public static final String TotalMatchingProjects = "TotalMatchingProjects";
	public static final String NumberReturned = "NumberReturned";
	public static final String StartingIndex = "StartingIndex";
	
	public int totalMatchingProjects;
	public int numberReturned;
	public int startingIndex;
	public List<Project> projects;

}
