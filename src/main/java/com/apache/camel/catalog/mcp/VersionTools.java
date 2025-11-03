package com.apache.camel.catalog.mcp;

import io.quarkiverse.mcp.server.McpLog;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.camel.catalog.CamelCatalog;
import org.apache.camel.catalog.DefaultCamelCatalog;
import org.apache.camel.tooling.model.ReleaseModel;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class VersionTools {

    @Inject
    private CamelCatalog camelCatalog;

    public VersionTools(CamelCatalog camelCatalog) {
        this.camelCatalog = camelCatalog;
    }

    /**
     * Retrieves available Camel Quarkus releases from the catalog, ordered by version (latest first).
     *
     * @param limit Maximum number of releases to return (optional, defaults to all)
     * @param offset Number of releases to skip from the beginning (optional, defaults to 0)
     * @param log MCP logging instance for tracking tool invocations
     * @return List of ReleaseModel objects containing version information and metadata, sorted by version (latest first)
     */
    @Tool(name = "camelQuarkusReleases",
            description = "Retrieves a list of Apache Camel Quarkus releases ordered by version (latest first), including version numbers, release dates, and compatibility information. Supports pagination via limit and offset parameters. Use this to discover available Camel Quarkus versions for migration planning or version compatibility checks.")
    public List<ReleaseModel> camelQuarkusReleases(
            @ToolArg(description = "Maximum number of releases to return (optional, defaults to all releases)")
            Integer limit,
            @ToolArg(description = "Number of releases to skip from the beginning (optional, defaults to 0)")
            Integer offset,
            McpLog log) {

        int actualOffset = offset != null ? Math.max(0, offset) : 0;
        log.info("Fetching Camel Quarkus releases from catalog (limit: %s, offset: %s)",
                limit != null ? limit : "all", actualOffset);

        try {
            List<ReleaseModel> releases = camelCatalog.camelQuarkusReleases();

            if (releases == null) {
                releases = Collections.emptyList();
            }

            int totalCount = releases.size();

            // Sort by version (latest first) and apply offset and limit using skip and limit
            var stream = releases.stream()
                    .sorted(VERSION_COMPARATOR)
                    .skip(actualOffset);

            if (limit != null && limit > 0) {
                stream = stream.limit(limit);
            }

            List<ReleaseModel> result = stream.collect(Collectors.toList());

            log.info("Successfully retrieved %s of %s Camel Quarkus release(s) (offset: %s, limit: %s)",
                    result.size(), totalCount, actualOffset, limit != null ? limit : "all");
            return result;
        } catch (Exception e) {
            log.error("Error retrieving Camel Quarkus releases: %s", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve Camel Quarkus releases", e);
        }
    }

    /**
     * Retrieves available Apache Camel releases from the catalog, ordered by version (latest first).
     *
     * @param limit Maximum number of releases to return (optional, defaults to all)
     * @param offset Number of releases to skip from the beginning (optional, defaults to 0)
     * @param log MCP logging instance for tracking tool invocations
     * @return List of ReleaseModel objects containing version information and metadata, sorted by version (latest first)
     */
    @Tool(name = "camelReleases",
            description = "Retrieves a list of Apache Camel releases ordered by version (latest first), including version numbers, release dates, and LTS (Long Term Support) information. Supports pagination via limit and offset parameters. Helpful for understanding the Camel version history and planning upgrades.")
    public List<ReleaseModel> camelReleases(
            @ToolArg(description = "Maximum number of releases to return (optional, defaults to all releases)")
            Integer limit,
            @ToolArg(description = "Number of releases to skip from the beginning (optional, defaults to 0)")
            Integer offset,
            McpLog log) {

        int actualOffset = offset != null ? Math.max(0, offset) : 0;
        log.info("Fetching Camel releases from catalog (limit: %s, offset: %s)",
                limit != null ? limit : "all", actualOffset);

        try {
            List<ReleaseModel> releases = camelCatalog.camelReleases();

            if (releases == null) {
                releases = Collections.emptyList();
            }

            int totalCount = releases.size();

            // Sort by version (latest first) and apply offset and limit using skip and limit
            var stream = releases.stream()
                    .sorted(VERSION_COMPARATOR)
                    .skip(actualOffset);

            if (limit != null && limit > 0) {
                stream = stream.limit(limit);
            }

            List<ReleaseModel> result = stream.collect(Collectors.toList());

            log.info("Successfully retrieved %s of %s Camel release(s) (offset: %s, limit: %s)",
                    result.size(), totalCount, actualOffset, limit != null ? limit : "all");
            return result;
        } catch (Exception e) {
            log.error("Error retrieving Camel releases: %s", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve Camel releases", e);
        }
    }

    /**
     * Comparator for semantic versioning that orders versions from latest to oldest.
     * Handles versions like "4.9.0", "4.10.0", "3.21.0", etc.
     */
    private static final Comparator<ReleaseModel> VERSION_COMPARATOR = (r1, r2) -> {
        String v1 = r1.getVersion();
        String v2 = r2.getVersion();

        if (v1 == null && v2 == null) return 0;
        if (v1 == null) return 1;
        if (v2 == null) return -1;

        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");

        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            int num1 = i < parts1.length ? parseVersionPart(parts1[i]) : 0;
            int num2 = i < parts2.length ? parseVersionPart(parts2[i]) : 0;

            if (num1 != num2) {
                // Reverse order for latest first
                return Integer.compare(num2, num1);
            }
        }
        return 0;
    };

    /**
     * Parses a version part, extracting numeric portion and ignoring qualifiers like "-SNAPSHOT"
     */
    private static int parseVersionPart(String part) {
        try {
            // Handle parts like "0-SNAPSHOT" or "1-RC1"
            int dashIndex = part.indexOf('-');
            if (dashIndex > 0) {
                part = part.substring(0, dashIndex);
            }
            return Integer.parseInt(part);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

}
