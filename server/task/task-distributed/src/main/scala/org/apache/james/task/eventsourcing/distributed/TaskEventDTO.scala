/** **************************************************************
  * Licensed to the Apache Software Foundation (ASF) under one   *
  * or more contributor license agreements.  See the NOTICE file *
  * distributed with this work for additional information        *
  * regarding copyright ownership.  The ASF licenses this file   *
  * to you under the Apache License, Version 2.0 (the            *
  * "License"); you may not use this file except in compliance   *
  * with the License.  You may obtain a copy of the License at   *
  *                                                              *
  * http://www.apache.org/licenses/LICENSE-2.0                   *
  *                                                              *
  * Unless required by applicable law or agreed to in writing,   *
  * software distributed under the License is distributed on an  *
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
  * KIND, either express or implied.  See the License for the    *
  * specific language governing permissions and limitations      *
  * under the License.                                           *
  * ***************************************************************/

package org.apache.james.task.eventsourcing.distributed

import java.util.Optional

import com.fasterxml.jackson.annotation.JsonProperty
import org.apache.james.eventsourcing.EventId
import org.apache.james.eventsourcing.eventstore.cassandra.dto.EventDTO
import org.apache.james.server.task.json.{JsonTaskAdditionalInformationSerializer, JsonTaskSerializer}
import org.apache.james.task.eventsourcing._
import org.apache.james.task.{Hostname, Task, TaskId}

import scala.compat.java8.OptionConverters._

sealed abstract class TaskEventDTO(val getType: String, val getAggregate: String, val getEvent: Int) extends EventDTO {
  protected def domainAggregateId: TaskAggregateId = TaskAggregateId(TaskId.fromString(getAggregate))
  protected def domainEventId: EventId = EventId.fromSerialized(getEvent)
}

case class CreatedDTO(@JsonProperty("type") typeName: String,
                      @JsonProperty("aggregate") aggregateId: String,
                      @JsonProperty("event") eventId: Int,
                      @JsonProperty("task") getTask: String,
                      @JsonProperty("hostname") getHostname: String)
  extends TaskEventDTO(typeName, aggregateId, eventId) {
  def toDomainObject(serializer: JsonTaskSerializer): Created = Created(domainAggregateId, domainEventId, serializer.deserialize(getTask), Hostname(getHostname))
}

object CreatedDTO {
  def fromDomainObject(event: Created, typeName: String, serializer: JsonTaskSerializer): CreatedDTO =
    CreatedDTO(typeName, event.aggregateId.taskId.asString(), event.eventId.serialize(), serializer.serialize(event.task), event.hostname.asString)
}

case class StartedDTO(@JsonProperty("type") typeName: String,
                      @JsonProperty("aggregate") aggregateId: String,
                      @JsonProperty("event") eventId: Int,
                      @JsonProperty("hostname") getHostname: String)
  extends TaskEventDTO(typeName, aggregateId, eventId) {
  def toDomainObject: Started = Started(domainAggregateId, domainEventId, Hostname(getHostname))
}

object StartedDTO {
  def fromDomainObject(event: Started, typeName: String): StartedDTO =
    StartedDTO(typeName, event.aggregateId.taskId.asString(), event.eventId.serialize(), event.hostname.asString)
}

case class CancelRequestedDTO(@JsonProperty("type") typeName: String,
                              @JsonProperty("aggregate") aggregateId: String,
                              @JsonProperty("event") eventId: Int,
                              @JsonProperty("hostname") getHostname: String)
  extends TaskEventDTO(typeName, aggregateId, eventId) {
  def toDomainObject: CancelRequested = CancelRequested(domainAggregateId, domainEventId, Hostname(getHostname))
}

object CancelRequestedDTO {
  def fromDomainObject(event: CancelRequested, typeName: String): CancelRequestedDTO =
    CancelRequestedDTO(typeName, event.aggregateId.taskId.asString(), event.eventId.serialize(), event.hostname.asString)
}

case class CompletedDTO(@JsonProperty("type") typeName: String,
                        @JsonProperty("aggregate") aggregateId: String,
                        @JsonProperty("event") eventId: Int,
                        @JsonProperty("result") getResult: String,
                        @JsonProperty("additionalInformation") getAdditionalInformation: Optional[String])
  extends TaskEventDTO(typeName, aggregateId, eventId) {
  def toDomainObject(jsonTaskAdditionalInformationSerializer: JsonTaskAdditionalInformationSerializer): Completed = {
    val deserializedAdditionalInformation = getAdditionalInformation.asScala.map(jsonTaskAdditionalInformationSerializer.deserialize)
    Completed(domainAggregateId, domainEventId, domainResult, deserializedAdditionalInformation)

  }
  private def domainResult: Task.Result = getResult match {
    case "COMPLETED" => Task.Result.COMPLETED
    case "PARTIAL" => Task.Result.PARTIAL
  }
}

object CompletedDTO {
  def fromDomainObject(jsonTaskAdditionalInformationSerializer: JsonTaskAdditionalInformationSerializer)(event: Completed, typeName: String): CompletedDTO = {
    val serializedAdditionalInformation = event.additionalInformation.map(jsonTaskAdditionalInformationSerializer.serialize).asJava
    CompletedDTO(typeName, event.aggregateId.taskId.asString(), event.eventId.serialize(), resultToString(event.result), serializedAdditionalInformation)
  }

  private def resultToString(result: Task.Result): String = result match {
    case Task.Result.COMPLETED => "COMPLETED"
    case Task.Result.PARTIAL => "PARTIAL"
  }
}

case class FailedDTO(@JsonProperty("type") typeName: String,
                     @JsonProperty("aggregate") aggregateId: String,
                     @JsonProperty("event") eventId: Int,
                     @JsonProperty("additionalInformation") getAdditionalInformation: Optional[String],
                     @JsonProperty("errorMessage") getErrorMessage: Optional[String],
                     @JsonProperty("exception") getException: Optional[String])
  extends TaskEventDTO(typeName, aggregateId, eventId) {
  def toDomainObject(jsonTaskAdditionalInformationSerializer: JsonTaskAdditionalInformationSerializer): Failed = {
    val deserializedAdditionalInformation = getAdditionalInformation.asScala.map(jsonTaskAdditionalInformationSerializer.deserialize)
    Failed(domainAggregateId, domainEventId, deserializedAdditionalInformation, getErrorMessage.asScala, getException.asScala)
  }
}

object FailedDTO {
  def fromDomainObject(jsonTaskAdditionalInformationSerializer: JsonTaskAdditionalInformationSerializer)(event: Failed, typeName: String): FailedDTO = {
    val serializedAdditionalInformation = event.additionalInformation.map(jsonTaskAdditionalInformationSerializer.serialize).asJava
    FailedDTO(typeName, event.aggregateId.taskId.asString(), event.eventId.serialize(), serializedAdditionalInformation, event.errorMessage.asJava, event.exception.asJava)
  }
}

case class CancelledDTO(@JsonProperty("type") typeName: String,
                        @JsonProperty("aggregate") aggregateId: String,
                        @JsonProperty("event") eventId: Int,
                        @JsonProperty("additionalInformation") getAdditionalInformation: Optional[String])
  extends TaskEventDTO(typeName, aggregateId, eventId) {
  def toDomainObject(jsonTaskAdditionalInformationSerializer: JsonTaskAdditionalInformationSerializer): Cancelled = {
    val deserializedAdditionalInformation = getAdditionalInformation.asScala.map(jsonTaskAdditionalInformationSerializer.deserialize)
    Cancelled(domainAggregateId, domainEventId, deserializedAdditionalInformation)
  }
}

object CancelledDTO {
  def fromDomainObject(jsonTaskAdditionalInformationSerializer: JsonTaskAdditionalInformationSerializer)(event: Cancelled, typeName: String): CancelledDTO = {
    val serializedAdditionalInformation = event.additionalInformation.map(jsonTaskAdditionalInformationSerializer.serialize).asJava
    CancelledDTO(typeName, event.aggregateId.taskId.asString(), event.eventId.serialize(), serializedAdditionalInformation)
  }
}

case class AdditionalInformationUpdatedDTO(@JsonProperty("type") typeName: String,
                     @JsonProperty("aggregate") aggregateId: String,
                     @JsonProperty("event") eventId: Int,
                     @JsonProperty("additionalInformation") getAdditionalInformation: String)
  extends TaskEventDTO(typeName, aggregateId, eventId) {
  def toDomainObject(jsonTaskAdditionalInformationSerializer: JsonTaskAdditionalInformationSerializer): AdditionalInformationUpdated = {
    val deserializedAdditionalInformation = jsonTaskAdditionalInformationSerializer.deserialize(getAdditionalInformation)
    AdditionalInformationUpdated(domainAggregateId, domainEventId, deserializedAdditionalInformation)
  }
}

object AdditionalInformationUpdatedDTO {
  def fromDomainObject(jsonTaskAdditionalInformationSerializer: JsonTaskAdditionalInformationSerializer)(event: AdditionalInformationUpdated, typeName: String): AdditionalInformationUpdatedDTO = {
    val serializedAdditionalInformation = jsonTaskAdditionalInformationSerializer.serialize(event.additionalInformation)
    AdditionalInformationUpdatedDTO(typeName, event.aggregateId.taskId.asString(), event.eventId.serialize(), serializedAdditionalInformation)
  }
}
