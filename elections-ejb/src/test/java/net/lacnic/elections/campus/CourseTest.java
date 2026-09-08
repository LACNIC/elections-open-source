package net.lacnic.elections.campus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CourseTest {

	@Test
	void shouldExposeCourseDataAndDescription() {
		Course course = new Course(42L, "Nomination Course");

		assertEquals(42L, course.getId());
		assertEquals("Nomination Course", course.getFullName());
		assertEquals("42 - Nomination Course", course.getFullDescription());

		course.setId(43L);
		course.setFullName("Updated");
		assertEquals(43L, course.getId());
		assertEquals("Updated", course.getFullName());
	}
}
