/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.helper;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.application.ProcessApplicationInfo;
import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.filter.FilterQuery;
import org.camunda.bpm.engine.form.FormField;
import org.camunda.bpm.engine.form.FormProperty;
import org.camunda.bpm.engine.form.FormType;
import org.camunda.bpm.engine.form.StartFormData;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricActivityStatistics;
import org.camunda.bpm.engine.history.HistoricCaseActivityInstance;
import org.camunda.bpm.engine.history.HistoricCaseInstance;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricFormField;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.TaskQueryImpl;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;
import org.camunda.bpm.engine.management.ActivityStatistics;
import org.camunda.bpm.engine.management.IncidentStatistics;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.management.ProcessDefinitionStatistics;
import org.camunda.bpm.engine.query.Query;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.Resource;
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Attachment;
import org.camunda.bpm.engine.task.Comment;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * Provides mocks for the basic engine entities, such as
 * {@link ProcessDefinition}, {@link User}, etc., that are reused across the
 * various kinds of tests.
 *
 * @author Thorben Lindhauer
 *
 */
public abstract class MockProvider {

  // general non existing Id
  public static final String NON_EXISTING_ID = "nonExistingId";

  // engine
  public static final String EXAMPLE_PROCESS_ENGINE_NAME = "default";
  public static final String ANOTHER_EXAMPLE_PROCESS_ENGINE_NAME = "anotherEngineName";
  public static final String NON_EXISTING_PROCESS_ENGINE_NAME = "aNonExistingEngineName";

  // task properties
  public static final String EXAMPLE_TASK_ID = "anId";
  public static final String EXAMPLE_TASK_NAME = "aName";
  public static final String EXAMPLE_TASK_ASSIGNEE_NAME = "anAssignee";
  public static final String EXAMPLE_TASK_CREATE_TIME = "2013-01-23T13:42:42";
  public static final String EXAMPLE_TASK_DUE_DATE = "2013-01-23T13:42:43";
  public static final String EXAMPLE_FOLLOW_UP_DATE = "2013-01-23T13:42:44";
  public static final DelegationState EXAMPLE_TASK_DELEGATION_STATE = DelegationState.RESOLVED;
  public static final String EXAMPLE_TASK_DESCRIPTION = "aDescription";
  public static final String EXAMPLE_TASK_EXECUTION_ID = "anExecution";
  public static final String EXAMPLE_TASK_OWNER = "anOwner";
  public static final String EXAMPLE_TASK_PARENT_TASK_ID = "aParentId";
  public static final int EXAMPLE_TASK_PRIORITY = 42;
  public static final String EXAMPLE_TASK_DEFINITION_KEY = "aTaskDefinitionKey";
  public static final boolean EXAMPLE_TASK_SUSPENSION_STATE = false;

  // task comment
  public static final String EXAMPLE_TASK_COMMENT_ID = "aTaskCommentId";
  public static final String EXAMPLE_TASK_COMMENT_FULL_MESSAGE = "aTaskCommentFullMessage";
  public static final String EXAMPLE_TASK_COMMENT_TIME = "2014-04-24T14:10:44";

  // task attachment
  public static final String EXAMPLE_TASK_ATTACHMENT_ID = "aTaskAttachmentId";
  public static final String EXAMPLE_TASK_ATTACHMENT_NAME = "aTaskAttachmentName";
  public static final String EXAMPLE_TASK_ATTACHMENT_DESCRIPTION = "aTaskAttachmentDescription";
  public static final String EXAMPLE_TASK_ATTACHMENT_TYPE = "aTaskAttachmentType";
  public static final String EXAMPLE_TASK_ATTACHMENT_URL = "aTaskAttachmentUrl";

  // form data
  public static final String EXAMPLE_FORM_KEY = "aFormKey";
  public static final String EXAMPLE_DEPLOYMENT_ID = "aDeploymentId";

  // form property data
  public static final String EXAMPLE_FORM_PROPERTY_ID = "aFormPropertyId";
  public static final String EXAMPLE_FORM_PROPERTY_NAME = "aFormName";
  public static final String EXAMPLE_FORM_PROPERTY_TYPE_NAME = "aFormPropertyTypeName";
  public static final String EXAMPLE_FORM_PROPERTY_VALUE = "aValue";
  public static final boolean EXAMPLE_FORM_PROPERTY_READABLE = true;
  public static final boolean EXAMPLE_FORM_PROPERTY_WRITABLE = true;
  public static final boolean EXAMPLE_FORM_PROPERTY_REQUIRED = true;

  // process instance
  public static final String EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY = "aKey";
  public static final String EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY_LIKE = "aKeyLike";
  public static final String EXAMPLE_PROCESS_INSTANCE_ID = "aProcInstId";
  public static final String ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID = "anotherId";
  public static final boolean EXAMPLE_PROCESS_INSTANCE_IS_SUSPENDED = false;
  public static final boolean EXAMPLE_PROCESS_INSTANCE_IS_ENDED = false;
  public static final String EXAMPLE_PROCESS_INSTANCE_ID_LIST = EXAMPLE_PROCESS_INSTANCE_ID + "," + ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID;
  public static final String EXAMPLE_PROCESS_INSTANCE_ID_LIST_WITH_DUP = EXAMPLE_PROCESS_INSTANCE_ID + "," + ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID + "," + EXAMPLE_PROCESS_INSTANCE_ID;
  public static final String EXAMPLE_NON_EXISTENT_PROCESS_INSTANCE_ID = "aNonExistentProcInstId";
  public static final String EXAMPLE_PROCESS_INSTANCE_ID_LIST_WITH_NONEXISTENT_ID = EXAMPLE_PROCESS_INSTANCE_ID + "," + EXAMPLE_NON_EXISTENT_PROCESS_INSTANCE_ID;


  // variable instance
  public static final String EXAMPLE_VARIABLE_INSTANCE_ID = "aVariableInstanceId";

  public static final String SERIALIZABLE_VARIABLE_INSTANCE_ID = "serializableVariableInstanceId";
  public static final String SPIN_VARIABLE_INSTANCE_ID = "spinVariableInstanceId";

  public static final String EXAMPLE_VARIABLE_INSTANCE_NAME = "aVariableInstanceName";

  public static final StringValue EXAMPLE_PRIMITIVE_VARIABLE_VALUE = Variables.stringValue("aVariableInstanceValue");
  public static final String EXAMPLE_VARIABLE_INSTANCE_PROC_INST_ID = "aVariableInstanceProcInstId";
  public static final String EXAMPLE_VARIABLE_INSTANCE_EXECUTION_ID = "aVariableInstanceExecutionId";
  public static final String EXAMPLE_VARIABLE_INSTANCE_CASE_INST_ID = "aVariableInstanceCaseInstId";
  public static final String EXAMPLE_VARIABLE_INSTANCE_CASE_EXECUTION_ID = "aVariableInstanceCaseExecutionId";
  public static final String EXAMPLE_VARIABLE_INSTANCE_TASK_ID = "aVariableInstanceTaskId";
  public static final String EXAMPLE_VARIABLE_INSTANCE_ACTIVITY_INSTANCE_ID = "aVariableInstanceVariableInstanceId";
  public static final String EXAMPLE_VARIABLE_INSTANCE_ERROR_MESSAGE = "aVariableInstanceErrorMessage";

  public static final String EXAMPLE_VARIABLE_INSTANCE_SERIALIZED_VALUE = "aSerializedValue";
  public static final byte[] EXAMPLE_VARIABLE_INSTANCE_BYTE = "aSerializedValue".getBytes();

  public static final String EXAMPLE_SPIN_DATA_FORMAT = "aDataFormatId";
  public static final String EXAMPLE_SPIN_ROOT_TYPE = "path.to.a.RootType";


  // execution
  public static final String EXAMPLE_EXECUTION_ID = "anExecutionId";
  public static final boolean EXAMPLE_EXECUTION_IS_ENDED = false;

  // event subscription
  public static final String EXAMPLE_EVENT_SUBSCRIPTION_ID = "anEventSubscriptionId";
  public static final String EXAMPLE_EVENT_SUBSCRIPTION_TYPE = "message";
  public static final String EXAMPLE_EVENT_SUBSCRIPTION_NAME = "anEvent";
  public static final String EXAMPLE_EVENT_SUBSCRIPTION_CREATION_DATE = "2013-01-23T13:59:43";

  // process definition
  public static final String EXAMPLE_PROCESS_DEFINITION_ID = "aProcDefId";
  public static final String NON_EXISTING_PROCESS_DEFINITION_ID = "aNonExistingProcDefId";
  public static final String EXAMPLE_PROCESS_DEFINITION_NAME = "aName";
  public static final String EXAMPLE_PROCESS_DEFINITION_NAME_LIKE = "aNameLike";
  public static final String EXAMPLE_PROCESS_DEFINITION_KEY = "aKey";
  public static final String NON_EXISTING_PROCESS_DEFINITION_KEY = "aNonExistingKey";
  public static final String EXAMPLE_PROCESS_DEFINITION_CATEGORY = "aCategory";
  public static final String EXAMPLE_PROCESS_DEFINITION_DESCRIPTION = "aDescription";
  public static final int EXAMPLE_PROCESS_DEFINITION_VERSION = 42;
  public static final String EXAMPLE_PROCESS_DEFINITION_RESOURCE_NAME = "aResourceName";
  public static final String EXAMPLE_PROCESS_DEFINITION_DIAGRAM_RESOURCE_NAME = "aResourceName.png";
  public static final boolean EXAMPLE_PROCESS_DEFINITION_IS_SUSPENDED = true;

  public static final String ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID = "aProcessDefinitionId:2";

  public static final String EXAMPLE_ACTIVITY_ID = "anActivity";
  public static final String ANOTHER_EXAMPLE_ACTIVITY_ID = "anotherActivity";
  public static final String EXAMPLE_ACTIVITY_NAME = "anActivityName";
  public static final String EXAMPLE_ACTIVITY_TYPE = "anActivityType";
  public static final String EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION = "2013-04-23T13:42:43";

  // deployment
  public static final String NON_EXISTING_DEPLOYMENT_ID = "aNonExistingDeploymentId";
  public static final String EXAMPLE_DEPLOYMENT_NAME = "aName";
  public static final String EXAMPLE_DEPLOYMENT_NAME_LIKE = "aNameLike";
  public static final String EXAMPLE_DEPLOYMENT_TIME = "2013-01-23T13:59:43";
  public static final String NON_EXISTING_DEPLOYMENT_TIME = "2013-04-23T13:42:43";

  // deployment resources
  public static final String EXAMPLE_DEPLOYMENT_RESOURCE_ID = "aDeploymentResourceId";
  public static final String NON_EXISTING_DEPLOYMENT_RESOURCE_ID = "aNonExistingDeploymentResourceId";
  public static final String EXAMPLE_DEPLOYMENT_RESOURCE_NAME = "aDeploymentResourceName";

  // statistics
  public static final int EXAMPLE_FAILED_JOBS = 42;
  public static final int EXAMPLE_INSTANCES = 123;

  public static final long EXAMPLE_INSTANCES_LONG = 123;
  public static final long EXAMPLE_FINISHED_LONG = 124;
  public static final long EXAMPLE_CANCELED_LONG = 125;
  public static final long EXAMPLE_COMPLETE_SCOPE_LONG = 126;

  public static final long ANOTHER_EXAMPLE_INSTANCES_LONG = 127;
  public static final long ANOTHER_EXAMPLE_FINISHED_LONG = 128;
  public static final long ANOTHER_EXAMPLE_CANCELED_LONG = 129;
  public static final long ANOTHER_EXAMPLE_COMPLETE_SCOPE_LONG = 130;

  public static final int ANOTHER_EXAMPLE_FAILED_JOBS = 43;
  public static final int ANOTHER_EXAMPLE_INSTANCES = 124;

  public static final String ANOTHER_EXAMPLE_INCIDENT_TYPE = "anotherIncidentType";
  public static final int ANOTHER_EXAMPLE_INCIDENT_COUNT = 2;

  // user & groups
  public static final String EXAMPLE_GROUP_ID = "groupId1";
  public static final String EXAMPLE_GROUP_ID2 = "groupId2";
  public static final String EXAMPLE_GROUP_NAME = "group1";
  public static final String EXAMPLE_GROUP_TYPE = "organizational-unit";
  public static final String EXAMPLE_GROUP_NAME_UPDATE = "group1Update";

  public static final String EXAMPLE_USER_ID = "userId";
  public static final String EXAMPLE_USER_ID2 = "userId2";
  public static final String EXAMPLE_USER_FIRST_NAME = "firstName";
  public static final String EXAMPLE_USER_LAST_NAME = "lastName";
  public static final String EXAMPLE_USER_EMAIL = "test@example.org";
  public static final String EXAMPLE_USER_PASSWORD = "s3cret";

  public static final String EXAMPLE_USER_FIRST_NAME_UPDATE = "firstNameUpdate";
  public static final String EXAMPLE_USER_LAST_NAME_UPDATE = "lastNameUpdate";
  public static final String EXAMPLE_USER_EMAIL_UPDATE = "testUpdate@example.org";

  // Job Definitions
  public static final String EXAMPLE_JOB_DEFINITION_ID = "aJobDefId";
  public static final String NON_EXISTING_JOB_DEFINITION_ID = "aNonExistingJobDefId";
  public static final String EXAMPLE_JOB_TYPE = "aJobType";
  public static final String EXAMPLE_JOB_CONFIG = "aJobConfig";
  public static final boolean EXAMPLE_JOB_DEFINITION_IS_SUSPENDED = true;
  public static final String EXAMPLE_JOB_DEFINITION_DELAYED_EXECUTION = "2013-04-23T13:42:43";

  // Jobs
  public static final String EXAMPLE_JOB_ID = "aJobId";
  public static final String NON_EXISTING_JOB_ID = "aNonExistingJobId";
  public static final int EXAMPLE_NEGATIVE_JOB_RETRIES = -3;
  public static final int EXAMPLE_JOB_RETRIES = 3;
  public static final String EXAMPLE_JOB_NO_EXCEPTION_MESSAGE = "";
  public static final String EXAMPLE_EXCEPTION_MESSAGE = "aExceptionMessage";
  public static final String EXAMPLE_EMPTY_JOB_ID = "";
  public static final String EXAMPLE_DUE_DATE =  "2013-04-23T13:42:43";
  public static final Boolean EXAMPLE_WITH_RETRIES_LEFT = true;
  public static final Boolean EXAMPLE_EXECUTABLE = true;
  public static final Boolean EXAMPLE_TIMERS = true;
  public static final Boolean EXAMPLE_MESSAGES = true;
  public static final Boolean EXAMPLE_WITH_EXCEPTION = true;
  public static final Boolean EXAMPLE_NO_RETRIES_LEFT = true;
  public static final Boolean EXAMPLE_JOB_IS_SUSPENDED = true;

  public static final String EXAMPLE_RESOURCE_TYPE_NAME = "exampleResource";
  public static final int EXAMPLE_RESOURCE_TYPE_ID = 12345678;
  public static final String EXAMPLE_RESOURCE_TYPE_ID_STRING = "12345678";
  public static final String EXAMPLE_RESOURCE_ID = "exampleResourceId";
  public static final String EXAMPLE_PERMISSION_NAME = "READ";
  public static final Permission[] EXAMPLE_GRANT_PERMISSION_VALUES = new Permission[] { Permissions.NONE, Permissions.READ, Permissions.UPDATE };
  public static final Permission[] EXAMPLE_REVOKE_PERMISSION_VALUES = new Permission[] { Permissions.ALL, Permissions.READ, Permissions.UPDATE };
  public static final String[] EXAMPLE_PERMISSION_VALUES_STRING = new String[] { "READ", "UPDATE" };

  public static final String EXAMPLE_AUTHORIZATION_ID = "someAuthorizationId";
  public static final int EXAMPLE_AUTHORIZATION_TYPE = 0;
  public static final String EXAMPLE_AUTHORIZATION_TYPE_STRING = "0";

  // process applications
  public static final String EXAMPLE_PROCESS_APPLICATION_NAME = "aProcessApplication";
  public static final String EXAMPLE_PROCESS_APPLICATION_CONTEXT_PATH = "http://camunda.org/someContext";

  // Historic Process Instance
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_DELETE_REASON = "aDeleteReason";
  public static final long EXAMPLE_HISTORIC_PROCESS_INSTANCE_DURATION_MILLIS = 2000l;
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_START_TIME = "2013-04-23T13:42:43";
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_END_TIME = "2013-04-23T13:42:43";
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_START_USER_ID = "aStartUserId";
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_START_ACTIVITY_ID = "aStartActivityId";
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_SUPER_PROCESS_INSTANCE_ID = "aSuperProcessInstanceId";
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_SUB_PROCESS_INSTANCE_ID = "aSubProcessInstanceId";
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_CASE_INSTANCE_ID = "aCaseInstanceId";

  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_STARTED_AFTER = "2013-04-23T13:42:43";
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_STARTED_BEFORE = "2013-01-23T13:42:43";
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_FINISHED_AFTER = "2013-01-23T13:42:43";
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_FINISHED_BEFORE = "2013-04-23T13:42:43";

  // Historic Case Instance
  public static final long EXAMPLE_HISTORIC_CASE_INSTANCE_DURATION_MILLIS = 2000l;
  public static final String EXAMPLE_HISTORIC_CASE_INSTANCE_CREATE_TIME = "2013-04-23T13:42:43";
  public static final String EXAMPLE_HISTORIC_CASE_INSTANCE_CLOSE_TIME = "2013-04-23T13:42:43";
  public static final String EXAMPLE_HISTORIC_CASE_INSTANCE_CREATE_USER_ID = "aCreateUserId";
  public static final String EXAMPLE_HISTORIC_CASE_INSTANCE_SUPER_CASE_INSTANCE_ID = "aSuperCaseInstanceId";
  public static final String EXAMPLE_HISTORIC_CASE_INSTANCE_SUB_CASE_INSTANCE_ID = "aSubCaseInstanceId";

  public static final String EXAMPLE_HISTORIC_CASE_INSTANCE_CREATED_AFTER = "2013-04-23T13:42:43";
  public static final String EXAMPLE_HISTORIC_CASE_INSTANCE_CREATED_BEFORE = "2013-01-23T13:42:43";
  public static final String EXAMPLE_HISTORIC_CASE_INSTANCE_CLOSED_AFTER = "2013-01-23T13:42:43";
  public static final String EXAMPLE_HISTORIC_CASE_INSTANCE_CLOSED_BEFORE = "2013-04-23T13:42:43";

  public static final boolean EXAMPLE_HISTORIC_CASE_INSTANCE_IS_ACTIVE = true;
  public static final boolean EXAMPLE_HISTORIC_CASE_INSTANCE_IS_COMPLETED = true;
  public static final boolean EXAMPLE_HISTORIC_CASE_INSTANCE_IS_TERMINATED = true;
  public static final boolean EXAMPLE_HISTORIC_CASE_INSTANCE_IS_CLOSED = true;

  // Historic Activity Instance
  public static final String EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_ID = "aHistoricActivityInstanceId";
  public static final String EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_PARENT_ACTIVITY_INSTANCE_ID = "aHistoricParentActivityInstanceId";
  public static final String EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_CALLED_PROCESS_INSTANCE_ID = "aHistoricCalledProcessInstanceId";
  public static final String EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_START_TIME = "2013-04-23T13:42:43";
  public static final String EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_END_TIME = "2013-04-23T18:42:43";
  public static final long EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_DURATION = 2000l;
  public static final String EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_STARTED_AFTER = "2013-04-23T13:42:43";
  public static final String EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_STARTED_BEFORE = "2013-01-23T13:42:43";
  public static final String EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_FINISHED_AFTER = "2013-01-23T13:42:43";
  public static final String EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_FINISHED_BEFORE = "2013-04-23T13:42:43";
  public static final boolean EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_IS_CANCELED = true;
  public static final boolean EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_IS_COMPLETE_SCOPE = true;

  // Historic Case Activity Instance
  public static final String EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_ID = "aCaseActivityInstanceId";
  public static final String EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_PARENT_CASE_ACTIVITY_INSTANCE_ID = "aParentCaseActivityId";
  public static final String EXAMPLE_HISTORIC_CASE_ACTIVITY_ID = "aCaseActivityId";
  public static final String EXAMPLE_HISTORIC_CASE_ACTIVITY_NAME = "aCaseActivityName";
  public static final String EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_CALLED_PROCESS_INSTANCE_ID = "aCalledProcessInstanceId";
  public static final String EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_CALLED_CASE_INSTANCE_ID = "aCalledCaseInstanceId";
  public static final String EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_CREATE_TIME = "2014-04-23T18:42:42";
  public static final String EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_END_TIME = "2014-04-23T18:42:43";
  public static final long EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_DURATION = 2000l;
  public static final boolean EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_AVAILABLE = true;
  public static final boolean EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_ENABLED = true;
  public static final boolean EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_DISABLED = true;
  public static final boolean EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_ACTIVE = true;
  public static final boolean EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_FAILED = true;
  public static final boolean EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_SUSPENDED = true;
  public static final boolean EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_COMPLETED = true;
  public static final boolean EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_TERMINATED = true;
  public static final boolean EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_UNFINISHED = true;
  public static final boolean EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_FINISHED = true;

  public static final String EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_CREATED_AFTER = "2014-04-23T18:41:42";
  public static final String EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_CREATED_BEFORE = "2014-04-23T18:43:42";
  public static final String EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_ENDED_AFTER = "2014-04-23T18:41:43";
  public static final String EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_ENDED_BEFORE = "2014-04-23T18:43:43";

  // user operation log
  public static final String EXAMPLE_USER_OPERATION_LOG_ID = "userOpLogId";
  public static final String EXAMPLE_USER_OPERATION_ID = "opId";
  public static final String EXAMPLE_USER_OPERATION_TYPE = UserOperationLogEntry.OPERATION_TYPE_CLAIM;
  public static final String EXAMPLE_USER_OPERATION_ENTITY = EntityTypes.TASK;
  public static final String EXAMPLE_USER_OPERATION_PROPERTY = "opProperty";
  public static final String EXAMPLE_USER_OPERATION_ORG_VALUE = "orgValue";
  public static final String EXAMPLE_USER_OPERATION_NEW_VALUE = "newValue";
  public static final String EXAMPLE_USER_OPERATION_TIMESTAMP = "2014-02-20T16:53:37";

  // historic detail
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_ID = "aHistoricVariableUpdateId";
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_PROC_INST_ID = "aProcInst";
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_ACT_INST_ID = "anActInst";
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_EXEC_ID = "anExecutionId";
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_TASK_ID = "aTaskId";
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_TIME = "2014-01-01T00:00:00";
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_NAME = "aVariableName";
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_TYPE_NAME = "String";
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_VALUE_TYPE_NAME = "String";
  public static final int EXAMPLE_HISTORIC_VAR_UPDATE_REVISION = 1;
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_ERROR = "anErrorMessage";

  public static final String EXAMPLE_HISTORIC_FORM_FIELD_ID = "anId";
  public static final String EXAMPLE_HISTORIC_FORM_FIELD_PROC_INST_ID = "aProcInst";
  public static final String EXAMPLE_HISTORIC_FORM_FIELD_ACT_INST_ID = "anActInst";
  public static final String EXAMPLE_HISTORIC_FORM_FIELD_EXEC_ID = "anExecutionId";
  public static final String EXAMPLE_HISTORIC_FORM_FIELD_TASK_ID = "aTaskId";
  public static final String EXAMPLE_HISTORIC_FORM_FIELD_TIME = "2014-01-01T00:00:00";
  public static final String EXAMPLE_HISTORIC_FORM_FIELD_FIELD_ID = "aFormFieldId";
  public static final String EXAMPLE_HISTORIC_FORM_FIELD_VALUE = "aFormFieldValue";

  // historic task instance
  public static final String EXAMPLE_HISTORIC_TASK_INST_ID = "aHistoricTaskInstanceId";
  public static final String EXAMPLE_HISTORIC_TASK_INST_PROC_DEF_ID = "aProcDefId";
  public static final String EXAMPLE_HISTORIC_TASK_INST_PROC_INST_ID = "aProcInstId";
  public static final String EXAMPLE_HISTORIC_TASK_INST_EXEC_ID = "anExecId";
  public static final String EXAMPLE_HISTORIC_TASK_INST_ACT_INST_ID = "anActInstId";
  public static final String EXAMPLE_HISTORIC_TASK_INST_NAME = "aName";
  public static final String EXAMPLE_HISTORIC_TASK_INST_DESCRIPTION = "aDescription";
  public static final String EXAMPLE_HISTORIC_TASK_INST_DELETE_REASON = "aDeleteReason";
  public static final String EXAMPLE_HISTORIC_TASK_INST_OWNER = "anOwner";
  public static final String EXAMPLE_HISTORIC_TASK_INST_ASSIGNEE = "anAssignee";
  public static final String EXAMPLE_HISTORIC_TASK_INST_START_TIME = "2014-01-01T00:00:00";
  public static final String EXAMPLE_HISTORIC_TASK_INST_END_TIME = "2014-01-01T00:00:00";
  public static final Long EXAMPLE_HISTORIC_TASK_INST_DURATION = 5000L;
  public static final String EXAMPLE_HISTORIC_TASK_INST_DEF_KEY = "aTaskDefinitionKey";
  public static final int EXAMPLE_HISTORIC_TASK_INST_PRIORITY = 60;
  public static final String EXAMPLE_HISTORIC_TASK_INST_DUE_DATE = "2014-01-01T00:00:00";
  public static final String EXAMPLE_HISTORIC_TASK_INST_FOLLOW_UP_DATE = "2014-01-01T00:00:00";
  public static final String EXAMPLE_HISTORIC_TASK_INST_PARENT_TASK_ID = "aParentTaskId";
  public static final String EXAMPLE_HISTORIC_TASK_INST_CASE_DEF_ID = "aCaseDefinitionId";
  public static final String EXAMPLE_HISTORIC_TASK_INST_CASE_INST_ID = "aCaseInstanceId";
  public static final String EXAMPLE_HISTORIC_TASK_INST_CASE_EXEC_ID = "aCaseExecutionId";

  // Incident
  public static final String EXAMPLE_INCIDENT_ID = "anIncidentId";
  public static final String EXAMPLE_INCIDENT_TIMESTAMP = "2014-01-01T00:00:00";
  public static final String EXAMPLE_INCIDENT_TYPE = "anIncidentType";
  public static final String EXAMPLE_INCIDENT_EXECUTION_ID = "anExecutionId";
  public static final String EXAMPLE_INCIDENT_ACTIVITY_ID = "anActivityId";
  public static final String EXAMPLE_INCIDENT_PROC_INST_ID = "aProcInstId";
  public static final String EXAMPLE_INCIDENT_PROC_DEF_ID = "aProcDefId";
  public static final String EXAMPLE_INCIDENT_CAUSE_INCIDENT_ID = "aCauseIncidentId";
  public static final String EXAMPLE_INCIDENT_ROOT_CAUSE_INCIDENT_ID = "aRootCauseIncidentId";
  public static final String EXAMPLE_INCIDENT_CONFIGURATION = "aConfiguration";
  public static final String EXAMPLE_INCIDENT_MESSAGE = "anIncidentMessage";

  public static final int EXAMPLE_INCIDENT_COUNT = 1;

  // Historic Incident
  public static final String EXAMPLE_HIST_INCIDENT_ID = "anIncidentId";
  public static final String EXAMPLE_HIST_INCIDENT_CREATE_TIME = "2014-01-01T00:00:00";
  public static final String EXAMPLE_HIST_INCIDENT_END_TIME = "2014-01-01T00:00:00";
  public static final String EXAMPLE_HIST_INCIDENT_TYPE = "anIncidentType";
  public static final String EXAMPLE_HIST_INCIDENT_EXECUTION_ID = "anExecutionId";
  public static final String EXAMPLE_HIST_INCIDENT_ACTIVITY_ID = "anActivityId";
  public static final String EXAMPLE_HIST_INCIDENT_PROC_INST_ID = "aProcInstId";
  public static final String EXAMPLE_HIST_INCIDENT_PROC_DEF_ID = "aProcDefId";
  public static final String EXAMPLE_HIST_INCIDENT_CAUSE_INCIDENT_ID = "aCauseIncidentId";
  public static final String EXAMPLE_HIST_INCIDENT_ROOT_CAUSE_INCIDENT_ID = "aRootCauseIncidentId";
  public static final String EXAMPLE_HIST_INCIDENT_CONFIGURATION = "aConfiguration";
  public static final String EXAMPLE_HIST_INCIDENT_MESSAGE = "anIncidentMessage";
  public static final boolean EXAMPLE_HIST_INCIDENT_STATE_OPEN = false;
  public static final boolean EXAMPLE_HIST_INCIDENT_STATE_DELETED = false;
  public static final boolean EXAMPLE_HIST_INCIDENT_STATE_RESOLVED = true;

  // case definition
  public static final String EXAMPLE_CASE_DEFINITION_ID = "aCaseDefnitionId";
  public static final String EXAMPLE_CASE_DEFINITION_KEY = "aCaseDefinitionKey";
  public static final int EXAMPLE_CASE_DEFINITION_VERSION = 1;
  public static final String EXAMPLE_CASE_DEFINITION_CATEGORY = "aCaseDefinitionCategory";
  public static final String EXAMPLE_CASE_DEFINITION_NAME = "aCaseDefinitionName";
  public static final String EXAMPLE_CASE_DEFINITION_NAME_LIKE = "aCaseDefinitionNameLike";
  public static final String EXAMPLE_CASE_DEFINITION_RESOURCE_NAME = "aCaseDefinitionResourceName";

  // case instance
  public static final String EXAMPLE_CASE_INSTANCE_ID = "aCaseInstId";
  public static final String EXAMPLE_CASE_INSTANCE_BUSINESS_KEY = "aBusinessKey";
  public static final String EXAMPLE_CASE_INSTANCE_BUSINESS_KEY_LIKE = "aBusinessKeyLike";
  public static final String EXAMPLE_CASE_INSTANCE_CASE_DEFINITION_ID = "aCaseDefinitionId";
  public static final boolean EXAMPLE_CASE_INSTANCE_IS_ACTIVE = true;
  public static final boolean EXAMPLE_CASE_INSTANCE_IS_COMPLETED = true;
  public static final boolean EXAMPLE_CASE_INSTANCE_IS_TERMINATED = true;

  // case execution
  public static final String EXAMPLE_CASE_EXECUTION_ID = "aCaseExecutionId";
  public static final String ANOTHER_EXAMPLE_CASE_EXECUTION_ID = "anotherCaseExecutionId";
  public static final String EXAMPLE_CASE_EXECUTION_CASE_INSTANCE_ID = "aCaseInstanceId";
  public static final String EXAMPLE_CASE_EXECUTION_PARENT_ID = "aParentId";
  public static final String EXAMPLE_CASE_EXECUTION_CASE_DEFINITION_ID = "aCaseDefinitionId";
  public static final String EXAMPLE_CASE_EXECUTION_ACTIVITY_ID = "anActivityId";
  public static final String EXAMPLE_CASE_EXECUTION_ACTIVITY_NAME = "anActivityName";
  public static final boolean EXAMPLE_CASE_EXECUTION_IS_ENABLED = true;
  public static final boolean EXAMPLE_CASE_EXECUTION_IS_ACTIVE = true;
  public static final boolean EXAMPLE_CASE_EXECUTION_IS_DISABLED = true;

  // filter
  public static final String EXAMPLE_FILTER_ID = "aFilterId";
  public static final String ANOTHER_EXAMPLE_FILTER_ID = "anotherFilterId";
  public static final String EXAMPLE_FILTER_RESOURCE_TYPE = EntityTypes.TASK;
  public static final String EXAMPLE_FILTER_NAME = "aFilterName";
  public static final String EXAMPLE_FILTER_OWNER = "aFilterOwner";
  public static final Query EXAMPLE_FILTER_QUERY = new TaskQueryImpl().taskName("test").processVariableValueEquals("foo", "bar").caseInstanceVariableValueEquals("foo", "bar").taskVariableValueEquals("foo", "bar");
  public static final TaskQueryDto EXAMPLE_FILTER_QUERY_DTO = TaskQueryDto.fromQuery(EXAMPLE_FILTER_QUERY);
  public static final Map<String, Object> EXAMPLE_FILTER_PROPERTIES = Collections.singletonMap("color", (Object) "#112233");

  // tasks
  public static Task createMockTask() {
    return mockTask().build();
  }

  public static MockTaskBuilder mockTask() {
    return new MockTaskBuilder()
      .id(EXAMPLE_TASK_ID).name(EXAMPLE_TASK_NAME)
      .assignee(EXAMPLE_TASK_ASSIGNEE_NAME)
      .createTime(DateTimeUtil.parseDate(EXAMPLE_TASK_CREATE_TIME))
      .dueDate(DateTimeUtil.parseDate(EXAMPLE_TASK_DUE_DATE))
      .followUpDate(DateTimeUtil.parseDate(EXAMPLE_FOLLOW_UP_DATE))
      .delegationState(EXAMPLE_TASK_DELEGATION_STATE).description(EXAMPLE_TASK_DESCRIPTION)
      .executionId(EXAMPLE_TASK_EXECUTION_ID).owner(EXAMPLE_TASK_OWNER)
      .parentTaskId(EXAMPLE_TASK_PARENT_TASK_ID)
      .priority(EXAMPLE_TASK_PRIORITY)
      .processDefinitionId(EXAMPLE_PROCESS_DEFINITION_ID)
      .processInstanceId(EXAMPLE_PROCESS_INSTANCE_ID)
      .taskDefinitionKey(EXAMPLE_TASK_DEFINITION_KEY)
      .caseDefinitionId(EXAMPLE_CASE_DEFINITION_ID)
      .caseInstanceId(EXAMPLE_CASE_INSTANCE_ID)
      .caseExecutionId(EXAMPLE_CASE_EXECUTION_ID)
      .formKey(EXAMPLE_FORM_KEY);
  }

  public static List<Task> createMockTasks() {
    List<Task> mocks = new ArrayList<Task>();
    mocks.add(createMockTask());
    return mocks;
  }

  public static TaskFormData createMockTaskFormData() {
    FormProperty mockFormProperty = mock(FormProperty.class);
    when(mockFormProperty.getId()).thenReturn(EXAMPLE_FORM_PROPERTY_ID);
    when(mockFormProperty.getName()).thenReturn(EXAMPLE_FORM_PROPERTY_NAME);
    when(mockFormProperty.getValue()).thenReturn(EXAMPLE_FORM_PROPERTY_VALUE);
    when(mockFormProperty.isReadable()).thenReturn(EXAMPLE_FORM_PROPERTY_READABLE);
    when(mockFormProperty.isWritable()).thenReturn(EXAMPLE_FORM_PROPERTY_WRITABLE);
    when(mockFormProperty.isRequired()).thenReturn(EXAMPLE_FORM_PROPERTY_REQUIRED);

    FormType mockFormType = mock(FormType.class);
    when(mockFormType.getName()).thenReturn(EXAMPLE_FORM_PROPERTY_TYPE_NAME);
    when(mockFormProperty.getType()).thenReturn(mockFormType);

    TaskFormData mockFormData = mock(TaskFormData.class);
    when(mockFormData.getFormKey()).thenReturn(EXAMPLE_FORM_KEY);
    when(mockFormData.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);

    List<FormProperty> mockFormProperties = new ArrayList<FormProperty>();
    mockFormProperties.add(mockFormProperty);
    when(mockFormData.getFormProperties()).thenReturn(mockFormProperties);
    return mockFormData;
  }

  public static TaskFormData createMockTaskFormDataUsingFormFieldsWithoutFormKey() {
    FormField mockFormField = mock(FormField.class);
    when(mockFormField.getId()).thenReturn(EXAMPLE_FORM_PROPERTY_ID);
    when(mockFormField.getLabel()).thenReturn(EXAMPLE_FORM_PROPERTY_NAME);
    when(mockFormField.getDefaultValue()).thenReturn(EXAMPLE_FORM_PROPERTY_VALUE);

    FormType mockFormType = mock(FormType.class);
    when(mockFormType.getName()).thenReturn(EXAMPLE_FORM_PROPERTY_TYPE_NAME);
    when(mockFormField.getType()).thenReturn(mockFormType);

    TaskFormData mockFormData = mock(TaskFormData.class);
    when(mockFormData.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);

    List<FormField> mockFormFields = new ArrayList<FormField>();
    mockFormFields.add(mockFormField);
    when(mockFormData.getFormFields()).thenReturn(mockFormFields);
    return mockFormData;
  }

  // task comment
  public static Comment createMockTaskComment() {
    Comment mockComment = mock(Comment.class);
    when(mockComment.getId()).thenReturn(EXAMPLE_TASK_COMMENT_ID);
    when(mockComment.getTaskId()).thenReturn(EXAMPLE_TASK_ID);
    when(mockComment.getUserId()).thenReturn(EXAMPLE_USER_ID);
    when(mockComment.getTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_TASK_COMMENT_TIME));
    when(mockComment.getFullMessage()).thenReturn(EXAMPLE_TASK_COMMENT_FULL_MESSAGE);
    return mockComment;
  }

  public static List<Comment> createMockTaskComments() {
    List<Comment> mocks = new ArrayList<Comment>();
    mocks.add(createMockTaskComment());
    return mocks;
  }

  // task attachment
  public static Attachment createMockTaskAttachment() {
    Attachment mockAttachment = mock(Attachment.class);
    when(mockAttachment.getId()).thenReturn(EXAMPLE_TASK_ATTACHMENT_ID);
    when(mockAttachment.getName()).thenReturn(EXAMPLE_TASK_ATTACHMENT_NAME);
    when(mockAttachment.getDescription()).thenReturn(EXAMPLE_TASK_ATTACHMENT_DESCRIPTION);
    when(mockAttachment.getType()).thenReturn(EXAMPLE_TASK_ATTACHMENT_TYPE);
    when(mockAttachment.getUrl()).thenReturn(EXAMPLE_TASK_ATTACHMENT_URL);
    when(mockAttachment.getTaskId()).thenReturn(EXAMPLE_TASK_ID);
    when(mockAttachment.getProcessInstanceId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);

    return mockAttachment;
  }

  public static List<Attachment> createMockTaskAttachments() {
    List<Attachment> mocks = new ArrayList<Attachment>();
    mocks.add(createMockTaskAttachment());
    return mocks;
  }

  // form data
  public static StartFormData createMockStartFormData(ProcessDefinition definition) {
    FormProperty mockFormProperty = mock(FormProperty.class);
    when(mockFormProperty.getId()).thenReturn(EXAMPLE_FORM_PROPERTY_ID);
    when(mockFormProperty.getName()).thenReturn(EXAMPLE_FORM_PROPERTY_NAME);
    when(mockFormProperty.getValue()).thenReturn(EXAMPLE_FORM_PROPERTY_VALUE);
    when(mockFormProperty.isReadable()).thenReturn(EXAMPLE_FORM_PROPERTY_READABLE);
    when(mockFormProperty.isWritable()).thenReturn(EXAMPLE_FORM_PROPERTY_WRITABLE);
    when(mockFormProperty.isRequired()).thenReturn(EXAMPLE_FORM_PROPERTY_REQUIRED);

    FormType mockFormType = mock(FormType.class);
    when(mockFormType.getName()).thenReturn(EXAMPLE_FORM_PROPERTY_TYPE_NAME);
    when(mockFormProperty.getType()).thenReturn(mockFormType);

    StartFormData mockFormData = mock(StartFormData.class);
    when(mockFormData.getFormKey()).thenReturn(EXAMPLE_FORM_KEY);
    when(mockFormData.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    when(mockFormData.getProcessDefinition()).thenReturn(definition);

    List<FormProperty> mockFormProperties = new ArrayList<FormProperty>();
    mockFormProperties.add(mockFormProperty);
    when(mockFormData.getFormProperties()).thenReturn(mockFormProperties);
    return mockFormData;
  }

  public static StartFormData createMockStartFormDataUsingFormFieldsWithoutFormKey(ProcessDefinition definition) {
    FormField mockFormField = mock(FormField.class);
    when(mockFormField.getId()).thenReturn(EXAMPLE_FORM_PROPERTY_ID);
    when(mockFormField.getLabel()).thenReturn(EXAMPLE_FORM_PROPERTY_NAME);
    when(mockFormField.getDefaultValue()).thenReturn(EXAMPLE_FORM_PROPERTY_VALUE);

    FormType mockFormType = mock(FormType.class);
    when(mockFormType.getName()).thenReturn(EXAMPLE_FORM_PROPERTY_TYPE_NAME);
    when(mockFormField.getType()).thenReturn(mockFormType);

    StartFormData mockFormData = mock(StartFormData.class);
    when(mockFormData.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    when(mockFormData.getProcessDefinition()).thenReturn(definition);

    List<FormField> mockFormFields = new ArrayList<FormField>();
    mockFormFields.add(mockFormField);
    when(mockFormData.getFormFields()).thenReturn(mockFormFields);

    return mockFormData;
  }

  public static ProcessInstance createMockInstance() {
    ProcessInstance mock = mock(ProcessInstance.class);

    when(mock.getId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(mock.getBusinessKey()).thenReturn(EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY);
    when(mock.getCaseInstanceId()).thenReturn(EXAMPLE_CASE_INSTANCE_ID);
    when(mock.getProcessDefinitionId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
    when(mock.getProcessInstanceId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(mock.isSuspended()).thenReturn(EXAMPLE_PROCESS_INSTANCE_IS_SUSPENDED);
    when(mock.isEnded()).thenReturn(EXAMPLE_PROCESS_INSTANCE_IS_ENDED);

    return mock;
  }

  public static VariableInstance createMockVariableInstance() {
    return mockVariableInstance().build();
  }

  public static MockVariableInstanceBuilder mockVariableInstance() {
    return new MockVariableInstanceBuilder()
      .id(EXAMPLE_VARIABLE_INSTANCE_ID)
      .name(EXAMPLE_VARIABLE_INSTANCE_NAME)
      .typedValue(EXAMPLE_PRIMITIVE_VARIABLE_VALUE)
      .processInstanceId(EXAMPLE_VARIABLE_INSTANCE_PROC_INST_ID)
      .executionId(EXAMPLE_VARIABLE_INSTANCE_EXECUTION_ID)
      .caseInstanceId(EXAMPLE_VARIABLE_INSTANCE_CASE_INST_ID)
      .caseExecutionId(EXAMPLE_VARIABLE_INSTANCE_CASE_EXECUTION_ID)
      .taskId(EXAMPLE_VARIABLE_INSTANCE_TASK_ID)
      .activityInstanceId(EXAMPLE_VARIABLE_INSTANCE_ACTIVITY_INSTANCE_ID)
      .errorMessage(null);
  }

  public static VariableInstance createMockVariableInstance(TypedValue value) {
    return mockVariableInstance().typedValue(value).build();
  }

  public static Execution createMockExecution() {
    Execution mock = mock(Execution.class);

    when(mock.getId()).thenReturn(EXAMPLE_EXECUTION_ID);
    when(mock.getProcessInstanceId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(mock.isEnded()).thenReturn(EXAMPLE_EXECUTION_IS_ENDED);

    return mock;
  }

  public static EventSubscription createMockEventSubscription() {
    EventSubscription mock = mock(EventSubscription.class);

    when(mock.getId()).thenReturn(EXAMPLE_EVENT_SUBSCRIPTION_ID);
    when(mock.getEventType()).thenReturn(EXAMPLE_EVENT_SUBSCRIPTION_TYPE);
    when(mock.getEventName()).thenReturn(EXAMPLE_EVENT_SUBSCRIPTION_NAME);
    when(mock.getExecutionId()).thenReturn(EXAMPLE_EXECUTION_ID);
    when(mock.getProcessInstanceId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(mock.getActivityId()).thenReturn(EXAMPLE_ACTIVITY_ID);
    when(mock.getCreated()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_EVENT_SUBSCRIPTION_CREATION_DATE));

    return mock;
  }

  // statistics
  public static List<ProcessDefinitionStatistics> createMockProcessDefinitionStatistics() {
    ProcessDefinitionStatistics statistics = mock(ProcessDefinitionStatistics.class);
    when(statistics.getFailedJobs()).thenReturn(EXAMPLE_FAILED_JOBS);
    when(statistics.getInstances()).thenReturn(EXAMPLE_INSTANCES);
    when(statistics.getId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
    when(statistics.getName()).thenReturn(EXAMPLE_PROCESS_DEFINITION_NAME);
    when(statistics.getKey()).thenReturn(EXAMPLE_PROCESS_DEFINITION_KEY);

    IncidentStatistics incidentStaticits = mock(IncidentStatistics.class);
    when(incidentStaticits.getIncidentType()).thenReturn(EXAMPLE_INCIDENT_TYPE);
    when(incidentStaticits.getIncidentCount()).thenReturn(EXAMPLE_INCIDENT_COUNT);

    List<IncidentStatistics> exampleIncidentList = new ArrayList<IncidentStatistics>();
    exampleIncidentList.add(incidentStaticits);
    when(statistics.getIncidentStatistics()).thenReturn(exampleIncidentList);

    ProcessDefinitionStatistics anotherStatistics = mock(ProcessDefinitionStatistics.class);
    when(anotherStatistics.getFailedJobs()).thenReturn(ANOTHER_EXAMPLE_FAILED_JOBS);
    when(anotherStatistics.getInstances()).thenReturn(ANOTHER_EXAMPLE_INSTANCES);
    when(anotherStatistics.getId()).thenReturn(ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID);
    when(anotherStatistics.getName()).thenReturn(EXAMPLE_PROCESS_DEFINITION_NAME);
    when(anotherStatistics.getKey()).thenReturn(EXAMPLE_PROCESS_DEFINITION_KEY);

    IncidentStatistics anotherIncidentStaticits = mock(IncidentStatistics.class);
    when(anotherIncidentStaticits.getIncidentType()).thenReturn(ANOTHER_EXAMPLE_INCIDENT_TYPE);
    when(anotherIncidentStaticits.getIncidentCount()).thenReturn(ANOTHER_EXAMPLE_INCIDENT_COUNT);

    List<IncidentStatistics> anotherExampleIncidentList = new ArrayList<IncidentStatistics>();
    anotherExampleIncidentList.add(anotherIncidentStaticits);
    when(anotherStatistics.getIncidentStatistics()).thenReturn(anotherExampleIncidentList);

    List<ProcessDefinitionStatistics> processDefinitionResults = new ArrayList<ProcessDefinitionStatistics>();
    processDefinitionResults.add(statistics);
    processDefinitionResults.add(anotherStatistics);

    return processDefinitionResults;
  }

  public static List<ActivityStatistics> createMockActivityStatistics() {
    ActivityStatistics statistics = mock(ActivityStatistics.class);
    when(statistics.getFailedJobs()).thenReturn(EXAMPLE_FAILED_JOBS);
    when(statistics.getInstances()).thenReturn(EXAMPLE_INSTANCES);
    when(statistics.getId()).thenReturn(EXAMPLE_ACTIVITY_ID);

    IncidentStatistics incidentStaticits = mock(IncidentStatistics.class);
    when(incidentStaticits.getIncidentType()).thenReturn(EXAMPLE_INCIDENT_TYPE);
    when(incidentStaticits.getIncidentCount()).thenReturn(EXAMPLE_INCIDENT_COUNT);

    List<IncidentStatistics> exampleIncidentList = new ArrayList<IncidentStatistics>();
    exampleIncidentList.add(incidentStaticits);
    when(statistics.getIncidentStatistics()).thenReturn(exampleIncidentList);

    ActivityStatistics anotherStatistics = mock(ActivityStatistics.class);
    when(anotherStatistics.getFailedJobs()).thenReturn(ANOTHER_EXAMPLE_FAILED_JOBS);
    when(anotherStatistics.getInstances()).thenReturn(ANOTHER_EXAMPLE_INSTANCES);
    when(anotherStatistics.getId()).thenReturn(ANOTHER_EXAMPLE_ACTIVITY_ID);

    IncidentStatistics anotherIncidentStaticits = mock(IncidentStatistics.class);
    when(anotherIncidentStaticits.getIncidentType()).thenReturn(ANOTHER_EXAMPLE_INCIDENT_TYPE);
    when(anotherIncidentStaticits.getIncidentCount()).thenReturn(ANOTHER_EXAMPLE_INCIDENT_COUNT);

    List<IncidentStatistics> anotherExampleIncidentList = new ArrayList<IncidentStatistics>();
    anotherExampleIncidentList.add(anotherIncidentStaticits);
    when(anotherStatistics.getIncidentStatistics()).thenReturn(anotherExampleIncidentList);

    List<ActivityStatistics> activityResults = new ArrayList<ActivityStatistics>();
    activityResults.add(statistics);
    activityResults.add(anotherStatistics);

    return activityResults;
  }

  // process definition
  public static List<ProcessDefinition> createMockDefinitions() {
    List<ProcessDefinition> mocks = new ArrayList<ProcessDefinition>();
    mocks.add(createMockDefinition());
    return mocks;
  }

  public static ProcessDefinition createMockDefinition() {
    MockDefinitionBuilder builder = new MockDefinitionBuilder();
    ProcessDefinition mockDefinition = builder.id(EXAMPLE_PROCESS_DEFINITION_ID).category(EXAMPLE_PROCESS_DEFINITION_CATEGORY)
        .name(EXAMPLE_PROCESS_DEFINITION_NAME).key(EXAMPLE_PROCESS_DEFINITION_KEY).description(EXAMPLE_PROCESS_DEFINITION_DESCRIPTION)
        .version(EXAMPLE_PROCESS_DEFINITION_VERSION).resource(EXAMPLE_PROCESS_DEFINITION_RESOURCE_NAME).deploymentId(EXAMPLE_DEPLOYMENT_ID)
        .diagram(EXAMPLE_PROCESS_DEFINITION_DIAGRAM_RESOURCE_NAME).suspended(EXAMPLE_PROCESS_DEFINITION_IS_SUSPENDED).build();

    return mockDefinition;
  }

  // deployments
  public static List<Deployment> createMockDeployments() {
    List<Deployment> mocks = new ArrayList<Deployment>();
    mocks.add(createMockDeployment());
    return mocks;
  }

  public static Deployment createMockDeployment() {
    Deployment mockDeployment = mock(Deployment.class);
    when(mockDeployment.getId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    when(mockDeployment.getName()).thenReturn(EXAMPLE_DEPLOYMENT_NAME);
    when(mockDeployment.getDeploymentTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_DEPLOYMENT_TIME));

    return mockDeployment;
  }

  // deployment resources
  public static List<Resource> createMockDeploymentResources() {
    List<Resource> mocks = new ArrayList<Resource>();
    mocks.add(createMockDeploymentResource());
    return mocks;
  }

  public static Resource createMockDeploymentResource() {
    Resource mockResource = mock(ResourceEntity.class);
    when(mockResource.getId()).thenReturn(EXAMPLE_DEPLOYMENT_RESOURCE_ID);
    when(mockResource.getName()).thenReturn(EXAMPLE_DEPLOYMENT_RESOURCE_NAME);
    when(mockResource.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);

    return mockResource;
  }

  // user & groups

  public static Group createMockGroup() {
    Group mockGroup = mock(Group.class);
    when(mockGroup.getId()).thenReturn(EXAMPLE_GROUP_ID);
    when(mockGroup.getName()).thenReturn(EXAMPLE_GROUP_NAME);
    when(mockGroup.getType()).thenReturn(EXAMPLE_GROUP_TYPE);

    return mockGroup;
  }

  public static Group createMockGroupUpdate() {
    Group mockGroup = mock(Group.class);
    when(mockGroup.getId()).thenReturn(EXAMPLE_GROUP_ID);
    when(mockGroup.getName()).thenReturn(EXAMPLE_GROUP_NAME_UPDATE);

    return mockGroup;
  }

  public static List<Group> createMockGroups() {
    List<Group> mockGroups = new ArrayList<Group>();
    mockGroups.add(createMockGroup());
    return mockGroups;
  }

  public static User createMockUser() {
    User mockUser = mock(User.class);
    when(mockUser.getId()).thenReturn(EXAMPLE_USER_ID);
    when(mockUser.getFirstName()).thenReturn(EXAMPLE_USER_FIRST_NAME);
    when(mockUser.getLastName()).thenReturn(EXAMPLE_USER_LAST_NAME);
    when(mockUser.getEmail()).thenReturn(EXAMPLE_USER_EMAIL);
    when(mockUser.getPassword()).thenReturn(EXAMPLE_USER_PASSWORD);
    return mockUser;
  }

  public static Authentication createMockAuthentication() {
    Authentication mockAuthentication = mock(Authentication.class);

    when(mockAuthentication.getUserId()).thenReturn(EXAMPLE_USER_ID);

    return mockAuthentication;
  }

  // jobs
  public static Job createMockJob() {
    Job mock = new MockJobBuilder()
      .id(EXAMPLE_JOB_ID)
      .processInstanceId(EXAMPLE_PROCESS_INSTANCE_ID)
      .executionId(EXAMPLE_EXECUTION_ID)
      .processDefinitionId(EXAMPLE_PROCESS_DEFINITION_ID)
      .processDefinitionKey(EXAMPLE_PROCESS_DEFINITION_KEY)
      .retries(EXAMPLE_JOB_RETRIES)
      .exceptionMessage(EXAMPLE_JOB_NO_EXCEPTION_MESSAGE)
      .dueDate(DateTimeUtil.parseDate(EXAMPLE_DUE_DATE))
      .suspended(EXAMPLE_JOB_IS_SUSPENDED)
      .build();
    return mock;
  }

  public static List<Job> createMockJobs() {
    List<Job> mockList = new ArrayList<Job>();
    mockList.add(createMockJob());
    return mockList;
  }

  public static List<Job> createMockEmptyJobList() {
    List<Job> mockList = new ArrayList<Job>();
    return mockList;
  }

  public static User createMockUserUpdate() {
    User mockUser = mock(User.class);
    when(mockUser.getId()).thenReturn(EXAMPLE_USER_ID);
    when(mockUser.getFirstName()).thenReturn(EXAMPLE_USER_FIRST_NAME_UPDATE);
    when(mockUser.getLastName()).thenReturn(EXAMPLE_USER_LAST_NAME_UPDATE);
    when(mockUser.getEmail()).thenReturn(EXAMPLE_USER_EMAIL_UPDATE);
    when(mockUser.getPassword()).thenReturn(EXAMPLE_USER_PASSWORD);
    return mockUser;
  }

  public static List<User> createMockUsers() {
    ArrayList<User> list = new ArrayList<User>();
    list.add(createMockUser());
    return list;
  }

  public static Authorization createMockGlobalAuthorization() {
    Authorization mockAuthorization = mock(Authorization.class);

    when(mockAuthorization.getId()).thenReturn(EXAMPLE_AUTHORIZATION_ID);
    when(mockAuthorization.getAuthorizationType()).thenReturn(Authorization.AUTH_TYPE_GLOBAL);
    when(mockAuthorization.getUserId()).thenReturn(Authorization.ANY);

    when(mockAuthorization.getResourceType()).thenReturn(EXAMPLE_RESOURCE_TYPE_ID);
    when(mockAuthorization.getResourceId()).thenReturn(EXAMPLE_RESOURCE_ID);
    when(mockAuthorization.getPermissions(Permissions.values())).thenReturn(EXAMPLE_GRANT_PERMISSION_VALUES);

    return mockAuthorization;
  }

  public static Authorization createMockGrantAuthorization() {
    Authorization mockAuthorization = mock(Authorization.class);

    when(mockAuthorization.getId()).thenReturn(EXAMPLE_AUTHORIZATION_ID);
    when(mockAuthorization.getAuthorizationType()).thenReturn(Authorization.AUTH_TYPE_GRANT);
    when(mockAuthorization.getUserId()).thenReturn(EXAMPLE_USER_ID);

    when(mockAuthorization.getResourceType()).thenReturn(EXAMPLE_RESOURCE_TYPE_ID);
    when(mockAuthorization.getResourceId()).thenReturn(EXAMPLE_RESOURCE_ID);
    when(mockAuthorization.getPermissions(Permissions.values())).thenReturn(EXAMPLE_GRANT_PERMISSION_VALUES);

    return mockAuthorization;
  }

  public static Authorization createMockRevokeAuthorization() {
    Authorization mockAuthorization = mock(Authorization.class);

    when(mockAuthorization.getId()).thenReturn(EXAMPLE_AUTHORIZATION_ID);
    when(mockAuthorization.getAuthorizationType()).thenReturn(Authorization.AUTH_TYPE_REVOKE);
    when(mockAuthorization.getUserId()).thenReturn(EXAMPLE_USER_ID);

    when(mockAuthorization.getResourceType()).thenReturn(EXAMPLE_RESOURCE_TYPE_ID);
    when(mockAuthorization.getResourceId()).thenReturn(EXAMPLE_RESOURCE_ID);
    when(mockAuthorization.getPermissions(Permissions.values())).thenReturn(EXAMPLE_REVOKE_PERMISSION_VALUES);

    return mockAuthorization;
  }

  public static List<Authorization> createMockAuthorizations() {
    return Arrays.asList(new Authorization[] { createMockGlobalAuthorization(), createMockGrantAuthorization(), createMockRevokeAuthorization() });
  }

  public static List<Authorization> createMockGrantAuthorizations() {
    return Arrays.asList(new Authorization[] { createMockGrantAuthorization() });
  }

  public static List<Authorization> createMockRevokeAuthorizations() {
    return Arrays.asList(new Authorization[] { createMockRevokeAuthorization() });
  }

  public static List<Authorization> createMockGlobalAuthorizations() {
    return Arrays.asList(new Authorization[] { createMockGlobalAuthorization() });
  }

  public static Date createMockDuedate() {
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.add(Calendar.DATE, 3);
    return cal.getTime();
  } // process application

  public static ProcessApplicationInfo createMockProcessApplicationInfo() {
    ProcessApplicationInfo appInfo = mock(ProcessApplicationInfo.class);
    Map<String, String> mockAppProperties = new HashMap<String, String>();
    String mockServletContextPath = MockProvider.EXAMPLE_PROCESS_APPLICATION_CONTEXT_PATH;
    mockAppProperties.put(ProcessApplicationInfo.PROP_SERVLET_CONTEXT_PATH, mockServletContextPath);
    when(appInfo.getProperties()).thenReturn(mockAppProperties);
    return appInfo;
  }

  // History
  public static List<HistoricActivityInstance> createMockHistoricActivityInstances() {
    List<HistoricActivityInstance> mockList = new ArrayList<HistoricActivityInstance>();
    mockList.add(createMockHistoricActivityInstance());
    return mockList;
  }

  public static HistoricActivityInstance createMockHistoricActivityInstance() {
    HistoricActivityInstance mock = mock(HistoricActivityInstance.class);

    when(mock.getId()).thenReturn(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_ID);
    when(mock.getParentActivityInstanceId()).thenReturn(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_PARENT_ACTIVITY_INSTANCE_ID);
    when(mock.getActivityId()).thenReturn(EXAMPLE_ACTIVITY_ID);
    when(mock.getActivityName()).thenReturn(EXAMPLE_ACTIVITY_NAME);
    when(mock.getActivityType()).thenReturn(EXAMPLE_ACTIVITY_TYPE);
    when(mock.getProcessDefinitionId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
    when(mock.getProcessInstanceId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(mock.getExecutionId()).thenReturn(EXAMPLE_EXECUTION_ID);
    when(mock.getTaskId()).thenReturn(EXAMPLE_TASK_ID);
    when(mock.getCalledProcessInstanceId()).thenReturn(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_CALLED_PROCESS_INSTANCE_ID);
    when(mock.getAssignee()).thenReturn(EXAMPLE_TASK_ASSIGNEE_NAME);
    when(mock.getStartTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_START_TIME));
    when(mock.getEndTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_END_TIME));
    when(mock.getDurationInMillis()).thenReturn(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_DURATION);
    when(mock.isCanceled()).thenReturn(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_IS_CANCELED);
    when(mock.isCompleteScope()).thenReturn(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_IS_COMPLETE_SCOPE);

    return mock;
  }

  public static List<HistoricActivityInstance> createMockRunningHistoricActivityInstances() {
    List<HistoricActivityInstance> mockList = new ArrayList<HistoricActivityInstance>();
    mockList.add(createMockRunningHistoricActivityInstance());
    return mockList;
  }

  public static HistoricActivityInstance createMockRunningHistoricActivityInstance() {
    HistoricActivityInstance mock = mock(HistoricActivityInstance.class);

    when(mock.getId()).thenReturn(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_ID);
    when(mock.getParentActivityInstanceId()).thenReturn(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_PARENT_ACTIVITY_INSTANCE_ID);
    when(mock.getActivityId()).thenReturn(EXAMPLE_ACTIVITY_ID);
    when(mock.getActivityName()).thenReturn(EXAMPLE_ACTIVITY_NAME);
    when(mock.getActivityType()).thenReturn(EXAMPLE_ACTIVITY_TYPE);
    when(mock.getProcessDefinitionId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
    when(mock.getProcessInstanceId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(mock.getExecutionId()).thenReturn(EXAMPLE_EXECUTION_ID);
    when(mock.getTaskId()).thenReturn(EXAMPLE_TASK_ID);
    when(mock.getCalledProcessInstanceId()).thenReturn(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_CALLED_PROCESS_INSTANCE_ID);
    when(mock.getAssignee()).thenReturn(EXAMPLE_TASK_ASSIGNEE_NAME);
    when(mock.getStartTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_START_TIME));
    when(mock.getEndTime()).thenReturn(null);
    when(mock.getDurationInMillis()).thenReturn(null);

    return mock;
  }

  public static List<HistoricCaseActivityInstance> createMockHistoricCaseActivityInstances() {
    ArrayList<HistoricCaseActivityInstance> mockList = new ArrayList<HistoricCaseActivityInstance>();
    mockList.add(createMockHistoricCaseActivityInstance());
    return mockList;
  }

  public static HistoricCaseActivityInstance createMockHistoricCaseActivityInstance() {
    HistoricCaseActivityInstance mock = mock(HistoricCaseActivityInstance.class);

    when(mock.getId()).thenReturn(EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_ID);
    when(mock.getParentCaseActivityInstanceId()).thenReturn(EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_PARENT_CASE_ACTIVITY_INSTANCE_ID);
    when(mock.getCaseActivityId()).thenReturn(EXAMPLE_HISTORIC_CASE_ACTIVITY_ID);
    when(mock.getCaseActivityName()).thenReturn(EXAMPLE_HISTORIC_CASE_ACTIVITY_NAME);
    when(mock.getCaseDefinitionId()).thenReturn(EXAMPLE_CASE_DEFINITION_ID);
    when(mock.getCaseInstanceId()).thenReturn(EXAMPLE_CASE_INSTANCE_ID);
    when(mock.getCaseExecutionId()).thenReturn(EXAMPLE_CASE_EXECUTION_ID);
    when(mock.getTaskId()).thenReturn(EXAMPLE_TASK_ID);
    when(mock.getCalledProcessInstanceId()).thenReturn(EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_CALLED_PROCESS_INSTANCE_ID);
    when(mock.getCalledCaseInstanceId()).thenReturn(EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_CALLED_CASE_INSTANCE_ID);
    when(mock.getCreateTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_CREATE_TIME));
    when(mock.getEndTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_END_TIME));
    when(mock.getDurationInMillis()).thenReturn(EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_DURATION);
    when(mock.isAvailable()).thenReturn(EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_AVAILABLE);
    when(mock.isEnabled()).thenReturn(EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_ENABLED);
    when(mock.isDisabled()).thenReturn(EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_DISABLED);
    when(mock.isActive()).thenReturn(EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_ACTIVE);
    when(mock.isCompleted()).thenReturn(EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_COMPLETED);
    when(mock.isTerminated()).thenReturn(EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_TERMINATED);

    return mock;
  }

  public static List<HistoricCaseActivityInstance> createMockRunningHistoricCaseActivityInstances() {
    List<HistoricCaseActivityInstance> mockList = new ArrayList<HistoricCaseActivityInstance>();
    mockList.add(createMockRunningHistoricCaseActivityInstance());
    return mockList;
  }

  public static HistoricCaseActivityInstance createMockRunningHistoricCaseActivityInstance() {
    HistoricCaseActivityInstance mock = createMockHistoricCaseActivityInstance();

    when(mock.getEndTime()).thenReturn(null);
    when(mock.getDurationInMillis()).thenReturn(null);
    when(mock.isAvailable()).thenReturn(false);
    when(mock.isEnabled()).thenReturn(false);
    when(mock.isDisabled()).thenReturn(false);
    when(mock.isActive()).thenReturn(true);
    when(mock.isCompleted()).thenReturn(false);
    when(mock.isTerminated()).thenReturn(false);

    return mock;
  }

  public static List<HistoricActivityStatistics> createMockHistoricActivityStatistics() {
    HistoricActivityStatistics statistics = mock(HistoricActivityStatistics.class);

    when(statistics.getId()).thenReturn(EXAMPLE_ACTIVITY_ID);
    when(statistics.getInstances()).thenReturn(EXAMPLE_INSTANCES_LONG);
    when(statistics.getCanceled()).thenReturn(EXAMPLE_CANCELED_LONG);
    when(statistics.getFinished()).thenReturn(EXAMPLE_FINISHED_LONG);
    when(statistics.getCompleteScope()).thenReturn(EXAMPLE_COMPLETE_SCOPE_LONG);

    HistoricActivityStatistics anotherStatistics = mock(HistoricActivityStatistics.class);

    when(anotherStatistics.getId()).thenReturn(ANOTHER_EXAMPLE_ACTIVITY_ID);
    when(anotherStatistics.getInstances()).thenReturn(ANOTHER_EXAMPLE_INSTANCES_LONG);
    when(anotherStatistics.getCanceled()).thenReturn(ANOTHER_EXAMPLE_CANCELED_LONG);
    when(anotherStatistics.getFinished()).thenReturn(ANOTHER_EXAMPLE_FINISHED_LONG);
    when(anotherStatistics.getCompleteScope()).thenReturn(ANOTHER_EXAMPLE_COMPLETE_SCOPE_LONG);

    List<HistoricActivityStatistics> activityResults = new ArrayList<HistoricActivityStatistics>();
    activityResults.add(statistics);
    activityResults.add(anotherStatistics);

    return activityResults;
  }

  public static List<HistoricProcessInstance> createMockHistoricProcessInstances() {
    List<HistoricProcessInstance> mockList = new ArrayList<HistoricProcessInstance>();
    mockList.add(createMockHistoricProcessInstance());
    return mockList;
  }

  public static HistoricProcessInstance createMockHistoricProcessInstance() {
    HistoricProcessInstance mock = mock(HistoricProcessInstance.class);

    when(mock.getId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(mock.getBusinessKey()).thenReturn(EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY);
    when(mock.getProcessDefinitionId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
    when(mock.getDeleteReason()).thenReturn(EXAMPLE_HISTORIC_PROCESS_INSTANCE_DELETE_REASON);
    when(mock.getEndTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_PROCESS_INSTANCE_END_TIME));
    when(mock.getStartTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_PROCESS_INSTANCE_START_TIME));
    when(mock.getDurationInMillis()).thenReturn(EXAMPLE_HISTORIC_PROCESS_INSTANCE_DURATION_MILLIS);
    when(mock.getStartUserId()).thenReturn(EXAMPLE_HISTORIC_PROCESS_INSTANCE_START_USER_ID);
    when(mock.getStartActivityId()).thenReturn(EXAMPLE_HISTORIC_PROCESS_INSTANCE_START_ACTIVITY_ID);
    when(mock.getSuperProcessInstanceId()).thenReturn(EXAMPLE_HISTORIC_PROCESS_INSTANCE_SUPER_PROCESS_INSTANCE_ID);
    when(mock.getCaseInstanceId()).thenReturn(EXAMPLE_HISTORIC_PROCESS_INSTANCE_CASE_INSTANCE_ID);

    return mock;
  }

  public static List<HistoricProcessInstance> createMockRunningHistoricProcessInstances() {
    List<HistoricProcessInstance> mockList = new ArrayList<HistoricProcessInstance>();
    mockList.add(createMockHistoricProcessInstanceUnfinished());
    return mockList;
  }

  public static HistoricProcessInstance createMockHistoricProcessInstanceUnfinished() {
    HistoricProcessInstance mock = mock(HistoricProcessInstance.class);
    when(mock.getId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(mock.getBusinessKey()).thenReturn(EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY);
    when(mock.getProcessDefinitionId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
    when(mock.getDeleteReason()).thenReturn(EXAMPLE_HISTORIC_PROCESS_INSTANCE_DELETE_REASON);
    when(mock.getEndTime()).thenReturn(null);
    when(mock.getStartTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_PROCESS_INSTANCE_START_TIME));
    when(mock.getDurationInMillis()).thenReturn(EXAMPLE_HISTORIC_PROCESS_INSTANCE_DURATION_MILLIS);
    return mock;
  }

  public static List<HistoricCaseInstance> createMockHistoricCaseInstances() {
    List<HistoricCaseInstance> mockList = new ArrayList<HistoricCaseInstance>();
    mockList.add(createMockHistoricCaseInstance());
    return mockList;
  }

  public static HistoricCaseInstance createMockHistoricCaseInstance() {
    HistoricCaseInstance mock = mock(HistoricCaseInstance.class);

    when(mock.getId()).thenReturn(EXAMPLE_CASE_INSTANCE_ID);
    when(mock.getBusinessKey()).thenReturn(EXAMPLE_CASE_INSTANCE_BUSINESS_KEY);
    when(mock.getCaseDefinitionId()).thenReturn(EXAMPLE_CASE_DEFINITION_ID);
    when(mock.getCreateTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_CASE_INSTANCE_CREATE_TIME));
    when(mock.getCloseTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_CASE_INSTANCE_CLOSE_TIME));
    when(mock.getDurationInMillis()).thenReturn(EXAMPLE_HISTORIC_CASE_INSTANCE_DURATION_MILLIS);
    when(mock.getCreateUserId()).thenReturn(EXAMPLE_HISTORIC_CASE_INSTANCE_CREATE_USER_ID);
    when(mock.getSuperCaseInstanceId()).thenReturn(EXAMPLE_HISTORIC_CASE_INSTANCE_SUPER_CASE_INSTANCE_ID);
    when(mock.isActive()).thenReturn(EXAMPLE_HISTORIC_CASE_INSTANCE_IS_ACTIVE);
    when(mock.isCompleted()).thenReturn(EXAMPLE_HISTORIC_CASE_INSTANCE_IS_COMPLETED);
    when(mock.isTerminated()).thenReturn(EXAMPLE_HISTORIC_CASE_INSTANCE_IS_TERMINATED);
    when(mock.isClosed()).thenReturn(EXAMPLE_HISTORIC_CASE_INSTANCE_IS_CLOSED);

    return mock;
  }

  public static List<HistoricCaseInstance> createMockRunningHistoricCaseInstances() {
    List<HistoricCaseInstance> mockList = new ArrayList<HistoricCaseInstance>();
    mockList.add(createMockHistoricCaseInstanceNotClosed());
    return mockList;
  }

  public static HistoricCaseInstance createMockHistoricCaseInstanceNotClosed() {
    HistoricCaseInstance mock = createMockHistoricCaseInstance();

    when(mock.getCloseTime()).thenReturn(null);
    when(mock.getDurationInMillis()).thenReturn(null);
    when(mock.isActive()).thenReturn(true);
    when(mock.isCompleted()).thenReturn(false);
    when(mock.isTerminated()).thenReturn(false);
    when(mock.isClosed()).thenReturn(false);

    return mock;
  }

  public static HistoricVariableInstance createMockHistoricVariableInstance() {
    return mockHistoricVariableInstance().build();
  }

  public static MockHistoricVariableInstanceBuilder mockHistoricVariableInstance() {
    return new MockHistoricVariableInstanceBuilder()
        .id(EXAMPLE_VARIABLE_INSTANCE_ID)
        .name(EXAMPLE_VARIABLE_INSTANCE_NAME)
        .typedValue(EXAMPLE_PRIMITIVE_VARIABLE_VALUE)
        .processInstanceId(EXAMPLE_VARIABLE_INSTANCE_PROC_INST_ID)
        .activityInstanceId(EXAMPLE_VARIABLE_INSTANCE_ACTIVITY_INSTANCE_ID)
        .errorMessage(null);
  }

  public static List<ProcessInstance> createAnotherMockProcessInstanceList() {
    List<ProcessInstance> mockProcessInstanceList = new ArrayList<ProcessInstance>();
    mockProcessInstanceList.add(createMockInstance());
    mockProcessInstanceList.add(createAnotherMockInstance());
    return mockProcessInstanceList;
  }

  public static ProcessInstance createAnotherMockInstance() {
    ProcessInstance mock = mock(ProcessInstance.class);

    when(mock.getId()).thenReturn(ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID);
    when(mock.getBusinessKey()).thenReturn(EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY);
    when(mock.getProcessDefinitionId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
    when(mock.getProcessInstanceId()).thenReturn(ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID);
    when(mock.isSuspended()).thenReturn(EXAMPLE_PROCESS_INSTANCE_IS_SUSPENDED);
    when(mock.isEnded()).thenReturn(EXAMPLE_PROCESS_INSTANCE_IS_ENDED);

    return mock;
  }

  public static Set<String> createMockSetFromList(String list){
    return new HashSet<String>(Arrays.asList(list.split(",")));
  }

  public static IdentityLink createMockUserAssigneeIdentityLink() {
    IdentityLink identityLink = mock(IdentityLink.class);
    when(identityLink.getTaskId()).thenReturn(EXAMPLE_TASK_ID);
    when(identityLink.getType()).thenReturn(IdentityLinkType.ASSIGNEE);
    when(identityLink.getUserId()).thenReturn(EXAMPLE_USER_ID);

    return identityLink;
  }

  public static IdentityLink createMockCandidateGroupIdentityLink() {
    IdentityLink identityLink = mock(IdentityLink.class);
    when(identityLink.getTaskId()).thenReturn(EXAMPLE_TASK_ID);
    when(identityLink.getType()).thenReturn(IdentityLinkType.CANDIDATE);
    when(identityLink.getGroupId()).thenReturn(EXAMPLE_GROUP_ID);

    return identityLink;
  }

  public static IdentityLink createAnotherMockCandidateGroupIdentityLink() {
    IdentityLink identityLink = mock(IdentityLink.class);
    when(identityLink.getTaskId()).thenReturn(EXAMPLE_TASK_ID);
    when(identityLink.getType()).thenReturn(IdentityLinkType.CANDIDATE);
    when(identityLink.getGroupId()).thenReturn(EXAMPLE_GROUP_ID2);

    return identityLink;
  }

  // job definition
  public static List<JobDefinition> createMockJobDefinitions() {
    List<JobDefinition> mocks = new ArrayList<JobDefinition>();
    mocks.add(createMockJobDefinition());
    return mocks;
  }

  public static JobDefinition createMockJobDefinition() {
    JobDefinition jobDefinition = mock(JobDefinition.class);

    when(jobDefinition.getId()).thenReturn(EXAMPLE_JOB_DEFINITION_ID);
    when(jobDefinition.getProcessDefinitionId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
    when(jobDefinition.getProcessDefinitionKey()).thenReturn(EXAMPLE_PROCESS_DEFINITION_KEY);
    when(jobDefinition.getJobType()).thenReturn(EXAMPLE_JOB_TYPE);
    when(jobDefinition.getJobConfiguration()).thenReturn(EXAMPLE_JOB_CONFIG);
    when(jobDefinition.getActivityId()).thenReturn(EXAMPLE_ACTIVITY_ID);
    when(jobDefinition.isSuspended()).thenReturn(EXAMPLE_JOB_DEFINITION_IS_SUSPENDED);

    return jobDefinition;
  }

  public static List<UserOperationLogEntry> createUserOperationLogEntries() {
    List<UserOperationLogEntry> entries = new ArrayList<UserOperationLogEntry>();
    entries.add(createUserOperationLogEntry());
    return entries;
  }

  private static UserOperationLogEntry createUserOperationLogEntry() {
    UserOperationLogEntry entry = mock(UserOperationLogEntry.class);
    when(entry.getId()).thenReturn(EXAMPLE_USER_OPERATION_LOG_ID);
    when(entry.getProcessDefinitionId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
    when(entry.getProcessDefinitionKey()).thenReturn(EXAMPLE_PROCESS_DEFINITION_KEY);
    when(entry.getProcessInstanceId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(entry.getExecutionId()).thenReturn(EXAMPLE_EXECUTION_ID);
    when(entry.getCaseDefinitionId()).thenReturn(EXAMPLE_CASE_DEFINITION_ID);
    when(entry.getCaseInstanceId()).thenReturn(EXAMPLE_CASE_INSTANCE_ID);
    when(entry.getCaseExecutionId()).thenReturn(EXAMPLE_CASE_EXECUTION_ID);
    when(entry.getTaskId()).thenReturn(EXAMPLE_TASK_ID);
    when(entry.getUserId()).thenReturn(EXAMPLE_USER_ID);
    when(entry.getTimestamp()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_USER_OPERATION_TIMESTAMP));
    when(entry.getOperationId()).thenReturn(EXAMPLE_USER_OPERATION_ID);
    when(entry.getOperationType()).thenReturn(EXAMPLE_USER_OPERATION_TYPE);
    when(entry.getEntityType()).thenReturn(EXAMPLE_USER_OPERATION_ENTITY);
    when(entry.getProperty()).thenReturn(EXAMPLE_USER_OPERATION_PROPERTY);
    when(entry.getOrgValue()).thenReturn(EXAMPLE_USER_OPERATION_ORG_VALUE);
    when(entry.getNewValue()).thenReturn(EXAMPLE_USER_OPERATION_NEW_VALUE);
    return entry;
  }

  // historic detail ////////////////////

  public static HistoricVariableUpdate createMockHistoricVariableUpdate() {
    return mockHistoricVariableUpdate().build();
  }

  public static MockHistoricVariableUpdateBuilder mockHistoricVariableUpdate() {
    return new MockHistoricVariableUpdateBuilder()
        .id(EXAMPLE_HISTORIC_VAR_UPDATE_ID)
        .processInstanceId(EXAMPLE_HISTORIC_VAR_UPDATE_PROC_INST_ID)
        .activityInstanceId(EXAMPLE_HISTORIC_VAR_UPDATE_ACT_INST_ID)
        .executionId(EXAMPLE_HISTORIC_VAR_UPDATE_EXEC_ID)
        .taskId(EXAMPLE_HISTORIC_VAR_UPDATE_TASK_ID)
        .time(EXAMPLE_HISTORIC_VAR_UPDATE_TIME)
        .name(EXAMPLE_HISTORIC_VAR_UPDATE_NAME)
        .typedValue(EXAMPLE_PRIMITIVE_VARIABLE_VALUE)
        .revision(EXAMPLE_HISTORIC_VAR_UPDATE_REVISION)
        .errorMessage(null);
  }


  public static HistoricFormField createMockHistoricFormField() {
    HistoricFormField historicFromField = mock(HistoricFormField.class);

    when(historicFromField.getId()).thenReturn(EXAMPLE_HISTORIC_FORM_FIELD_ID);
    when(historicFromField.getProcessInstanceId()).thenReturn(EXAMPLE_HISTORIC_FORM_FIELD_PROC_INST_ID);
    when(historicFromField.getActivityInstanceId()).thenReturn(EXAMPLE_HISTORIC_FORM_FIELD_ACT_INST_ID);
    when(historicFromField.getExecutionId()).thenReturn(EXAMPLE_HISTORIC_FORM_FIELD_EXEC_ID);
    when(historicFromField.getTaskId()).thenReturn(EXAMPLE_HISTORIC_FORM_FIELD_TASK_ID);
    when(historicFromField.getTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_FORM_FIELD_TIME));
    when(historicFromField.getFieldId()).thenReturn(EXAMPLE_HISTORIC_FORM_FIELD_FIELD_ID);
    when(historicFromField.getFieldValue()).thenReturn(EXAMPLE_HISTORIC_FORM_FIELD_VALUE);

    return historicFromField;
  }

  public static List<HistoricFormField> createMockHistoricFormFields() {
    List<HistoricFormField> entries = new ArrayList<HistoricFormField>();
    entries.add(createMockHistoricFormField());
    return entries;
  }

  public static List<HistoricDetail> createMockHistoricDetails() {
    List<HistoricDetail> entries = new ArrayList<HistoricDetail>();
    entries.add(createMockHistoricVariableUpdate());
    entries.add(createMockHistoricFormField());
    return entries;
  }

  public static HistoricTaskInstance createMockHistoricTaskInstance() {
    HistoricTaskInstance taskInstance = mock(HistoricTaskInstance.class);

    when(taskInstance.getId()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_ID);
    when(taskInstance.getProcessInstanceId()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_PROC_INST_ID);
    when(taskInstance.getActivityInstanceId()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_ACT_INST_ID);
    when(taskInstance.getExecutionId()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_EXEC_ID);
    when(taskInstance.getProcessDefinitionId()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_PROC_DEF_ID);
    when(taskInstance.getName()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_NAME);
    when(taskInstance.getDescription()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_DESCRIPTION);
    when(taskInstance.getDeleteReason()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_DELETE_REASON);
    when(taskInstance.getOwner()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_OWNER);
    when(taskInstance.getAssignee()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_ASSIGNEE);
    when(taskInstance.getStartTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_TASK_INST_START_TIME));
    when(taskInstance.getEndTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_TASK_INST_END_TIME));
    when(taskInstance.getDurationInMillis()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_DURATION);
    when(taskInstance.getTaskDefinitionKey()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_DEF_KEY);
    when(taskInstance.getPriority()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_PRIORITY);
    when(taskInstance.getDueDate()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_TASK_INST_DUE_DATE));
    when(taskInstance.getFollowUpDate()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_TASK_INST_FOLLOW_UP_DATE));
    when(taskInstance.getParentTaskId()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_PARENT_TASK_ID);
    when(taskInstance.getCaseDefinitionId()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_CASE_DEF_ID);
    when(taskInstance.getCaseInstanceId()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_CASE_INST_ID);
    when(taskInstance.getCaseExecutionId()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_CASE_EXEC_ID);

    return taskInstance;
  }

  public static List<HistoricTaskInstance> createMockHistoricTaskInstances() {
    List<HistoricTaskInstance> entries = new ArrayList<HistoricTaskInstance>();
    entries.add(createMockHistoricTaskInstance());
    return entries;
  }

  // Incident ///////////////////////////////////////

  public static Incident createMockIncident() {
    Incident incident = mock(Incident.class);

    when(incident.getId()).thenReturn(EXAMPLE_INCIDENT_ID);
    when(incident.getIncidentTimestamp()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_INCIDENT_TIMESTAMP));
    when(incident.getIncidentType()).thenReturn(EXAMPLE_INCIDENT_TYPE);
    when(incident.getExecutionId()).thenReturn(EXAMPLE_INCIDENT_EXECUTION_ID);
    when(incident.getActivityId()).thenReturn(EXAMPLE_INCIDENT_ACTIVITY_ID);
    when(incident.getProcessInstanceId()).thenReturn(EXAMPLE_INCIDENT_PROC_INST_ID);
    when(incident.getProcessDefinitionId()).thenReturn(EXAMPLE_INCIDENT_PROC_DEF_ID);
    when(incident.getCauseIncidentId()).thenReturn(EXAMPLE_INCIDENT_CAUSE_INCIDENT_ID);
    when(incident.getRootCauseIncidentId()).thenReturn(EXAMPLE_INCIDENT_ROOT_CAUSE_INCIDENT_ID);
    when(incident.getConfiguration()).thenReturn(EXAMPLE_INCIDENT_CONFIGURATION);
    when(incident.getIncidentMessage()).thenReturn(EXAMPLE_INCIDENT_MESSAGE);

    return incident;
  }

  public static List<Incident> createMockIncidents() {
    List<Incident> entries = new ArrayList<Incident>();
    entries.add(createMockIncident());
    return entries;
  }

  // Historic Incident ///////////////////////////////////////

  public static HistoricIncident createMockHistoricIncident() {
    HistoricIncident incident = mock(HistoricIncident.class);

    when(incident.getId()).thenReturn(EXAMPLE_HIST_INCIDENT_ID);
    when(incident.getCreateTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HIST_INCIDENT_CREATE_TIME));
    when(incident.getEndTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HIST_INCIDENT_END_TIME));
    when(incident.getIncidentType()).thenReturn(EXAMPLE_HIST_INCIDENT_TYPE);
    when(incident.getExecutionId()).thenReturn(EXAMPLE_HIST_INCIDENT_EXECUTION_ID);
    when(incident.getActivityId()).thenReturn(EXAMPLE_HIST_INCIDENT_ACTIVITY_ID);
    when(incident.getProcessInstanceId()).thenReturn(EXAMPLE_HIST_INCIDENT_PROC_INST_ID);
    when(incident.getProcessDefinitionId()).thenReturn(EXAMPLE_HIST_INCIDENT_PROC_DEF_ID);
    when(incident.getCauseIncidentId()).thenReturn(EXAMPLE_HIST_INCIDENT_CAUSE_INCIDENT_ID);
    when(incident.getRootCauseIncidentId()).thenReturn(EXAMPLE_HIST_INCIDENT_ROOT_CAUSE_INCIDENT_ID);
    when(incident.getConfiguration()).thenReturn(EXAMPLE_HIST_INCIDENT_CONFIGURATION);
    when(incident.getIncidentMessage()).thenReturn(EXAMPLE_HIST_INCIDENT_MESSAGE);
    when(incident.isOpen()).thenReturn(EXAMPLE_HIST_INCIDENT_STATE_OPEN);
    when(incident.isDeleted()).thenReturn(EXAMPLE_HIST_INCIDENT_STATE_DELETED);
    when(incident.isResolved()).thenReturn(EXAMPLE_HIST_INCIDENT_STATE_RESOLVED);

    return incident;
  }

  public static List<HistoricIncident> createMockHistoricIncidents() {
    List<HistoricIncident> entries = new ArrayList<HistoricIncident>();
    entries.add(createMockHistoricIncident());
    return entries;
  }

  // case definition
  public static List<CaseDefinition> createMockCaseDefinitions() {
    List<CaseDefinition> mocks = new ArrayList<CaseDefinition>();
    mocks.add(createMockCaseDefinition());
    return mocks;
  }

  public static CaseDefinition createMockCaseDefinition() {
    MockCaseDefinitionBuilder builder = new MockCaseDefinitionBuilder();

    CaseDefinition mockDefinition = builder
        .id(EXAMPLE_CASE_DEFINITION_ID)
        .category(EXAMPLE_CASE_DEFINITION_CATEGORY)
        .name(EXAMPLE_CASE_DEFINITION_NAME)
        .key(EXAMPLE_CASE_DEFINITION_KEY)
        .version(EXAMPLE_CASE_DEFINITION_VERSION)
        .resource(EXAMPLE_CASE_DEFINITION_RESOURCE_NAME)
        .deploymentId(EXAMPLE_DEPLOYMENT_ID)
        .build();

    return mockDefinition;
  }

  // case instance
  public static List<CaseInstance> createMockCaseInstances() {
    List<CaseInstance> mocks = new ArrayList<CaseInstance>();
    mocks.add(createMockCaseInstance());
    return mocks;
  }

  public static CaseInstance createMockCaseInstance() {
    CaseInstance mock = mock(CaseInstance.class);

    when(mock.getId()).thenReturn(EXAMPLE_CASE_INSTANCE_ID);
    when(mock.getBusinessKey()).thenReturn(EXAMPLE_CASE_INSTANCE_BUSINESS_KEY);
    when(mock.getCaseDefinitionId()).thenReturn(EXAMPLE_CASE_INSTANCE_CASE_DEFINITION_ID);
    when(mock.isActive()).thenReturn(EXAMPLE_CASE_INSTANCE_IS_ACTIVE);
    when(mock.isCompleted()).thenReturn(EXAMPLE_CASE_INSTANCE_IS_COMPLETED);
    when(mock.isTerminated()).thenReturn(EXAMPLE_CASE_INSTANCE_IS_TERMINATED);

    return mock;
  }

  // case execution
  public static List<CaseExecution> createMockCaseExecutions() {
    List<CaseExecution> mocks = new ArrayList<CaseExecution>();
    mocks.add(createMockCaseExecution());
    return mocks;
  }

  public static CaseExecution createMockCaseExecution() {
    CaseExecution mock = mock(CaseExecution.class);

    when(mock.getId()).thenReturn(EXAMPLE_CASE_EXECUTION_ID);
    when(mock.getCaseInstanceId()).thenReturn(EXAMPLE_CASE_EXECUTION_CASE_INSTANCE_ID);
    when(mock.getParentId()).thenReturn(EXAMPLE_CASE_EXECUTION_PARENT_ID);
    when(mock.getCaseDefinitionId()).thenReturn(EXAMPLE_CASE_EXECUTION_CASE_DEFINITION_ID);
    when(mock.getActivityId()).thenReturn(EXAMPLE_CASE_EXECUTION_ACTIVITY_ID);
    when(mock.getActivityName()).thenReturn(EXAMPLE_CASE_EXECUTION_ACTIVITY_NAME);
    when(mock.isActive()).thenReturn(EXAMPLE_CASE_EXECUTION_IS_ACTIVE);
    when(mock.isEnabled()).thenReturn(EXAMPLE_CASE_EXECUTION_IS_ENABLED);
    when(mock.isDisabled()).thenReturn(EXAMPLE_CASE_EXECUTION_IS_DISABLED);

    return mock;
  }

  public static VariableMap createMockFormVariables() {
    VariableMap mock = Variables.createVariables();
    mock.putValueTyped(EXAMPLE_VARIABLE_INSTANCE_NAME, EXAMPLE_PRIMITIVE_VARIABLE_VALUE);
    return mock;
  }

  public static List<Filter> createMockFilters() {
    List<Filter> mocks = new ArrayList<Filter>();
    mocks.add(createMockFilter(EXAMPLE_FILTER_ID));
    mocks.add(createMockFilter(ANOTHER_EXAMPLE_FILTER_ID));
    return mocks;
  }

  public static Filter createMockFilter() {
    return createMockFilter(EXAMPLE_FILTER_ID);
  }

  public static Filter createMockFilter(String id) {
    Filter mock = mockFilter()
      .id(id)
      .resourceType(EXAMPLE_FILTER_RESOURCE_TYPE)
      .name(EXAMPLE_FILTER_NAME)
      .owner(EXAMPLE_FILTER_OWNER)
      .query(EXAMPLE_FILTER_QUERY)
      .properties(EXAMPLE_FILTER_PROPERTIES)
      .build();

    doThrow(new NotValidException("Name must not be null"))
      .when(mock).setName(null);
    doThrow(new NotValidException("Name must not be empty"))
      .when(mock).setName("");
    doThrow(new NotValidException("Query must not be null"))
      .when(mock).setQuery(null);

    return mock;
  }

  public static MockFilterBuilder mockFilter() {
    return new MockFilterBuilder()
      .id(EXAMPLE_FILTER_ID)
      .resourceType(EXAMPLE_FILTER_RESOURCE_TYPE)
      .name(EXAMPLE_FILTER_NAME)
      .owner(EXAMPLE_FILTER_OWNER)
      .query(EXAMPLE_FILTER_QUERY)
      .properties(EXAMPLE_FILTER_PROPERTIES);
  }

  public static FilterQuery createMockFilterQuery() {
    List<Filter> mockFilters = createMockFilters();

    FilterQuery query = mock(FilterQuery.class);

    when(query.list()).thenReturn(mockFilters);
    when(query.count()).thenReturn((long) mockFilters.size());
    when(query.filterId(anyString())).thenReturn(query);
    when(query.singleResult()).thenReturn(mockFilters.get(0));

    FilterQuery nonExistingQuery = mock(FilterQuery.class);
    when(query.filterId(NON_EXISTING_ID)).thenReturn(nonExistingQuery);
    when(nonExistingQuery.singleResult()).thenReturn(null);

    return query;

  }
}
