/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.webadmin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.james.rrt.cassandra.CassandraMappingsSourcesDAO;
import org.apache.james.rrt.cassandra.migration.MappingsSourcesMigration;
import org.apache.james.server.task.json.JsonTaskSerializer;

import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.junit.jupiter.api.Test;

class CassandraMappingsSolveInconsistenciesTaskTest {
    private static final String SERIALIZED = "{\"type\":\"cassandra-mappings-solve-inconsistencies\"}";
    private static final MappingsSourcesMigration MAPPINGS_SOURCES_MIGRATION = mock(MappingsSourcesMigration.class);
    private static final CassandraMappingsSourcesDAO CASSANDRA_MAPPINGS_SOURCES_DAO = mock(CassandraMappingsSourcesDAO.class);
    private static final CassandraMappingsSolveInconsistenciesTask TASK = new CassandraMappingsSolveInconsistenciesTask(MAPPINGS_SOURCES_MIGRATION, CASSANDRA_MAPPINGS_SOURCES_DAO);
    private static final JsonTaskSerializer TESTEE = new JsonTaskSerializer(CassandraMappingsSolveInconsistenciesTask.module(MAPPINGS_SOURCES_MIGRATION, CASSANDRA_MAPPINGS_SOURCES_DAO));

    @Test
    void taskShouldBeSerializable() throws JsonProcessingException {
        JsonAssertions.assertThatJson(TESTEE.serialize(TASK))
            .isEqualTo(SERIALIZED);
    }

    @Test
    void taskShouldBeDeserializable() throws IOException {
        assertThat(TESTEE.deserialize(SERIALIZED))
            .isEqualToComparingFieldByFieldRecursively(TASK);
    }
}
