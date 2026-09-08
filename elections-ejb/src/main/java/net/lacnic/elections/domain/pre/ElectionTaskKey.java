package net.lacnic.elections.domain.pre;

public enum ElectionTaskKey {
	PROFILE(TaskDependencyLevel.LEVEL_1, 10),
	COUNTRIES(TaskDependencyLevel.LEVEL_1, 20),
	INCOMPATIBILITIES(TaskDependencyLevel.LEVEL_1, 30),
	COURSE(TaskDependencyLevel.LEVEL_2, 40),
	ORG_SUPPORTS(TaskDependencyLevel.LEVEL_2, 50),
	USER_SUPPORTS_2(TaskDependencyLevel.LEVEL_2, 60),
	USER_SUPPORTS_5(TaskDependencyLevel.LEVEL_2, 70),
	ORGANIZATIONS(TaskDependencyLevel.LEVEL_2, 80),
	OTHER_STATUTORY_QUESTIONS(TaskDependencyLevel.LEVEL_2, 90),
	OTHER_NON_STATUTORY_QUESTIONS(TaskDependencyLevel.LEVEL_2, 100),
	DECLARATIONS(TaskDependencyLevel.LEVEL_2, 110),
	DECLARATIONS_NON_STATUTORY(TaskDependencyLevel.LEVEL_2, 120),
	EVALUATION(TaskDependencyLevel.LEVEL_3, 130);

	private final TaskDependencyLevel defaultDependencyLevel;
	private final int defaultDisplayOrder;

	ElectionTaskKey(TaskDependencyLevel defaultDependencyLevel, int defaultDisplayOrder) {
		this.defaultDependencyLevel = defaultDependencyLevel;
		this.defaultDisplayOrder = defaultDisplayOrder;
	}

	public TaskDependencyLevel getDefaultDependencyLevel() {
		return defaultDependencyLevel;
	}

	public int getDefaultDisplayOrder() {
		return defaultDisplayOrder;
	}
}
