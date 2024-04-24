/*
 * Copyright 2002-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.annotation;

import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.parsing.FailFastProblemReporter;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ConfigurationClassParser}, {@link ConfigurationClass},
 * and {@link BeanMethod}.
 *
 * @author Sam Brannen
 * @since 5.3.9
 */
class ConfigurationClassAndBeanMethodTests {

	@Test
	void verifyEquals() throws Exception {
		ConfigurationClass configurationClass1 = newConfigurationClass(Config1.class);
		ConfigurationClass configurationClass2 = newConfigurationClass(Config1.class);
		ConfigurationClass configurationClass3 = newConfigurationClass(Config2.class);

		assertThat(configurationClass1).isNotEqualTo(null);
		assertThat(configurationClass1).isNotSameAs(configurationClass2);

		assertThat(configurationClass1).isEqualTo(configurationClass1);
		assertThat(configurationClass2).isEqualTo(configurationClass2);
		assertThat(configurationClass1).isEqualTo(configurationClass2);
		assertThat(configurationClass2).isEqualTo(configurationClass1);

		assertThat(configurationClass1).isNotEqualTo(configurationClass3);
		assertThat(configurationClass3).isNotEqualTo(configurationClass2);

		// ---------------------------------------------------------------------

		List<BeanMethod> beanMethods1 = getBeanMethods(configurationClass1);
		BeanMethod beanMethod10 = beanMethods1.get(0);
		BeanMethod beanMethod11 = beanMethods1.get(1);
		BeanMethod beanMethod12 = beanMethods1.get(2);

		List<BeanMethod> beanMethods2 = getBeanMethods(configurationClass2);
		BeanMethod beanMethod20 = beanMethods2.get(0);
		BeanMethod beanMethod21 = beanMethods2.get(1);
		BeanMethod beanMethod22 = beanMethods2.get(2);

		List<BeanMethod> beanMethods3 = getBeanMethods(configurationClass3);
		BeanMethod beanMethod30 = beanMethods3.get(0);
		BeanMethod beanMethod31 = beanMethods3.get(1);
		BeanMethod beanMethod32 = beanMethods3.get(2);

		assertThat(beanMethod10).isNotEqualTo(null);
		assertThat(beanMethod10).isNotSameAs(beanMethod20);

		assertThat(beanMethod10).isEqualTo(beanMethod10);
		assertThat(beanMethod10).isEqualTo(beanMethod20);
		assertThat(beanMethod11).isEqualTo(beanMethod21);
		assertThat(beanMethod12).isEqualTo(beanMethod22);

		assertThat(beanMethod10.getMetadata().getMethodName()).isEqualTo(beanMethod30.getMetadata().getMethodName());
		assertThat(beanMethod10).isNotEqualTo(beanMethod30);
		assertThat(beanMethod11).isNotEqualTo(beanMethod31);
		assertThat(beanMethod12).isNotEqualTo(beanMethod32);
	}

	@Test
	void verifyHashCode() throws Exception {
		ConfigurationClass configurationClass1 = newConfigurationClass(Config1.class);
		ConfigurationClass configurationClass2 = newConfigurationClass(Config1.class);
		ConfigurationClass configurationClass3 = newConfigurationClass(Config2.class);

		assertThat(configurationClass1).hasSameHashCodeAs(configurationClass2);
		assertThat(configurationClass1).doesNotHaveSameHashCodeAs(configurationClass3);

		// ---------------------------------------------------------------------

		List<BeanMethod> beanMethods1 = getBeanMethods(configurationClass1);
		BeanMethod beanMethod10 = beanMethods1.get(0);
		BeanMethod beanMethod11 = beanMethods1.get(1);
		BeanMethod beanMethod12 = beanMethods1.get(2);

		List<BeanMethod> beanMethods2 = getBeanMethods(configurationClass2);
		BeanMethod beanMethod20 = beanMethods2.get(0);
		BeanMethod beanMethod21 = beanMethods2.get(1);
		BeanMethod beanMethod22 = beanMethods2.get(2);

		List<BeanMethod> beanMethods3 = getBeanMethods(configurationClass3);
		BeanMethod beanMethod30 = beanMethods3.get(0);
		BeanMethod beanMethod31 = beanMethods3.get(1);
		BeanMethod beanMethod32 = beanMethods3.get(2);

		assertThat(beanMethod10).hasSameHashCodeAs(beanMethod20);
		assertThat(beanMethod11).hasSameHashCodeAs(beanMethod21);
		assertThat(beanMethod12).hasSameHashCodeAs(beanMethod22);

		assertThat(beanMethod10).doesNotHaveSameHashCodeAs(beanMethod30);
		assertThat(beanMethod11).doesNotHaveSameHashCodeAs(beanMethod31);
		assertThat(beanMethod12).doesNotHaveSameHashCodeAs(beanMethod32);
	}

	@Test
	void verifyToString() throws Exception {
		ConfigurationClass configurationClass = newConfigurationClass(Config1.class);
		assertThat(configurationClass.toString())
			.startsWith("ConfigurationClass: beanName 'Config1', class path resource");

		List<BeanMethod> beanMethods = getBeanMethods(configurationClass);
		String prefix = "BeanMethod: " + Config1.class.getName();
		assertThat(beanMethods.get(0).toString()).isEqualTo(prefix + ".bean0()");
		assertThat(beanMethods.get(1).toString()).isEqualTo(prefix + ".bean1(java.lang.String)");
		assertThat(beanMethods.get(2).toString()).isEqualTo(prefix + ".bean2(java.lang.String,java.lang.Integer)");
	}


	private static ConfigurationClass newConfigurationClass(Class<?> clazz) throws Exception {
		ConfigurationClassParser parser = newParser();
		parser.parse(clazz.getName(), clazz.getSimpleName());
		assertThat(parser.getConfigurationClasses()).hasSize(1);
		return parser.getConfigurationClasses().iterator().next();
	}

	private static ConfigurationClassParser newParser() {
		return new ConfigurationClassParser(
				new CachingMetadataReaderFactory(),
				new FailFastProblemReporter(),
				new StandardEnvironment(),
				new DefaultResourceLoader(),
				new AnnotationBeanNameGenerator(),
				new DefaultListableBeanFactory());
	}

	private static List<BeanMethod> getBeanMethods(ConfigurationClass configurationClass) {
		List<BeanMethod> beanMethods = configurationClass.getBeanMethods().stream()
				.sorted(Comparator.comparing(beanMethod -> beanMethod.getMetadata().getMethodName()))
				.toList();
		assertThat(beanMethods).hasSize(3);
		return beanMethods;
	}

	static class Config1 {

		@Bean
		String bean0() {
			return "";
		}

		@Bean
		String bean1(String text) {
			return "";
		}

		@Bean
		String bean2(String text, Integer num) {
			return "";
		}

	}

	static class Config2 {

		@Bean
		String bean0() {
			return "";
		}

		@Bean
		String bean1(String text) {
			return "";
		}

		@Bean
		String bean2(String text, Integer num) {
			return "";
		}

	}

}
