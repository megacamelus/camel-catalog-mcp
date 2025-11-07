package com.apache.camel.catalog.mcp;

import com.felipestanzani.jtoon.JToon;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolResponse;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Singleton;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

@Singleton
public class UpgradeTools {

    @Tool(description = "Camel Upgrade guide documentation for a major Apache Camel version. Use this when you need detailed information about upgrading to or from a specific Camel version.")
    public ToolResponse getCamelUpgradeGuideDocumentation(
            @ToolArg(description = "The major version of the upgrade guide to fetch. For example: '3' or '4'. Use null or empty string to retrieve all the upgrade guides.", required = false) String version) {
        io.quarkus.logging.Log.infof("Tool invoked: getCamelUpgradeGuideDocumentation(version=%s)", version);

        try {
            // If version is null or empty, return all guides merged by major version
            if (version == null || version.isEmpty()) {
                JsonObject allGuides = new JsonObject();

                // Merge Camel 3 guides
                String camel3Content = mergeGuides(
                        "camel-3-migration-guide.md",
                        "camel-3x-migration-guide.md"
                );
                if (camel3Content != null) {
                    allGuides.put("camel-3-migration-guide", camel3Content);
                }

                // Merge Camel 4 guides
                String camel4Content = mergeGuides(
                        "camel-4-migration-guide.md",
                        "camel-4x-migration-guide.md"
                );
                if (camel4Content != null) {
                    allGuides.put("camel-4-migration-guide", camel4Content);
                }

                return ToolResponse.success(allGuides.toString());
            }

            // Normalize version input
            String normalizedVersion = version.trim();
            String mergedContent = null;

            // Map version to merged guides
            if (normalizedVersion.equals("3")) {
                mergedContent = mergeGuides(
                        "camel-3-migration-guide.md",
                        "camel-3x-migration-guide.md"
                );
            } else if (normalizedVersion.equals("4")) {
                mergedContent = mergeGuides(
                        "camel-4-migration-guide.md",
                        "camel-4x-migration-guide.md"
                );
            } else {
                return ToolResponse.error("Unknown version '" + version + "'. Available versions: 3, 4. Use empty string to get all guides.");
            }

            if (mergedContent == null) {
                return ToolResponse.error("Failed to load guides for version '" + version + "'.");
            }

            JsonObject result = new JsonObject();
            result.put("camel-" + normalizedVersion + "-migration-guide", mergedContent);

            return ToolResponse.success(result.toString());
        } catch (Exception e) {
            return ToolResponse.error("Failed to load upgrade guide: " + e.getMessage());
        }
    }

    @Tool(description = "Quarkus Upgrade guide documentation for major Quarkus versions. Use this when you need detailed information about upgrading Quarkus to or from a specific version.")
    public ToolResponse getQuarkusUpgradeGuideDocumentation(
            @ToolArg(description = "The major version of the upgrade guide to fetch. For example: '2' or '3'. Use null or empty string to retrieve all the upgrade guides.", required = false) String version) {
        io.quarkus.logging.Log.infof("Tool invoked: getQuarkusUpgradeGuideDocumentation(version=%s)", version);

        try {
            // Define available Quarkus upgrade guides
            List<String> quarkusGuides = Arrays.asList(
                    "quarkus-2-migration.md",
                    "quarkus-3-migration.md"
            );

            // If version is null or empty, return all guides
            if (version == null || version.isEmpty()) {
                JsonObject allGuides = new JsonObject();
                for (String guide : quarkusGuides) {
                    String content = loadGuideFromResource(guide);
                    if (content != null) {
                        allGuides.put(guide.replace(".md", ""), content);
                    }
                }
                return ToolResponse.success(allGuides.toString());
            }

            // Normalize version input
            String normalizedVersion = version.trim();
            String guideFile = null;

            // Map version to guide file
            if (normalizedVersion.equals("2")) {
                guideFile = "quarkus-2-migration.md";
            } else if (normalizedVersion.equals("3")) {
                guideFile = "quarkus-3-migration.md";
            } else {
                return ToolResponse.error("Unknown version '" + version + "'. Available versions: 2, 3. Use empty string to get all guides.");
            }

            String content = loadGuideFromResource(guideFile);
            if (content == null) {
                return ToolResponse.error("Guide file '" + guideFile + "' not found in resources.");
            }

            JsonObject result = new JsonObject();
            result.put(guideFile.replace(".md", ""), content);

            return ToolResponse.success(result.toString());
        } catch (Exception e) {
            return ToolResponse.error("Failed to load upgrade guide: " + e.getMessage());
        }
    }

    private String mergeGuides(String... guideFiles) {
        StringBuilder merged = new StringBuilder();
        for (String guideFile : guideFiles) {
            String content = loadGuideFromResource(guideFile);
            if (content != null) {
                if (merged.length() > 0) {
                    merged.append("\n\n---\n\n");
                }
                merged.append(content);
            }
        }
        return merged.length() > 0 ? merged.toString() : null;
    }

    private String loadGuideFromResource(String fileName) {
        try {
            var inputStream = getClass().getClassLoader().getResourceAsStream("guides/" + fileName);
            if (inputStream == null) {
                return null;
            }
            return new String(inputStream.readAllBytes());
        } catch (IOException e) {
            return null;
        }
    }

    @Tool(description = "Lists all component versions compatible with a given Apache Camel release")
    public ToolResponse getCompatibleVersions(
            @ToolArg(description = "The Camel release version (e.g., '4.15.0', '4.14.0') or 'main' for the latest development version.") String release) {
        io.quarkus.logging.Log.infof("Tool invoked: getCompatibleVersions(release=%s)", release);

        if (release == null || release.isEmpty()) {
            release = "main";
        }
        try {
            // Build the GitHub raw URL for the parent pom.xml
            String url;
            if ("main".equalsIgnoreCase(release)) {
                url = "https://raw.githubusercontent.com/apache/camel/main/parent/pom.xml";
            } else {
                url = "https://raw.githubusercontent.com/apache/camel/refs/tags/camel-" + release + "/parent/pom.xml";
            }

            // Fetch the pom.xml content
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return ToolResponse.error("Failed to fetch parent pom.xml from " + url + ". Status code: " + response.statusCode());
            }

            String xmlContent = response.body();

            // Parse the XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(new ByteArrayInputStream(xmlContent.getBytes()));

            // Extract properties
            JsonObject properties = new JsonObject();
            org.w3c.dom.NodeList propertiesNodes = doc.getElementsByTagName("properties");

            if (propertiesNodes.getLength() > 0) {
                org.w3c.dom.Node propertiesNode = propertiesNodes.item(0);
                org.w3c.dom.NodeList propertyList = propertiesNode.getChildNodes();

                for (int i = 0; i < propertyList.getLength(); i++) {
                    org.w3c.dom.Node node = propertyList.item(i);
                    if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                        String propertyName = node.getNodeName();
                        String propertyValue = node.getTextContent();
                        properties.put(propertyName, propertyValue);
                    }
                }
            }

            return ToolResponse.success(JToon.encodeJson(properties.toString()));
        } catch (IOException e) {
            return ToolResponse.error("Failed to fetch parent pom.xml: " + e.getMessage());
        } catch (Exception e) {
            return ToolResponse.error("Failed to parse parent pom.xml: " + e.getMessage());
        }
    }
}
