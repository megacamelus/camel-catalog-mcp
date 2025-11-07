## Apache Camel 4.x Migration guide

### General Guidance
- Upgrade incrementally (e.g., 4.0 → 4.1 → 4.2).
- Use https://github.com/apache/camel-upgrade-recipes/ for automation; manual steps still required.

---

### Apache Camel Upgrade Recipes Tool

**Purpose**:  
The Camel Upgrade Recipes project, built on OpenRewrite, is designed to assist (not fully automate) the migration of Apache Camel applications. It helps reduce manual effort and errors during migration.

**Scope**:  
- Supports migration tasks via predefined recipes.
- Refer to the https://github.com/apache/camel-upgrade-recipes/blob/main/release_notes.adoc for details on specific migrations covered.

**Usage**:  
Two ways to run the upgrade tool:

1. **Using Maven Plugin**:
   ```bash
   mvn -U org.openrewrite.maven:rewrite-maven-plugin:run \
     -Drewrite.recipeArtifactCoordinates=org.apache.camel.upgrade:camel-upgrade-recipes:LATEST \
     -Drewrite.activeRecipes=org.apache.camel.upgrade.CamelMigrationRecipe
   ```

2. **Using Camel JBang**:
   ```bash
   camel update run $VERSION
   ```

### Key Changes by Version

#### 4.0 → 4.1
- **DSL**: `scriptLanguage` replaces `type` in bean definitions.
- **JMX**: `customId="true"` removed from route dumps.
- **Scheduler/Timer**: Metadata headers removed by default; use `includeMetadata=true`.
- **AWS2 Step Functions**: Header names standardized.
- **AWS2 SNS**: `queueUrl` replaced by `queueArn`.
- **PDF**: Font enum updated to PDFBox 3.0.0.
- **JBang**: `pipe` renamed to `script`; secrets options removed.
- **Jetty/Servlet**: Stack traces muted by default; use `muteException=false`.
- **YAML DSL**: `camel-yaml-dsl.json` removed; use `camelYamlDsl.json`.
- **Tracing**: `Tag` enum deprecated; use `TagConstants`.
- **Kafka**: Default `sessionTimeoutMs=45000`, `consumerRequestTimeoutMs=30000`.
- **REST**: `enableNoContentResponse=true` returns HTTP 204 for empty JSON/XML.

#### 4.1 → 4.2
- **API**: `matchProcess` method updated.
- **Debugger**: Refactored to interface + default impl.
- **Main**: `camel.main.debugger` → `camel.debug.enabled`.
- **File**: `readLockMinAge` restored to 3.x behavior.
- **Kafka**: DNS pre-validation added; disable via `preValidateHostAndPort=false`.
- **Observation**: `excludePatterns` changed from `Set` to comma-separated `String`.
- **Saga/LRA**: Methods now accept `Exchange`.
- **Ignite**: Not Java 21 compatible.
- **Dynamic Router**: Uses Multicast Processor; new config options added.
- **Spring Boot**: Honors `@Primary` beans; `initialProperties`/`overrideProperties` override Spring Boot props.

#### 4.2 → 4.3
- **Core**: `MemoryStateRepository` and `FileStateRepository` moved to `camel-support`.
- **EIPs**: `batch-config` → `batchConfig`, `stream-config` → `streamConfig`.
- **Throttle**: `maxRequestsPerPeriod` → `maxConcurrentRequests`.
- **Health Checks**: Consumers mark as ready earlier.
- **Management**: `nodeIdPrefix` used in MBean `ObjectName`.
- **Console**: JSON keys renamed; values now in millis.
- **JBang**: `transform` → `transform route`; new `transform message`.
- **Jetty**: Upgraded to v12.
- **Kafka**: `CommitManager` behavior clarified.
- **YAML DSL**: `exchange-property` → `exchangeProperty`; `inheritErrorHandler` removed from non-applicable EIPs.
- **HDFS**: Deprecated.
- **Kafka**: Metadata header renamed.
  
#### 4.3 → 4.4
- **Core**: `IOException` → `Exception` in `unmarshal`; `getCreated` deprecated.
- **Languages**: Fluent builder introduced; `source` replaces `headerName`.
- **WireTap**: `CORRELATION_ID` no longer set.
- **Throttle**: Supports both concurrent and total request modes.
- **MDC Logging**: Custom keys cleared post-routing.
- **Main**: `camel.main.routesController.*` → `camel.routecontroller.*`.
- **Kamelet**: Error handling disabled by default; enable via `noErrorHandler=false`.
- **Azure Components**: `useDefaultIdentity` replaced by `credentialType`.
- **CassandraQL/SQL**: `deserializationFilter` added.
- **CoAP**: Header removed; use `toD`.
- **Consul**: Migrated to `kiwiproject`.
- **Dynamic Router**: Control messages refactored.
- **Facebook**: Component removed.
- **Kafka**: Batch support added.
- **JSON Validator**: Loader class replaced.
- **Splunk HEC**: `token` moved to query param.
- **OpenAPI**: Swagger 2.0 deprecated.
- **Spring Boot**: BOM ordering changed; route controller config moved.
- **Vert.x HTTP**: Session handling added.

#### 4.4 → 4.5
- **Core**: Deprecated methods removed; `getUptimeMillis`, `getStartDate` removed.
- **Rest DSL**: `inlineRoutes=true` by default; placeholders resolved eagerly.
- **Avro**: Default library changed to Jackson Avro.
- **Intercept EIP**: `Exchange.INTERCEPTED_ENDPOINT` moved to property.
- **BOMs**: Hardcoded versions used.
- **Main**: `backlogTracing` options moved to `camel.trace`.
- **Console**: `@DevConsole` enhanced.
- **JBang**: `--profile` removed from `export`; `--observe` replaces `--health`/`--metrics`.
- **JSONPath**: Converts each list entry to POJO.
- **Kamelet**: Routes not registered as JMX MBeans.
- **Micrometer/Metrics**: `serviceName` → `kind`; context-level metrics added.
- **OpenAPI**: Swagger 2.0 dropped.
- **Vert.x HTTP**: Cookie handler added.
- **Shiro**: Upgraded to v2.0.
- **Twilio**: APIs removed.
- **Elasticsearch/OpenSearch**: Aggregation strategy class renamed.
- **Spring Redis**: Idempotent repo classes renamed.
- **Spring Boot**: `camel.springboot.xxx` deprecated; use `camel.main.xxx`.

#### 4.5 → 4.6
- **Variables**: `variableReceive` only sets variable on successful exchange.
- **Core Model**: Bean model unified.
- **XML/YAML DSL**: `property` → `properties`; `streamCaching` → `streamCache`.
- **Elasticsearch**: Class renamed.
- **Rest OpenAPI**: `specificationUri` → `String`; validator changed.
- **LangChain4j**: Component names and headers updated.
- **Platform HTTP**: `createConsumer` return type changed.
- **Vert.x HTTP**: Multipart `AttachmentMessage` ID changed.
- **Google Sheets**: `scopes` → comma-separated `String`.
- **Kafka**: Custom subscription adapters added.
- **Azure ServiceBus**: Refactored to high-level client; config options changed.
- **JBang**: Preloads files on `--source-dir`; `--open-api` uses contract-first.
- **AS2**: MIME compression/signing/encryption changes.
- **Spring Boot**: Cluster services moved to dedicated starters.

#### 4.6 → 4.7
- **API**: `ValidatorKey`, `TransformerKey` moved; `addRestService` updated.
- **Core**: `EndpointRegistry` changed; stats collector immutable.
- **Health**: Routes with `auto-startup=false` reported as UP; new default for `UnhealthyOnExhausted`, `UnhealthyOnRestarting`.
- **Cloud**: Deprecated.
- **Crypto**: PGP moved to `camel-crypto-pgp`.
- **DSL**: WireTap deep-copy for `StreamCache`; LoadBalancer EIP names aligned.
- **File**: `idempotent=true` now eager; `idempotentEager=false` restores old behavior.
- **JBang**: `generate` commands moved to plugin.
- **Jetty/Servlet/Undertow**: `CamelHttpServletRequest/Response` headers removed; use `HttpMessage` API.
- **Seda/Disruptor**: Deep-copy for `StreamCache` in `InOnly`.
- **Debug**: `messageHistoryOnBreakpointAsXml` includes current node.
- **Spring Security**: `AccessDecisionManager` → `AuthorizationManager`.
- **CloudEvents**: API moved to `camel-api`.
- **Hashicorp Vault**: `engine` now dynamic; env var removed.
- **Test**: `CamelTestSupport` refactored; methods deprecated.
- **PubNub**: `wherenow` operation removed.
- **AS2**: MIME compression/signing/encryption logic updated.
- **Spring Boot**: `camel-debug-starter` introduced; Kotlin DSL deprecated.

#### 4.7 → 4.8
- **API**: `bind` method added.
- **Kamelet**: Error handling refactored; retries now handled inside Kamelet.
- **Azure Files**: `FilesHeaders` → `FilesConstants`.
- **AWS2 S3**: `CamelAwsS3BucketName` → `CamelAwsS3OverrideBucketName`.
- **File**: Filtering optimized; API changes for `GenericFile`.
- **Google Storage**: `CamelGoogleCloudStorageBucketName` → `CamelGoogleCloudStorageOverrideBucketName`.
- **JGroups**: Cluster lock removed.
- **JBang**: `camel-k` commands removed.
- **Micrometer**: Dynamic endpoints fixed; `baseEndpointURIExchangeEventNotifier=false` discouraged.
- **Minio**: `CamelMinioBucketName` → `CamelMinioOverrideBucketName`.
- **Google PubSub Lite**: Deprecated.
- **Tracing**: MDC `trace_id`/`span_id` deprecated.
- **LangChain4j Chat**: Function calling removed; use `langchain4j-tools`.
- **SMB**: Extended consumer/producer options.
- **Solr**: Refactored; headers renamed to `CamelSolr*`.
- **Test Infra**: `XXXContainerService` constructors removed; use `XXXServiceFactory.createLocalService`.
- **Spring Boot**: `camel-k-starter` removed.

#### 4.10 → 4.11
- **EIPs**: `synchronous=true` added to parallel EIPs; MDC propagation in WireTap/OnCompletion.
- **API**: `removeTraits`, `bind(Supplier)` added.
- **Attachments**: Refactored to message traits; `AttachmentMap` removed.
- **Bean**: `Exchange.BEAN_METHOD_NAME` deprecated.
- **Main**: `camel.main.lightweight` removed.
- **File Components**: Dynamic polling optimized; new `DynamicPollingConsumer` API.
- **FTP**: `Exchange.FILE_NAME` includes relative path.
- **AS2**: Config options changed for tooling friendliness.
- **Kafka**: `recordMetadata` default `false`.
- **JBang**: `lazy-bean=true` by default; `--observe` replaces `--health`/`--metrics`.
- **SQL**: Batch operations now transactional.
- **Etcd3**: Removed.
- **Platform HTTP**: Header filter strategy class removed.
- **Undertow**: Header filter strategy deprecated.
- **Metrics**: `app.info` gauge added.
- **Observability**: `camel-opentelemetry` → `camel-opentelemetry2`.