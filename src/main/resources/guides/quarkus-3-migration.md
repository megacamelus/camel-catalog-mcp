## Camel Quarkus 3.x Migration guide

### General Migration References
- **Quarkus changes**: Refer to https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.0.
- **Camel changes**: Refer to ../../../manual/camel-4-migration-guide.html.

---

### Removed Extensions and Alternatives

| Removed Extension | Alternative(s) |
|-------------------|----------------|
| `camel-quarkus-atlasmap` | `camel-quarkus-mapstruct` |
| `camel-quarkus-atmos` | none |
| `camel-quarkus-avro-rpc` | none |
| `camel-quarkus-caffeine-lrucache` | `camel-quarkus-ignite`, `camel-quarkus-infinispan` |
| `camel-quarkus-corda` | none |
| `camel-quarkus-dozer` | `camel-quarkus-mapstruct` |
| `camel-quarkus-elasticsearch-rest` | `camel-quarkus-elasticsearch` |
| `camel-quarkus-hbase` | none |
| `camel-quarkus-iota` | none |
| `camel-quarkus-jbpm` | none |
| `camel-quarkus-jclouds` | none |
| `camel-quarkus-johnzon` | `camel-quarkus-jackson`, `camel-quarkus-fastjson`, `camel-quarkus-gson` |
| `camel-quarkus-microprofile-metrics` | `camel-quarkus-micrometer`, `camel-quarkus-opentelemetry` |
| `camel-quarkus-milo` | none |
| `camel-quarkus-opentracing` | `camel-quarkus-micrometer`, `camel-quarkus-opentelemetry` |
| `camel-quarkus-rabbitmq` | `camel-quarkus-spring-rabbitmq` |
| `camel-quarkus-vm` | `camel-quarkus-seda` |
| `camel-quarkus-xstream` | `camel-quarkus-jacksonxml` |

---

### Version-Specific Migration Notes

#### 3.15.0
- **Micrometer**: Upgraded to 1.13.x. Replace:
  - `io.micrometer:micrometer-registry-prometheus` → `io.quarkus:quarkus-micrometer-registry-prometheus`
- **Deprecated extensions**:
  - `camel-quarkus-kotlin`
  - `camel-quarkus-kotlin-dsl`
- **Deprecated methods in `CamelQuarkusTestSupport`**:
  - `isUseAdviceWith`, `doPreSetup`, `doPostSetup`, `postProcessTest`
  - `JUnit Lifecycle.PER_CLASS` usage is deprecated.

#### 3.17.0
- **AWS2 Extensions**: No longer tested with Quarkus Amazon Services. Use `camel-quarkus-aws2` directly.

#### 3.18.0
- **Kamelets + Java DSL**: Requires `camel-quarkus-yaml-dsl` dependency:
  ```xml
  <dependency>
    <groupId>org.apache.camel.quarkus</groupId>
    <artifactId>camel-quarkus-yaml-dsl</artifactId>
  </dependency>
  ```
  - Missing this causes: `IllegalArgumentException: Cannot find RoutesBuilderLoader...`

#### 3.23.0
- **Jolokia**: `/q/jolokia` endpoint no longer registered by default.
  - To re-enable:
    ```
    quarkus.camel.jolokia.register-management-endpoint=true
    ```
  - This option will be removed in future.

#### 3.26.0
- **Property rename**:
  - `quarkus.camel.dev-ui.update-internal` → `quarkus.camel.dev-ui.update-interval`
- **Hazelcast**:
  - `quarkus-hazelcast` removed from `camel-quarkus-hazelcast` due to incompatibility with Quarkus ≥ 3.26.0.
  - Use JVM mode with `camel-hazelcast`.
- **OptaPlanner**:
  - `optaplanner-quarkus` removed from `camel-quarkus-optaplanner` for same reason.
  - Use JVM mode with `camel-optaplanner`.

#### 3.27.0
- **Removed**:
  - `camel-quarkus-langchain4j` (deprecated in 3.26.0)
  - Quarkus LangChain4j functionality removed from all related extensions.
  - Use Camel LangChain4j components as per their documentation.