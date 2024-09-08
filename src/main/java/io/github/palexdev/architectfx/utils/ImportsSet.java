package io.github.palexdev.architectfx.utils;

import java.util.TreeSet;

public class ImportsSet extends TreeSet<String> {
	
	//================================================================================
	// Constructors
	//================================================================================
	public ImportsSet() {
		super((a, b) -> {
        	// Ensure fully qualified imports (non-star) come before star imports
        	boolean aStar = a.endsWith(".*");
        	boolean bStar = b.endsWith(".*");
        	if (aStar && !bStar) return 1;
        	if (!aStar && bStar) return -1;
        	return a.compareTo(b);  // Lexicographical order otherwise
		});
	}
}