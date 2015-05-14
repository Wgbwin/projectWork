package com.kodakalaris.kodakmomentslib.db;

public class DatabaseVersion {
	private int versionNum;
	private String sql;

	public int getVersionNum() {
		return versionNum;
	}

	public void setVersionNum(int versionNum) {
		this.versionNum = versionNum;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}
}
