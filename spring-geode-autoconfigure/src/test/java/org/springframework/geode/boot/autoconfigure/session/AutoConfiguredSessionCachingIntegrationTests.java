/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.springframework.geode.boot.autoconfigure.session;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionAttributes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.gemfire.GemfireAccessor;
import org.springframework.data.gemfire.GemfireOperations;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.data.gemfire.util.RegionUtils;
import org.springframework.geode.boot.autoconfigure.ContinuousQueryAutoConfiguration;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.data.gemfire.GemFireOperationsSessionRepository;
import org.springframework.session.data.gemfire.config.annotation.web.http.GemFireHttpSessionConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration Tests for auto-configuration of Spring Session using either Apache Geode or Pivotal GemFire
 * as the {@link Session} state management provider.
 *
 * This test asserts that the Spring Boot auto-configuration properly configures Spring Session
 * with either Apache Geode or Pivotal GemFire
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.Region
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.data.gemfire.GemfireTemplate
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @see org.springframework.session.Session
 * @see org.springframework.session.SessionRepository
 * @see org.springframework.session.data.gemfire.GemFireOperationsSessionRepository
 * @see org.springframework.session.data.gemfire.config.annotation.web.http.GemFireHttpSessionConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
	properties = "spring.session.data.gemfire.cache.client.region.shortcut=",
	webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@SuppressWarnings("unused")
public class AutoConfiguredSessionCachingIntegrationTests extends IntegrationTestsSupport {

	@Autowired
	private ApplicationContext applicationContext;

	@Before
	public void setup() {

		assertThat(this.applicationContext).isNotNull();
		assertThat(this.applicationContext.getBean(GemFireHttpSessionConfiguration.class)).isNotNull();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void sessionRegionExists() {

		assertThat(this.applicationContext.containsBean(GemFireHttpSessionConfiguration.DEFAULT_SESSION_REGION_NAME))
			.isTrue();

		Region<Object, Session> sessionRegion =
			this.applicationContext.getBean(GemFireHttpSessionConfiguration.DEFAULT_SESSION_REGION_NAME, Region.class);

		assertThat(sessionRegion).isNotNull();
		assertThat(sessionRegion.getName()).isEqualTo(GemFireHttpSessionConfiguration.DEFAULT_SESSION_REGION_NAME);
		assertThat(sessionRegion.getFullPath())
			.isEqualTo(RegionUtils.toRegionPath(GemFireHttpSessionConfiguration.DEFAULT_SESSION_REGION_NAME));

		RegionAttributes<Object, Session> sessionRegionAttributes = sessionRegion.getAttributes();

		assertThat(sessionRegionAttributes).isNotNull();
		assertThat(sessionRegionAttributes.getDataPolicy()).isEqualTo(DataPolicy.EMPTY);
	}

	@Test
	public void sessionRepositoryExists() {

		assertThat(this.applicationContext.containsBean("sessionRepository")).isTrue();

		SessionRepository<?> sessionRepository =
			this.applicationContext.getBean("sessionRepository", SessionRepository.class);

		assertThat(sessionRepository).isInstanceOf(GemFireOperationsSessionRepository.class);

		GemfireOperations gemfireOperations = ((GemFireOperationsSessionRepository) sessionRepository).getTemplate();

		Region<?, ?> sessionRegion =
			this.applicationContext.getBean(GemFireHttpSessionConfiguration.DEFAULT_SESSION_REGION_NAME, Region.class);

		GemfireTemplate sessionRegionTemplate =
			this.applicationContext.getBean("sessionRegionTemplate", GemfireTemplate.class);

		assertThat(gemfireOperations).isInstanceOf(GemfireAccessor.class);
		assertThat(gemfireOperations).isSameAs(sessionRegionTemplate);
		assertThat(((GemfireAccessor) gemfireOperations).getRegion()).isEqualTo(sessionRegion);
	}

	@EnableGemFireMockObjects
	@SpringBootApplication(exclude = ContinuousQueryAutoConfiguration.class)
	static class TestConfiguration { }

}
