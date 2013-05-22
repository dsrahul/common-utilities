package com.sample.examples.db2.common;

public enum Area {
	TPU005, TCDS, SCDS, ECDS, TDTK, SDTK, TSSM, SSSM, ADBA, LDBA;

	public static <T extends Enum<T>> String[] enumNameToStringArray() {
		int i = 0;
		Area[] values = Area.values();
		String[] result = new String[values.length];
		for (Area value : values) {
			result[i++] = value.name();
		}
		return result;
	}
}