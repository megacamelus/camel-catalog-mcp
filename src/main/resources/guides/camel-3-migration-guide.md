## **Camel 2.x to 3.0 migration guide**

### **Java Compatibility**
- Camel 3 supports Java 11; Java 8 support is deprecated.
- JAXB removed from JDK 11; add `jaxb-api`, `jaxb-core`, `jaxb-impl` manually.

### **Core Modularization**
- `camel-core` split into multiple modules (`camel-api`, `camel-base`, `camel-main`, etc.).
- Core components moved to separate JARs (e.g., `camel-direct`, `camel-file`, `camel-log`, etc.).
- Use fine-grained Maven dependencies to reduce classpath size.

### **Spring Boot**
- Maven `groupId` changed to `org.apache.camel.springboot`.

### **CamelContext**
- Only one `CamelContext` per deployment supported.
- `context` attribute removed from annotations like `@EndpointInject`.

### **Custom Extensions**
- Use `camel-support` instead of `camel-core`.
- Classes like `DefaultComponent`, `DefaultEndpoint` moved to `org.apache.camel.support`.

### **Component Changes**
- Deprecated components removed (e.g., `camel-http`, `camel-mina`, `camel-rx`, etc.).
- Renamed components: `http4`→`http`, `hdfs2`→`hdfs`, `mina2`→`mina`, etc.
- AWS split into multiple components (`camel-aws-s3`, `camel-aws-sqs`, etc.).
- `camel-telegram` token moved to query param.
- `camel-kafka` removed `bridgeEndpoint`, use header override.
- `camel-fhir` default version is R4.

### **Main Class**
- Moved to `camel-main`; supports only one `CamelContext`.

### **Properties Component**
- Custom tokens removed; uses hardcoded `${}`.
- OS env vars override property values.
- No longer supports endpoint URIs; use `getPropertiesComponent()`.

### **EIPs and DSL Changes**
- `hystrix` renamed to `circuitBreaker`.
- `consumer.` prefix removed from endpoint options.
- Processor calls must use `bean:` or `process()`.
- Tracer updated; `BacklogTracer` not enabled by default.
- XML DSL: `headerName`/`propertyName` → `name`; `completionSize` → `completionSizeExpression`.
- `threads`, `delay`, `sample`, `throttle` collapsed into single elements.
- `<custom>` → `<customLoadBalancer>`; `<zipFile>` → `<zipfile>`.

### **Removed Features**
- `camel-script`, `javax.script`, `@OutHeaders`, OUT message in simple language and mock.
- Multiple route inputs removed.
- `includeRoutes` on `RouteBuilder` removed.
- `@FallbackConverter` replaced with `@Converter(fallback=true)`.

### **JMX and Tracing**
- JMX APIs for explaining EIPs/components removed.
- JMX events moved to `CamelEvent` subclasses.

### **Component-Specific Changes**
- `camel-jetty` producer removed.
- `camel-xslt` split into `camel-xslt` and `camel-xslt-saxon`.
- `camel-sql` requires `version` column for optimistic locking.
- `camel-activemq` moved to Camel; package changed.
- `camel-zipdeflater` replaces `zip`/`gzip` dataformats.

### **API Refactoring**
- Many classes moved from `org.apache.camel.impl` to `org.apache.camel.support` or `org.apache.camel.impl.engine`.
- `CamelContext` catalog methods moved to `CatalogCamelContext`.
- `ModelCamelContext`, `ExtendedCamelContext` used for advanced APIs.
- Exceptions now mostly unchecked.
- `Service` lifecycle methods no longer throw checked exceptions.

### **Testing**
- `adviceWith` usage changed; prefer lambda style.
- `camel-test`: use `bind()` instead of `createRegistry()`.

### **Route Control**
- Route control methods moved to `RouteController`.

### **Security**
- Default algorithms updated: Crypto → SHA256withRSA, XML Security → RSA-SHA256, AES-256-GCM.
- Shiro requires explicit key/passphrase.

### **Language and Registry**
- Simple language: `property` → `exchangeProperty`.
- `terser` renamed to `hl7terser`.
- Registry: use `DefaultRegistry`, `bind()` instead of `put()`.

### **Helpers and Utilities**
- All helper classes moved to `camel-support`.
- `ObjectHelper` split between `camel-support` and `camel-util`.

### **Idempotent Repositories**
- Moved to `camel-support`.

### **Aggregation**
- `XsltAggregationStrategy` moved to `camel-xslt`.
- `Exchange.GROUPED_EXCHANGE` deprecated.

### **Maven Plugins**
- Split into `camel-maven-plugin` (run goal) and `camel-report-maven-plugin` (validate, coverage).

### **Known Issues**
- MDC logging and `camel-zipkin` may misbehave with async routing.