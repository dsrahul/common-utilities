package com.sample.examples.db2.common;


public class LayerContextHolder {

	private static final InheritableThreadLocal<Area> contextHolder = new InheritableThreadLocal<Area>();

	public static void setAreaType(Area areaType) {
		contextHolder.set(areaType);
	}

	public static Area getAreaType() {

		return (Area) contextHolder.get();
	}

	public static void clearAreaType() {
		contextHolder.remove();
	}
}
