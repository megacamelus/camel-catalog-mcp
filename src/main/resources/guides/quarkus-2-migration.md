### Camel Quarkus 2.x Migration guide

#### Quarkus Core Changes
- Observability endpoints moved:
  - `/health` → `/q/health`
  - `/metrics` → `/q/metrics`

#### Artifact Changes
- `camel-quarkus-main`: **Removed**. Use any other extension (they transitively include `camel-quarkus-core`).
- `camel-quarkus-xml-io`: **Replaced** by `camel-quarkus-xml-io-dsl`.

#### Configuration Changes
- Native resources:
  - `quarkus.camel.native.resources.include-patterns` → `quarkus.native.resources.includes`
  - `quarkus.camel.native.resources.exclude-patterns` → `quarkus.native.resources.excludes`
- SQL scripts:
  - `quarkus.camel.sql.script-files`: **Deprecated in 2.0.0**, **removed in 2.1.0**
  - Use `quarkus.native.resources.includes` instead.
- `quarkus.camel.main.enabled`: **Removed** in 2.7.0 (Camel Main is always enabled).
- FHIR config (2.8.0):
  - Only R4 enabled by default.
  - Enable others via:
    ```properties
    quarkus.camel.fhir.enable-dstu2=true
    quarkus.camel.fhir.enable-r4=false
    ```

#### Component Behavior Changes
- gRPC (2.2.0): `camel-grpc` consumers now respect their own config (no longer tied to `quarkus-grpc`).
- Quartz (2.11.0): Clustering config simplified—use Quarkus Quartz standard method.

#### Graceful Shutdown (2.6.0)
- Now **enabled by default** (except in dev mode without timeout).
- Example:
  ```properties
  camel.main.shutdownTimeout = 15
  ```

#### Removed Extensions

**In 2.7.0:**
- `camel-vertx-kafka`

**In 2.8.0:**
- `camel-weka`
- `camel-ipfs`

**In 2.10.0:**
- `camel-ahc`
- `camel-ahc-ws`
- `camel-atomix`
- `camel-beanstalk`
- `camel-beanio`
- `camel-etcd`
- `camel-elsql`
- `camel-ganglia`
- `camel-nsq`
- `camel-hystrix`
- `camel-jing`
- `camel-msv`
- `camel-nagios`
- `camel-ribbon`
- `camel-sip`
- `camel-soroush`
- `camel-spark`
- `camel-tagsoup`
- `camel-yammer`

#### Removed Annotation
- `@BuildTimeAvroDataFormat`: **Removed** in 2.7.0. Use build-time schema parsing via Avro extension.