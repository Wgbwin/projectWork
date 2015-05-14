package com.kodakalaris.kodakmomentslib.interfaces;

import java.io.Serializable;
import java.util.Map;

public interface SaveRestoreAble {
	void saveGlobalVariables(Map<String, Serializable> saveMaps);
	void restoreGlobalVariables(Map<String, Serializable> restoreMaps);
}
