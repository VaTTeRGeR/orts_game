/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.ashley.systems;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;

/**
 * A paralleled EntitySystem that iterates over each entity in concurrently running threads and calls processEntity() for each entity every time the EntitySystem is
 * updated. This causes undefined ans unsafe behaviour if entities interact with each other while executing.
 * @author Stefan Bachmann
 */
public abstract class ConcurrentIteratingSystem extends EntitySystem {
	private Family family;
	private ImmutableArray<Entity> entities;
	private int numThreads;
	private ExecutorService executor;
	/**
	 * Instantiates a system that will iterate over the entities described by the Family.
	 * @param family The family of entities iterated over in this System
	 */
	public ConcurrentIteratingSystem (Family family) {
		this(family, 0, 4);
	}

	/**
	 * Instantiates a system that will iterate over the entities described by the Family, with a specific priority.
	 * @param family The family of entities iterated over in this System
	 * @param priority The priority to execute this system with (lower means higher priority)
	 */
	public ConcurrentIteratingSystem (Family family, int priority) {
		this(family, priority, 4);
	}

	/**
	 * Instantiates a system that will iterate over the entities described by the Family, with a specific priority.
	 * @param family The family of entities iterated over in this System
	 * @param priority The priority to execute this system with (lower means higher priority)
	 * @param priority The number of threads to update this system with (Higher means more threads)
	 */
	public ConcurrentIteratingSystem (Family family, int priority, int numThreads) {
		super(priority);
		this.family = family;
		this.numThreads = numThreads;
	}

	@Override
	public void addedToEngine (Engine engine) {
		entities = engine.getEntitiesFor(family);
	}

	@Override
	public void removedFromEngine (Engine engine) {
		entities = null;
	}

	@Override
	public void update (final float deltaTime) {
		executor = Executors.newFixedThreadPool(numThreads);

		final int chunkSize = entities.size()/4;
		
		int begin = 0;
		int end = chunkSize;
		for (int t = 0; t < numThreads; t++) {
			final int s = begin;
			final int e = end;
			
			executor.execute(new Runnable() {
				@Override
				public void run() {
					//System.out.println("Processing enitities "+s+" to "+e);
					for (int i = s; i < e; i++) {
						processEntity(entities.get(i), deltaTime);
					}
				}
			});
			begin = end+1;
			if(t==numThreads-1) {
				end = entities.size();
			} else {
				end+=chunkSize;
			}
		}
		try {
			executor.shutdown();
			executor.awaitTermination(100, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return set of entities processed by the system
	 */
	public ImmutableArray<Entity> getEntities () {
		return entities;
	}

	/**
	 * @return the Family used when the system was created
	 */
	public Family getFamily () {
		return family;
	}

	/**
	 * This method is called on every entity on every update call of the EntitySystem. Override this to implement your system's
	 * specific processing.
	 * @param entity The current Entity being processed
	 * @param deltaTime The delta time between the last and current frame
	 */
	protected abstract void processEntity (Entity entity, float deltaTime);
}
