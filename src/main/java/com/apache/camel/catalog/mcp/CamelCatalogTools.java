package com.apache.camel.catalog.mcp;

import com.felipestanzani.jtoon.JToon;
import io.quarkiverse.mcp.server.McpLog;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.camel.catalog.CamelCatalog;
import org.apache.camel.catalog.EndpointValidationResult;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class CamelCatalogTools {

    @Inject
    private CamelCatalog camelCatalog;

    public CamelCatalogTools(CamelCatalog camelCatalog) {
        this.camelCatalog = camelCatalog;
    }

    /**
     * Retrieves all available component names from the Camel catalog.
     *
     * @param filter Optional filter string to match component names (case-insensitive). If provided, only components containing this string will be returned.
     * @param log MCP logging instance for tracking tool invocations
     * @return List of all component names available in the catalog, optionally filtered
     */
    @Tool(name = "findComponentNames",
          description = "Discovers all available Apache Camel component names in the catalog. Use this to explore what components are available.")
    public String findComponentNames(
            @ToolArg(description = "Optional filter string to match component names (case-insensitive). Only components containing this string will be returned. Leave empty to get all components.", required = false)
            String filter,
            McpLog log) {
        log.info("Fetching component names from catalog" + (filter != null && !filter.trim().isEmpty() ? " with filter: '" + filter + "'" : ""));

        try {
            List<String> componentNames = camelCatalog.findComponentNames();

            // Apply filter if provided
            if (filter != null && !filter.trim().isEmpty()) {
                String filterLower = filter.trim().toLowerCase();
                componentNames = componentNames.stream()
                    .filter(name -> name.toLowerCase().contains(filterLower))
                    .collect(Collectors.toList());
                log.info("Successfully retrieved %s component name(s) matching filter '%s'", componentNames.size(), filter);
            } else {
                log.info("Successfully retrieved %s component name(s)", componentNames.size());
            }

            return JToon.encode(componentNames);
        } catch (Exception e) {
            log.error("Error retrieving component names: %s", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve component names", e);
        }
    }

    /**
     * Retrieves the JSON schema for a specific Camel data format, including all available
     * properties, options, and configuration details.
     *
     * @param dataFormatName Name of the Camel data format
     * @param log MCP logging instance for tracking tool invocations
     * @return JSON schema string describing the data format's configuration options
     */
    @Tool(name = "dataFormatProperties",
          description = "Retrieves the complete JSON schema for a specific Apache Camel data format, including all configurable properties.")
    public String dataFormatProperties(
            @ToolArg(description = "The name of the Camel data format to query (e.g., 'json', 'xml', 'csv', 'avro').")
            String dataFormatName,
            McpLog log) {

        if (dataFormatName == null || dataFormatName.trim().isEmpty()) {
            log.error("Data format name cannot be null or empty");
            throw new IllegalArgumentException("Data format name is required");
        }

        log.info("Retrieving data format schema for: '%s'", dataFormatName);

        try {
            String schema = camelCatalog.dataFormatJSonSchema(dataFormatName);

            if (schema == null || schema.isEmpty()) {
                log.debug("No schema found for data format: '%s'", dataFormatName);
                throw new IllegalArgumentException("Data format '" + dataFormatName + "' not found in catalog. Please verify the data format name.");
            }

            log.info("Successfully retrieved schema for data format '%s' (%s characters)",
                     dataFormatName, schema.length());
            log.debug("Schema preview: %s", schema.substring(0, Math.min(100, schema.length())) + "...");

            return JToon.encodeJson(schema);
        } catch (IllegalArgumentException e) {
            // Re-throw validation exceptions
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving schema for data format '%s': %s", dataFormatName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve data format schema for '" + dataFormatName + "'", e);
        }
    }

    /**
     * Retrieves all available data format names from the Camel catalog.
     *
     * @param log MCP logging instance for tracking tool invocations
     * @return List of all data format names available in the catalog
     */
    @Tool(name = "findDataFormatNames",
          description = "Discovers all available Apache Camel data format names in the catalog. Use this to explore available data transformation formats for message processing.")
    public String findDataFormatNames(McpLog log) {
        log.info("Fetching all data format names from catalog");

        try {
            List<String> dataFormatNames = camelCatalog.findDataFormatNames();
            log.info("Successfully retrieved %s data format name(s)", dataFormatNames != null ? dataFormatNames.size() : 0);
            return JToon.encode(dataFormatNames);
        } catch (Exception e) {
            log.error("Error retrieving data format names: %s", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve data format names", e);
        }
    }

    /**
     * Retrieves the JSON schema for a specific Camel language, including all available
     * properties, options, and configuration details.
     *
     * @param languageName Name of the Camel language
     * @param log MCP logging instance for tracking tool invocations
     * @return JSON schema string describing the language's configuration options
     */
    @Tool(name = "languageProperties",
          description = "Retrieves the complete JSON schema for a specific Apache Camel expression language.")
    public String languageProperties(
            @ToolArg(description = "The name of the Camel language to query (e.g., 'simple', 'xpath', 'jsonpath', 'groovy').")
            String languageName,
            McpLog log) {

        if (languageName == null || languageName.trim().isEmpty()) {
            log.error("Language name cannot be null or empty");
            throw new IllegalArgumentException("Language name is required");
        }

        log.info("Retrieving language schema for: '%s'", languageName);

        try {
            String schema = camelCatalog.languageJSonSchema(languageName);

            if (schema == null || schema.isEmpty()) {
                log.debug("No schema found for language: '%s'", languageName);
                throw new IllegalArgumentException("Language '" + languageName + "' not found in catalog. Please verify the language name.");
            }

            log.info("Successfully retrieved schema for language '%s' (%s characters)",
                     languageName, schema.length());
            log.debug("Schema preview: %s", schema.substring(0, Math.min(100, schema.length())) + "...");

            return JToon.encodeJson(schema);
        } catch (IllegalArgumentException e) {
            // Re-throw validation exceptions
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving schema for language '%s': %s", languageName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve language schema for '" + languageName + "'", e);
        }
    }

    /**
     * Retrieves all available language names from the Camel catalog.
     *
     * @param log MCP logging instance for tracking tool invocations
     * @return List of all language names available in the catalog
     */
    @Tool(name = "findLanguageNames",
          description = "Discovers all available Apache Camel expression language names in the catalog.")
    public String findLanguageNames(McpLog log) {
        log.info("Fetching all language names from catalog");

        try {
            List<String> languageNames = camelCatalog.findLanguageNames();
            log.info("Successfully retrieved %s language name(s)", languageNames != null ? languageNames.size() : 0);
            return JToon.encode(languageNames);
        } catch (Exception e) {
            log.error("Error retrieving language names: %s", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve language names", e);
        }
    }

    /**
     * Retrieves the JSON schema for a specific Camel EIP model (pattern), including all available
     * properties, options, and configuration details.
     *
     * @param modelName Name of the Camel EIP model
     * @param log MCP logging instance for tracking tool invocations
     * @return JSON schema string describing the EIP model's configuration options
     */
    @Tool(name = "modelProperties",
          description = "Retrieves the complete JSON schema for a specific Apache Camel EIP (Enterprise Integration Pattern) model, including all configurable properties, data types, default values, and documentation. EIP patterns define routing and mediation rules (e.g., choice, split, aggregate, enrich, multicast). Use this to explore EIP configuration options and understand how to properly configure integration patterns in your routes.")
    public String modelProperties(
            @ToolArg(description = "The name of the Camel EIP model to query (e.g., 'choice', 'split', 'aggregate', 'multicast', 'enrich').")
            String modelName,
            McpLog log) {

        if (modelName == null || modelName.trim().isEmpty()) {
            log.error("Model name cannot be null or empty");
            throw new IllegalArgumentException("Model name is required");
        }

        log.info("Retrieving model schema for: '%s'", modelName);

        try {
            String schema = camelCatalog.modelJSonSchema(modelName);

            if (schema == null || schema.isEmpty()) {
                log.debug("No schema found for model: '%s'", modelName);
                throw new IllegalArgumentException("Model '" + modelName + "' not found in catalog. Please verify the model name.");
            }

            log.info("Successfully retrieved schema for model '%s' (%s characters)",
                     modelName, schema.length());
            log.debug("Schema preview: %s", schema.substring(0, Math.min(100, schema.length())) + "...");

            return JToon.encodeJson(schema);
        } catch (IllegalArgumentException e) {
            // Re-throw validation exceptions
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving schema for model '%s': %s", modelName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve model schema for '" + modelName + "'", e);
        }
    }

    /**
     * Retrieves all available model (EIP pattern) names from the Camel catalog.
     *
     * @param log MCP logging instance for tracking tool invocations
     * @return List of all EIP model names available in the catalog
     */
    @Tool(name = "findModelNames",
          description = "Discovers all available Apache Camel EIP (Enterprise Integration Pattern) model names in the catalog. EIP patterns define routing and mediation rules (e.g., choice, split, aggregate, enrich, multicast). Use this to explore available integration patterns for building Camel routes.")
    public String findModelNames(McpLog log) {
        log.info("Fetching all model (EIP) names from catalog");

        try {
            List<String> modelNames = camelCatalog.findModelNames();
            log.info("Successfully retrieved %s model name(s)", modelNames != null ? modelNames.size() : 0);
            return JToon.encode(modelNames);
        } catch (Exception e) {
            log.error("Error retrieving model names: %s", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve model names", e);
        }
    }

    /**
     * Retrieves the JSON schema for a specific Camel transformer, including all available
     * properties, options, and configuration details.
     *
     * @param transformerName Name of the Camel transformer
     * @param log MCP logging instance for tracking tool invocations
     * @return JSON schema string describing the transformer's configuration options
     */
    @Tool(name = "transformerProperties",
          description = "Retrieves the complete JSON schema for a specific Apache Camel transformer, including all configurable properties, data types, default values, and documentation. Transformers handle data type conversions and message transformations between different formats. Use this to explore transformer configuration options and understand how to properly configure data transformations in your routes.")
    public String transformerProperties(
            @ToolArg(description = "The name of the Camel transformer to query.")
            String transformerName,
            McpLog log) {

        if (transformerName == null || transformerName.trim().isEmpty()) {
            log.error("Transformer name cannot be null or empty");
            throw new IllegalArgumentException("Transformer name is required");
        }

        log.info("Retrieving transformer schema for: '%s'", transformerName);

        try {
            String schema = camelCatalog.transformerJSonSchema(transformerName);

            if (schema == null || schema.isEmpty()) {
                log.debug("No schema found for transformer: '%s'", transformerName);
                throw new IllegalArgumentException("Transformer '" + transformerName + "' not found in catalog. Please verify the transformer name.");
            }

            log.info("Successfully retrieved schema for transformer '%s' (%s characters)",
                     transformerName, schema.length());
            log.debug("Schema preview: %s", schema.substring(0, Math.min(100, schema.length())) + "...");

            return JToon.encodeJson(schema);
        } catch (IllegalArgumentException e) {
            // Re-throw validation exceptions
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving schema for transformer '%s': %s", transformerName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve transformer schema for '" + transformerName + "'", e);
        }
    }

    /**
     * Retrieves all available transformer names from the Camel catalog.
     *
     * @param log MCP logging instance for tracking tool invocations
     * @return List of all transformer names available in the catalog
     */
    @Tool(name = "findTransformerNames",
          description = "Discovers all available Apache Camel transformer names in the catalog. Transformers handle data type conversions and message transformations between different formats. Use this to explore available transformation capabilities for data conversion in routes.")
    public String findTransformerNames(McpLog log) {
        log.info("Fetching all transformer names from catalog");

        try {
            List<String> transformerNames = camelCatalog.findTransformerNames();
            log.info("Successfully retrieved %s transformer name(s)", transformerNames != null ? transformerNames.size() : 0);

            return JToon.encode(transformerNames);
        } catch (Exception e) {
            log.error("Error retrieving transformer names: %s", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve transformer names", e);
        }
    }

    /**
     * Validates endpoint URI properties and returns detailed validation results.
     *
     * @param uri The Camel endpoint URI to validate
     * @param log MCP logging instance for tracking tool invocations
     * @return EndpointValidationResult containing validation status, errors, and parsed properties
     */
    @Tool(name = "validateEndpointProperties",
          description = "Validates a Camel endpoint URI and returns comprehensive validation results including any configuration errors, unknown properties, and property type mismatches. Use this to catch configuration errors before runtime. Example: 'kafka:my-topic?brokers=localhost:9092' - helps ensure endpoint URIs are correctly formed and all properties are valid.")
    public String validateEndpointProperties(
            @ToolArg(description = "The Camel endpoint URI to validate (e.g., 'kafka:my-topic?brokers=localhost:9092', 'file:/data/inbox?delay=5000').")
            String uri,
            McpLog log) {

        if (uri == null || uri.trim().isEmpty()) {
            log.error("Endpoint URI cannot be null or empty");
            throw new IllegalArgumentException("Endpoint URI is required");
        }

        log.info("Validating endpoint URI: '%s'", uri);

        try {
            EndpointValidationResult result = camelCatalog.validateEndpointProperties(uri);

            if (result.hasErrors()) {
                log.info("Validation found errors for URI '%s'", uri);
            } else {
                log.info("Endpoint URI '%s' validated successfully", uri);
            }

            return JToon.encode(result);
        } catch (Exception e) {
            log.error("Error validating endpoint URI '%s': %s", uri, e.getMessage(), e);
            throw new RuntimeException("Failed to validate endpoint URI: '" + uri + "'", e);
        }
    }

    /**
     * Parses an endpoint URI and extracts all properties as key-value pairs.
     *
     * @param uri The Camel endpoint URI to parse
     * @param log MCP logging instance for tracking tool invocations
     * @return Map of property names to values extracted from the URI
     */
    @Tool(name = "endpointProperties",
          description = "Parses a Camel endpoint URI and extracts all configuration properties as key-value pairs. This is useful for understanding what properties are configured in an endpoint URI or for programmatically inspecting endpoint configurations. Example: parsing 'kafka:my-topic?brokers=localhost:9092&groupId=mygroup' returns a map with brokers, groupId, and other properties.")
    public String endpointProperties(
            @ToolArg(description = "The Camel endpoint URI to parse (e.g., 'kafka:my-topic?brokers=localhost:9092', 'timer:tick?period=1000').")
            String uri,
            McpLog log) {

        if (uri == null || uri.trim().isEmpty()) {
            log.error("Endpoint URI cannot be null or empty");
            throw new IllegalArgumentException("Endpoint URI is required");
        }

        log.info("Parsing endpoint properties from URI: '%s'", uri);

        try {
            Map<String, String> properties = camelCatalog.endpointProperties(uri);
            log.info("Successfully parsed %s propert(ies) from URI '%s'",
                    properties != null ? properties.size() : 0, uri);
            return JToon.encode(properties);
        } catch (URISyntaxException e) {
            log.error("Invalid URI syntax for '%s': %s", uri, e.getMessage(), e);
            throw new RuntimeException("Invalid endpoint URI syntax: '" + uri + "' - " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error parsing endpoint properties from URI '%s': %s", uri, e.getMessage(), e);
            throw new RuntimeException("Failed to parse endpoint properties from URI: '" + uri + "'", e);
        }
    }

    /**
     * Extracts the component name (scheme) from a Camel endpoint URI.
     *
     * @param uri The Camel endpoint URI
     * @param log MCP logging instance for tracking tool invocations
     * @return The component name/scheme from the URI, or null if it cannot be determined
     */
    @Tool(name = "endpointComponentName",
          description = "Extracts the component name (also called scheme) from a Camel endpoint URI. The component name is the part before the colon. For example, from 'kafka:my-topic?brokers=localhost:9092' it returns 'kafka', from 'file:/data/inbox' it returns 'file'. This is useful for identifying which Camel component an endpoint URI uses.")
    public String endpointComponentName(
            @ToolArg(description = "The Camel endpoint URI to extract the component name from (e.g., 'kafka:my-topic', 'jms:queue:orders').")
            String uri,
            McpLog log) {

        if (uri == null || uri.trim().isEmpty()) {
            log.error("Endpoint URI cannot be null or empty");
            throw new IllegalArgumentException("Endpoint URI is required");
        }

        log.info("Extracting component name from URI: '%s'", uri);

        try {
            String componentName = camelCatalog.endpointComponentName(uri);

            if (componentName == null || componentName.isEmpty()) {
                log.info("Could not determine component name from URI: '%s'", uri);
                return null;
            }

            log.info("Extracted component name '%s' from URI '%s'", componentName, uri);
            return JToon.encode(componentName);
        } catch (Exception e) {
            log.error("Error extracting component name from URI '%s': %s", uri, e.getMessage(), e);
            throw new RuntimeException("Failed to extract component name from URI: '" + uri + "'", e);
        }
    }

    /**
     * Gets the version of the Camel catalog itself.
     *
     * @param log MCP logging instance for tracking tool invocations
     * @return The Camel catalog version string
     */
    @Tool(name = "getCatalogVersion",
          description = "Returns the version of the Camel catalog being used. This represents the version of the catalog metadata and schemas, which typically corresponds to a Camel release version. Useful for understanding what Camel version information is available in the catalog.")
    public String getCatalogVersion(McpLog log) {
        log.info("Retrieving catalog version");

        try {
            String version = camelCatalog.getCatalogVersion();
            log.info("Catalog version: '%s'", version);
            return version;
        } catch (Exception e) {
            log.error("Error retrieving catalog version: %s", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve catalog version", e);
        }
    }

    /**
     * Gets the currently loaded Camel version in the catalog.
     *
     * @param log MCP logging instance for tracking tool invocations
     * @return The currently loaded Camel version string
     */
    @Tool(name = "getLoadedVersion",
          description = "Returns the currently loaded Camel version in the catalog. If a specific version was loaded using loadVersion(), this returns that version. Otherwise, it returns the default version that was loaded. This can differ from getCatalogVersion() if a different Camel version was dynamically loaded into the catalog.")
    public String getLoadedVersion(McpLog log) {
        log.info("Retrieving loaded Camel version");

        try {
            String version = camelCatalog.getLoadedVersion();
            log.info("Loaded Camel version: '%s'", version);
            return version;
        } catch (Exception e) {
            log.error("Error retrieving loaded version: %s", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve loaded version", e);
        }
    }

}
