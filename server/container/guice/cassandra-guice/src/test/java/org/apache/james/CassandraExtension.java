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

package org.apache.james;

import org.junit.jupiter.api.extension.ExtensionContext;

import com.google.inject.Module;

public class CassandraExtension implements GuiceModuleTestExtension {

    private final DockerCassandraRule cassandra;

    public CassandraExtension() {
        this(new DockerCassandraRule());
    }

    public CassandraExtension(DockerCassandraRule cassandra) {
        this.cassandra = cassandra;
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        cassandra.start();
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        cassandra.stop();
    }

    @Override
    public Module getModule() {
        return cassandra.getModule();
    }

    public void pause() {
        cassandra.pause();
    }

    public void unpause() {
        cassandra.unpause();
    }
}
