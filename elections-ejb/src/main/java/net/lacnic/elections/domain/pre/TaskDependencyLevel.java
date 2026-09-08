package net.lacnic.elections.domain.pre;

public enum TaskDependencyLevel {
	LEVEL_1(1),
	LEVEL_2(2),
	LEVEL_3(3),
	LEVEL_4(4),
	LEVEL_5(5),
	LEVEL_6(6);

	private final int index;

	TaskDependencyLevel(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public boolean isFirstLevel() {
		return this == LEVEL_1;
	}

	public static TaskDependencyLevel fromIndex(int index) {
		for (TaskDependencyLevel level : values()) {
			if (level.index == index) {
				return level;
			}
		}
		return LEVEL_1;
	}
}
