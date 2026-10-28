package com.education;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.jwt.secret=test-secret-key-that-is-long-enough-for-hs256")
class EducationApplicationTests {

	@Test
	void contextLoads() {
	}

}
