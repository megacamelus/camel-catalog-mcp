## **Camel 3.x to 4.0 migration guide**

### **General Migration**
- Upgrade from Camel 3.20+ only. For older versions, first upgrade to 3.20.
- Java 17 required; Java 11 support dropped.

Here’s a compact yet complete summary of the file **`camel-upgrade-recipes-tool.md`**, optimized for use in Camel 4 migration scenarios:

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

### **Removed Components**
- Fully removed: `camel-any23`, `camel-atlasmap`, `camel-atmos`, `camel-corda`, `camel-directvm`, `camel-dozer`, `camel-elasticsearch-rest`, `camel-gora`, `camel-hbase`, `camel-hyperledger-aries`, `camel-iota`, `camel-ipfs`, `camel-jbpm`, `camel-jclouds`, `camel-spark`, `camel-spring-integration`, `camel-weka`, `camel-xstream`, `camel-zipkin`.
- Replacements:
  - `camel-caffeine-lrucache` → `camel-cache`, `camel-ignite`, `camel-infinispan`
  - `camel-cdi` → `camel-spring-boot`, `camel-quarkus`
  - `camel-dozer` → `camel-mapstruct`
  - `camel-johnzon` → `camel-jackson`, `camel-fastjson`, `camel-gson`
  - `camel-microprofile-metrics`, `camel-opentracing`, `camel-zipkin` → `camel-micrometer`, `camel-opentelemetry`
  - `camel-milo` → `camel-plc4x`
  - `camel-rabbitmq` → `spring-rabbitmq-component`
  - `camel-rest-swagger` → `camel-openapi-rest`
  - `camel-restdsl-swagger-plugin` → `camel-restdsl-openapi-plugin`
  - `camel-resteasy` → `camel-cxf`, `camel-rest`
  - `camel-swagger-java` → `camel-openapi-java`
  - `camel-websocket`, `camel-websocket-jsr356` → `camel-vertx-websocket`
  - `camel-vertx-kafka` → `camel-kafka`
  - `camel-vm` → `camel-seda`

---

### **Logging & Testing**
- `slf4j-api` upgraded to 2.0.
- JUnit 4 modules removed; JUnit 5 used.

---

### **API Changes**
- Removed: `InOptionalOut`, `@FallbackConverter`, `getEndpointMap()`, `uri` on annotations, `label`, `consumerClass`, `asyncCallback`, `OnCamelContextStart/Stop`, `Discard`, `SimpleBuilder`, `archetypeCatalogAsXml`, `configure`, `getExtension`.
- Replaced: `adapt()` → `getCamelContextExtension()`, `getExchangeExtension()`.
- Moved: `IntrospectionSupport` → internal use; use `BeanIntrospection`.
- Changed: `dumpRoutes` type, `HealthCheck.isLiveness` default false.
- Added: `StreamCache.position()`, `EventNotifierSupport` implements `CamelContextAware`.
- Decoupled: `ExtendedCamelContext`, `ExtendedExchange`.

---

### **EIP Changes**
- Removed: `lang` on `<description>`, `InOnly`, `InOut`.
- Use `SetExchangePattern` or `To` instead.
- `PollEnrich`: URI now stored as exchange property `CamelToEndpoint`.
- `CircuitBreaker`: YAML/XML config attributes updated.

---

### **DSL Changes**
- XML: `<description>` now an attribute.
- YAML: `steps` must be child of `route`.

---

### **Type Converter**
- Removed: `String` → `File`.

---

### **Tracing**
- `traceTemplates=true` enables internal route tracing.
- `BacklogTracer` now traces streaming headers; may affect stream position.

---

### **Error Handling**
- `useOriginalMessage`/`Body`: now defensively copied and converted to `StreamCache`.

---

### **Health Checks**
- Default: readiness only.
- `CamelContextCheck`: both readiness and liveness.
- Only consumer checks enabled by default.
- Producer checks: `camel.health.producers-enabled=true` required.

---

### **JMX**
- Added: `doCatch`, `doFinally` MBeans.
- Renamed: `choiceStatistics` → `extendedInformation`, `exceptionStatistics` → `extendedInformation`.
- Removed: `dumpRouteAsXml(boolean, boolean)`.

---

### **Backlog Tracing**
- `backlogTracing=true` auto-enables tracer.
- `backlogTracingStandby=true` restores old behavior.
- Class moved: `BacklogTracerEventMessage` to `camel-api`.
- Refactored: `DefaultBacklogTracerEventMessage` → interface with input/output trace.

---

### **Serialization**
- XML serialization now uses `camel-xml-io`, not JAXB.

---

### **OpenAPI Plugin**
- Default rest component: `platform-http` (was `servlet`).

---

### **Component Changes**
- `Category`: enums reduced from 83 to 37.
- `camel-openapi-rest-dsl-generator`: model updated to `apicurio-data-models` 2.0.3.
- `camel-atom`: switched to RSSReader.
- `camel-azure-cosmosdb`: `itemPartitionKey` now `String`.
- `camel-bean`: method param types must use `.class` syntax.
- `camel-box`: SDK v4; thumbnail method removed.
- `camel-caffeine`: `keyType` removed; keys must be `String`.
- `camel-fhir`: upgraded to `hapi-fhir` 6.2.4; `Delete` API return type changed.
- `camel-google-*`: SDK v2; some API changes.
- `camel-http`: upgraded to HttpComponents v5; timeout config changed.
- `camel-http-common`: `HttpBinding.parseBody()` updated; `HttpMessage` → `Message`.
- `camel-kubernetes`: `replace*` → `update*`; constants renamed.
- `camel-web3j`: upgraded to v5; API changes.
- `camel-main`: constants moved to `MainConstants`.
- `camel-micrometer`: metrics renamed per Micrometer conventions.
- `camel-jbang`: `dependencies` → `dependency`, `-dir` → `--dir`, `stop --all` removed, placeholders use `#name`.
- `camel-jpa`: `transactionManager` removed; use `transactionStrategy`.
- `camel-openapi-java`: uses `swagger.v3`; return type changed; supports OpenAPI 3.0.x/3.1.x.
- `camel-optaplanner`: uses `SolverManager`; `ProblemFactChange` → `ProblemChange`.
- `camel-platform-http-vertx`: suspended route returns 503.
- `camel-salesforce`: blob field names simplified.
- `camel-slack`: default delay increased to 10s.
- `camel-spring-rabbitmq`: `replyTimeout` default changed to 30s.
- `camel-spring-boot`: `camel-spring-xml` removed; use `camel-spring-boot-xml-starter`.
- Graceful shutdown: Camel shutdown delayed to allow Spring Boot to finish first.
- `camel-micrometer-starter`: `uri` tags static by default; enable dynamic via `camel.metrics.uriTagDynamic=true`.
- `camel-platform-http-starter`: uses Spring HTTP server; servlet context-path removed.
- `camel-twitter`: updated to Twitter4j 4.1.2; package changes.

---

### **Patch Upgrades**
- **4.0.1 → 4.0.2**:
  - `camel-file`: `readLockMinAge` restored to 3.x behavior.
- **4.0.0 → 4.0.1**:
  - `camel-aws2-sns`: `queueUrl` → `queueArn`.