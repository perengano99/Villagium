package com.perengano99.villagium.social.profile;

public enum NvGender {
	UNDEFINED(0), MALE(1), FEMALE(2), OTHER(3);
	
	public final int id;
	
	NvGender(int id) {
		this.id = id;
	}
	
	public static NvGender fromId(int id) {
		return switch (id) {
			case 1 -> MALE;
			case 2 -> FEMALE;
			case 3 -> OTHER;
			default -> UNDEFINED;
		};
	}
}
