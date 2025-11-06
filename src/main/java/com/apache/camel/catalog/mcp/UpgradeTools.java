package com.apache.camel.catalog.mcp;

import com.felipestanzani.jtoon.JToon;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Singleton;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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

    // List of all available upgrade guide versions
    private static final List<String> UPGRADE_VERSIONS = Arrays.asList(
            "3.0", "3.1", "3.2", "3.3", "3.4", "3.5", "3.6", "3.7", "3.8", "3.9",
            "3.10", "3.11", "3.12", "3.13", "3.14", "3.15", "3.16", "3.17", "3.18", "3.19",
            "3.20", "3.21", "3.22",
            "4.0", "4.1", "4.2", "4.3", "4.4", "4.5", "4.6", "4.7", "4.8", "4.9",
            "4.10", "4.11", "4.12", "4.13", "4.14", "4.15", "4.16"
    );

    @Tool(description = "Lists all available Apache Camel upgrade guide versions. Use this to see which upgrade guides are available for migration between Camel versions.")
    public ToolResponse getAvailableUpgradeVersions() {
        JsonObject reply = new JsonObject();
        JsonArray versionsArray = new JsonArray();

        for (String version : UPGRADE_VERSIONS) {
            versionsArray.add(version);
        }

        reply.put("versions", versionsArray);
        reply.put("count", UPGRADE_VERSIONS.size());

        return ToolResponse.success(JToon.encodeJson(reply.toString()));
    }

    @Tool(description = "Camel Upgrade guide documentation for a specific Apache Camel version. Use this when you need detailed information about upgrading to or from a specific Camel version.")
    public ToolResponse getUpgradeGuideDocumentation(
            @ToolArg(description = "The version of the upgrade guide to fetch. For example: '3.1', '4.0', '4.9'. Use getAvailableUpgradeVersions to see all available versions.") String version) {

        // Validate the version
        if (!UPGRADE_VERSIONS.contains(version)) {
            return ToolResponse.error("Invalid version: " + version + ". Use getAvailableUpgradeVersions to see available versions.");
        }

        // Build the documentation URL based on the version
        String url = buildUpgradeGuideUrl(version);

        try {
            // Fetch the HTML documentation
            Document doc = Jsoup.connect(url).get();

            // Extract the content - prefer <article> tag if present, otherwise use full HTML
            String htmlContent;
            Element articleElement = doc.selectFirst("article");
            if (articleElement != null) {
                htmlContent = articleElement.html();
            } else {
                htmlContent = doc.html();
            }

            // Convert HTML to Markdown
            FlexmarkHtmlConverter converter = FlexmarkHtmlConverter.builder().build();
            String markdown = converter.convert(htmlContent);

            // Clean up the markdown
            markdown = cleanupMarkdown(markdown);

            // Return the markdown documentation
            JsonObject reply = new JsonObject();
            reply.put("version", version);
            reply.put("documentationUrl", url);
            reply.put("markdown", markdown);

            return ToolResponse.success(reply.toString());

        } catch (IOException e) {
            return ToolResponse.error("Failed to fetch upgrade guide from " + url + ": " + e.getMessage());
        } catch (Exception e) {
            return ToolResponse.error("Failed to convert upgrade guide to markdown: " + e.getMessage());
        }
    }

    /**
     * Builds the URL for the upgrade guide based on the version
     */
    private String buildUpgradeGuideUrl(String version) {
        String[] parts = version.split("\\.");
        String majorVersion = parts[0];

        if (version.equals("3.0")) {
            return "https://camel.apache.org/manual/camel-3-migration-guide.html";
        } else if (version.equals("4.0")) {
            return "https://camel.apache.org/manual/camel-4-migration-guide.html";
        } else if (majorVersion.equals("3")) {
            String minorVersion = parts[1];
            return "https://camel.apache.org/manual/camel-3x-upgrade-guide-3_" + minorVersion + ".html";
        } else if (majorVersion.equals("4")) {
            String minorVersion = parts[1];
            return "https://camel.apache.org/manual/camel-4x-upgrade-guide-4_" + minorVersion + ".html";
        }

        throw new IllegalArgumentException("Unsupported version: " + version);
    }

    /**
     * Cleans up markdown by removing excessive whitespace and formatting issues
     */
    private String cleanupMarkdown(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return markdown;
        }

        // Remove consecutive blank lines (more than 2 newlines in a row)
        // Keep maximum of 2 newlines (one blank line between sections)
        markdown = markdown.replaceAll("\n{3,}", "\n\n");

        // Remove consecutive spaces (more than 2 spaces)
        // Preserve double spaces at end of lines for markdown line breaks
        markdown = markdown.replaceAll("(?<!\\n) {3,}", " ");

        // Clean up spaces before newlines
        markdown = markdown.replaceAll(" +\n", "\n");

        // Clean up tabs and replace with spaces
        markdown = markdown.replaceAll("\t", "    ");

        // Remove trailing whitespace from the entire document
        markdown = markdown.trim();

        return markdown;
    }

    @Tool(description = "Lists all component versions compatible with a given Apache Camel release")
    public ToolResponse getCompatibleVersions(
            @ToolArg(description = "The Camel release version (e.g., '4.15.0', '4.14.0') or 'main' for the latest development version.") String release) {

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
