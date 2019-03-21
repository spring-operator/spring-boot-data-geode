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

package example.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.mapping.annotation.Region;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * The {@link Author} class is an Abstract Data Type (ADT) modeling a book author.
 *
 * @author John Blum
 * @see lombok
 * @see org.springframework.data.annotation.Id
 * @see org.springframework.data.gemfire.mapping.annotation.Region
 * @since 1.0.0
 */
@Data
@Region("Authors")
@RequiredArgsConstructor(staticName = "newAuthor")
@SuppressWarnings("unused")
public class Author {

	@Id
	private Long id;

	@NonNull
	private String name;

	public boolean isNew() {
		return getId() == null;
	}

	public Author identifiedBy(Long id) {
		setId(id);
		return this;
	}
}
