package org.pyfuscator.utils;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

//random name generator
public class NameGenerator {

	private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
	private static final String FIRST_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_";

	private final String prefix;
	private final int length;
	private final Set<String> used = new HashSet<>();
	private final Random rng;

	// constructor with parameters
	public NameGenerator(Long seed, String prefix, int length) {
		if (seed != null) {
			this.rng = new Random(seed);
		} else {
			this.rng = new Random();
		}
		this.prefix = prefix == null ? "" : prefix;
		this.length = length;
	}

	// constructor from config
	public NameGenerator(ObfuscationConfig config) {
		this(config.getSeed(), config.getVarPrefix(), config.getVarLength());
	}

	// default constructor with no prefix and legnth of 8
	public NameGenerator() {
		this(null, "v", 8);
	}

	public void reset() {
		used.clear();
	}

	public String generate() {
		return generate(this.length);
	}

	public String generate(int length) {
		while (true) {
			String candidate = buildCandidate(length);
			if (used.add(candidate)) {
				return candidate;
			}
		}
	}

	// builds a potential variable name
	private String buildCandidate(int bodyLength) {
		StringBuilder sb = new StringBuilder();
		if (!prefix.isEmpty()) {
			sb.append(prefix);
			if (!prefix.endsWith("_")) sb.append("_");
		}

		sb.append(FIRST_CHARS.charAt(rng.nextInt(FIRST_CHARS.length())));
		for (int i = 1; i < bodyLength; i++) {
			sb.append(ALPHANUMERIC.charAt(rng.nextInt(ALPHANUMERIC.length())));
		}
		return sb.toString();
	}

	public int usedCount() {
		return used.size();
	}

}