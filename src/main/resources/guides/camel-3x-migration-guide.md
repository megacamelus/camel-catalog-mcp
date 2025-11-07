## **Camel 3.x migration guide**

### Apache Camel 3.x Migration Summary

#### General Upgrade Strategy
- Upgrade incrementally (e.g., 3.0 → 3.1 → 3.2).
- Follow each version's guide in sequence.

---

### Core Framework Changes

- **JAXB**: Now optional. Use `camel-xml-io` for lightweight XML DSL parsing.
- **Graceful Shutdown**: Timeout reduced from 300s to 45s.
- **Message History**: Disabled by default.
- **Inflight Repository**: Browsing disabled by default.
- **Component Verifier**: Requires `camel-core-catalog`.
- **ManagedRuntimeCatalog**: Removed.
- **JMX**: Disabled by default in Spring Boot 2.2+.
- **Type Converters**: Use `@Converter(loader = true)` or enable scanning.
- **Shutdown Behavior**: `ExecutorServiceManager` now allows core thread timeout.
- **Stream Caching**: Enabled by default from 3.17.
- **Exchange Recycling**: Consumers can reuse `Exchange` instances.
- **Exchange Properties**: Split into user vs internal (`getAllProperties()`).
- **Error Handling**: `useOriginalMessage` now uses `StreamCache`.

---

### API & DSL Changes

- **Exchange API**:
  - `getCreated()` returns `long` (was `Date`).
  - `isExternalRedelivered()` returns `boolean` (was `Boolean`).
  - `Exchange.ROUTE_STOP`, `ROLLBACK_ONLY`, etc., replaced with methods.
- **Java DSL**:
  - Removed `Supplier`-based methods.
  - `RouteBuilder.adviceWith()` moved to `AdviceWith`.
- **Rest DSL**:
  - Embedded routes removed.
  - `uri` renamed to `path`.
  - Verb classes renamed (e.g., `GetVerbDefinition` → `GetDefinition`).
- **Removed Classes**:
  - `ModelHelper`, `JsonSchemaHelper`, `JavaUuidGenerator`, `camel-testcontainers-*`.
- **Renamed Classes**:
  - Many internal classes moved to better-suited packages.
- **Removed Annotations**:
  - `@Experimental` moved to `camel-api`.

---

### Component Changes

#### Endpoint URI Syntax Changes
- `camel-etcd`: `etcd:action/path` → `etcd-keys:path`, etc.
- `camel-nats`, `camel-nsq`: `nats:servers` → `nats:topic`.
- `camel-ipfs`: `ipfs:host:port/command` → `ipfs:command`.
- `camel-xmlsecurity`: `xmlsecurity:command/name` → `xmlsecurity-sign:name`, etc.
- `camel-milo`: `milo-client:tcp` → `milo-client:opc.tcp`.

#### Testing & Spring Boot
- `camel-test` no longer includes `camel-management`.
- Spring Boot config keys flattened (e.g., `camel.component.kafka.brokers`).
- `camel-spring-boot` actuator `/actuator/camelroutes` removed.
- `camel-spring-boot-xml-starter` required for XML DSL.

#### Component-Specific
- `camel-bean`: Default scope is `singleton`.
- `camel-ftp`: `stepwise=true` not supported with `streamDownload=true`.
- `camel-irc`: Removed `#room` from URI.
- `camel-weather`: Upgraded to Apache HttpClient 4.x.
- `camel-xstream`, `camel-any23`: XML DSL flattened.
- `camel-salesforce`: `sObjectName` precedence changed.
- `camel-jdbc`: Spring dependency removed; use `camel-spring-jdbc`.
- `camel-jsonpath`: Default parser changed to Jackson.
- `camel-jackson`: `enableJaxbAnnotationModule` removed.
- `camel-saxon`, `camel-xmlsecurity`, `camel-jaxb`: Secure XML parsing enforced.

---

### Component Lifecycle & Packaging

- **Custom Components**:
  - Use `camel-package-maven-plugin` instead of `camel-apt`.
- **Karaf/OSGi**:
  - Moved to `org.apache.camel.karaf`.
  - Introduced `camel-karaf-bom`.
  - Removed support for many components (e.g., `camel-hdfs`, `camel-pulsar`).
- **Removed Components**:
  - `camel-spark-rest`, `camel-hipchat`, `camel-apns`, `camel-dozer`, `camel-cmis`, `camel-vertx-kafka`, `camel-atomix`, `camel-beanstalk`, `camel-etcd`, `camel-hystrix`, `camel-yammer`, etc.
- **Deprecated AWS Components**:
  - All `camel-aws-*` replaced by `camel-aws2-*`.

---

### Testing & Dev Tools

- `camel-testcontainers-*`: Removed.
- `camel-jbang`: Placeholder syntax changed from `$name` to `#name`.

---

### Security & Compliance

- **TLS**: Default protocol changed to `TLSv1.3`.
- **XML Parsers**: DOCTYPE and external DTD/schema access disabled.
- **Sensitive Data**: Endpoint URIs now mask secrets in logs.