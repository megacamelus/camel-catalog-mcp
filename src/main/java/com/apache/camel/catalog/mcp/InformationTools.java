package com.apache.camel.catalog.mcp;

import com.apache.camel.catalog.mcp.exceptions.ComponentNotFoundException;
import com.felipestanzani.jtoon.JToon;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.camel.catalog.CamelCatalog;
import org.apache.camel.tooling.model.ComponentModel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.List;

@Singleton
public class InformationTools {

    @Inject
    private CamelCatalog camelCatalog;

    public InformationTools(CamelCatalog camelCatalog) {
        this.camelCatalog = camelCatalog;
    }

    @Tool(description = "Fetches detailed documentation for a specific Apache Camel component. Use this to understand its purpose, support level, maven artifact, headers, and URI syntax.")
    public String getInformationAboutComponent(@ToolArg(description = "The scheme name of the component. For example: 'file', 'kafka', or 'jms'.") String componentName) {
        final ComponentModel componentModel;
        try {
            componentModel = findComponent(componentName);
        } catch (ComponentNotFoundException e) {
            return JToon.encode(e.getMessage());
        }

        JsonObject reply = new JsonObject();
        reply.put("kind", componentModel.getKind().toString());
        reply.put("name", componentModel.getName());
        reply.put("title", componentModel.getTitle());
        reply.put("description", componentModel.getDescription());
        reply.put("deprecatedSince", componentModel.getDeprecatedSince());
        reply.put("supportLevel", componentModel.getSupportLevel());
        reply.put("groupId", componentModel.getGroupId());
        reply.put("artifactId", componentModel.getArtifactId());
        reply.put("version", componentModel.getVersion());
        reply.put("syntax", componentModel.getSyntax());

        return JToon.encode(reply);
    }


    // This confuses the models, so have become an option of the getInfomrationAboutComponent
    private String getInformationAboutComponentOptions(String componentName) {
        final ComponentModel componentModel;
        try {
            componentModel = findComponent(componentName);
        } catch (ComponentNotFoundException e) {
            return JToon.encode(e.getMessage());
        }

        JsonObject reply = new JsonObject();
        JsonArray array = new JsonArray();
        final List<ComponentModel.ComponentOptionModel> componentOptions = componentModel.getComponentOptions();
        for (var option : componentOptions) {
            JsonObject optionObj = new JsonObject();
            optionObj.put("name", option.getName());
            optionObj.put("description", option.getDescription());

            array.add(optionObj);
        }

        reply.put("options", array);

        return JToon.encode(reply);
    }


    private ToolResponse getInformationAboutSpecificComponentOption(String componentName, String optionName) {
        final ComponentModel componentModel;
        try {
            componentModel = findComponent(componentName);
        } catch (ComponentNotFoundException e) {
            return ToolResponse.error(e.getMessage());
        }

        JsonObject reply = new JsonObject();
        final List<ComponentModel.ComponentOptionModel> componentOptions = componentModel.getComponentOptions();
        final ComponentModel.ComponentOptionModel option =
                componentOptions.stream().filter(c -> c.getName().equals(optionName)).findFirst().get();

        reply.put("name", option.getName());
        reply.put("description", option.getDescription());
        reply.put("kind", option.getKind());
        reply.put("type", option.getType());
        reply.put("defaultValue", option.getDefaultValue());

        return ToolResponse.success(reply.toString());
    }


    @Tool(description = "Lists all configurable options for a specific Apache Camel component. It can filter by type: 'component' properties or 'endpoint' URI parameters.")
    public String getInformationAboutOptions(@ToolArg(description = "The scheme name of the component. For example: 'file' or 'http'.") String componentName, @ToolArg(description = "The category of options to list: 'component' (bean properties) or 'endpoint' (URI parameters). Defaults to 'endpoint'.", defaultValue = "endpoint") String category) {
        if (category.equals("component")) {
            return getInformationAboutComponentOptions(componentName);
        }

        final ComponentModel componentModel;
        try {
            componentModel = findComponent(componentName);
        } catch (ComponentNotFoundException e) {
            return JToon.encode(e.getMessage());
        }

        JsonObject reply = new JsonObject();
        JsonArray array = new JsonArray();
        final List<ComponentModel.EndpointOptionModel> componentOptions = componentModel.getEndpointOptions();
        for (var option : componentOptions) {
            JsonObject optionObj = new JsonObject();
            optionObj.put("name", option.getName());
            optionObj.put("kind", option.getKind());
            optionObj.put("description", option.getDescription());

            array.add(optionObj);
        }

        reply.put("options", array);

        return JToon.encode(reply);
    }

    @Tool(description = "Fetches detailed properties of a single, named configuration option for an Apache Camel component. Can be filtered by category ('component' or 'endpoint'). It returns details like the option's data type, default value, and description.")
    public ToolResponse getInformationAboutSpecificOption(
            @ToolArg(description = "The scheme name of the component. For example: 'file', 'kafka', or 'jms'.") String componentName,
            @ToolArg(description = "The exact, case-sensitive name of the option to look up. For example: 'fileName' or 'bridgeErrorHandler'.") String optionName,
            @ToolArg(description = "The category of the option: 'component' for bean properties or 'endpoint' for URI parameters. Defaults to 'endpoint'.", defaultValue = "endpoint") String category) {

        if (category.equals("component")) {
            return getInformationAboutSpecificComponentOption(componentName, optionName);
        }

        final ComponentModel componentModel;
        try {
            componentModel = findComponent(componentName);
        } catch (ComponentNotFoundException e) {
            return ToolResponse.error(e.getMessage());
        }

        JsonObject reply = new JsonObject();
        final List<ComponentModel.EndpointOptionModel> componentOptions = componentModel.getEndpointOptions();
        final ComponentModel.EndpointOptionModel option =
                componentOptions.stream().filter(c -> c.getName().equals(optionName)).findFirst().get();

        reply.put("name", option.getName());
        reply.put("description", option.getDescription());
        reply.put("kind", option.getKind());
        reply.put("type", option.getType());
        reply.put("defaultValue", option.getDefaultValue());

        return ToolResponse.success(reply.toString());
    }

    @Tool(description = "Fetches the Maven and Gradle dependency snippets for a specific Apache Camel component. Use this to find the correct code to add to a project's build file.")
    public ToolResponse getDependency(
            @ToolArg(description = "The scheme name of the component. For example: 'file', 'kafka', or 'jms'.") String componentName) {
        final ComponentModel componentModel;
        try {
            componentModel = findComponent(componentName);
        } catch (ComponentNotFoundException e) {
            return ToolResponse.error(e.getMessage());
        }

        JsonObject reply = new JsonObject();
        reply.put("groupId", componentModel.getGroupId());
        reply.put("artifactId", componentModel.getArtifactId());
        reply.put("version", componentModel.getVersion());

        return ToolResponse.success(reply.toString());
    }

    private ComponentModel findComponent(String componentName) throws ComponentNotFoundException {
        final String adjustedComponentName = componentName.toLowerCase();

        ComponentModel componentModel = camelCatalog.componentModel(adjustedComponentName);

        if (componentModel == null) {
            if (componentName.startsWith("camel-")) {
                componentModel =  camelCatalog.componentModel(adjustedComponentName.replace("camel-", ""));
                if (componentModel == null) {
                    throw new ComponentNotFoundException("The component name " + componentName + " does not exist");
                }
            }
        }

        return componentModel;
    }

    /**
     * Extracts the major.minor version from a version string (e.g., "4.9.0" -> "4.9", "4.10.1" -> "4.10")
     */
    private String extractMajorMinorVersion(String version) {
        String[] parts = version.split("\\.");
        if (parts.length >= 2) {
            return parts[0] + "." + parts[1];
        }
        // Fallback if version format is unexpected
        return version;
    }

    @Tool(description = "Fetches the URL for the official documentation page of a specific Apache Camel component. Use this when the user asks for a direct link.")
    public ToolResponse getComponentURL(
            @ToolArg(description = "The scheme name of the component. For example: 'file', 'kafka', or 'jms'.") String componentName) {
        final ComponentModel componentModel;
        try {
            componentModel = findComponent(componentName);
        } catch (ComponentNotFoundException e) {
            return ToolResponse.error(e.getMessage());
        }

        final String baseVersion = extractMajorMinorVersion(componentModel.getVersion());

        String page = "https://camel.apache.org/components/" + baseVersion + ".x/" + componentModel.getName() + "-component.html";

        JsonObject reply = new JsonObject();
        reply.put("page", page);

        return ToolResponse.success(reply.toString());
    }

    /**
     * Replaces the content of a section (parent div of an h2 with specific id) with a message
     */
    private void replaceH2SectionContent(Element root, String h2Id, String replacementText) {
        Element h2 = root.selectFirst("h2#" + h2Id);
        if (h2 != null && h2.parent() != null) {
            Element parentDiv = h2.parent();
            // Clear all content except the h2
            parentDiv.children().forEach(child -> {
                if (!child.equals(h2)) {
                    child.remove();
                }
            });
            // Add a paragraph with the replacement text
            parentDiv.appendElement("p").text(replacementText);
        }
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

    @Tool(description = "Fetches and converts the full documentation for a specific Apache Camel component from the official documentation website to Markdown format. Use this when you need comprehensive documentation including examples, configuration details, and usage instructions.")
    public ToolResponse getComponentDocumentation(
            @ToolArg(description = "The scheme name of the component. For example: 'file', 'kafka', or 'jms'.") String componentName) {
        final ComponentModel componentModel;
        try {
            componentModel = findComponent(componentName);
        } catch (ComponentNotFoundException e) {
            return ToolResponse.error(e.getMessage());
        }

        // Build the documentation URL using the same logic as getComponentURL
        final String baseVersion = extractMajorMinorVersion(componentModel.getVersion());
        String url = "https://camel.apache.org/components/" + baseVersion + ".x/" + componentModel.getName() + "-component.html";

        try {
            // Fetch the HTML documentation
            Document doc = Jsoup.connect(url).get();

            // Extract the content - prefer <article> tag if present, otherwise use full HTML
            String htmlContent;
            Element articleElement = doc.selectFirst("article");
            if (articleElement != null) {
                // Replace content of tables tool references to improve performances
                replaceH2SectionContent(articleElement, "_configuring_options", "");
                replaceH2SectionContent(articleElement, "_component_options",
                    "The component options can be retrieved via the tool getInformationAboutOptions");
                replaceH2SectionContent(articleElement, "_endpoint_options",
                    "The endpoint options can be retrieved via the tool getInformationAboutOptions");

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
            reply.put("componentName", componentModel.getName());
            reply.put("documentationUrl", url);
            reply.put("markdown", markdown);

            return ToolResponse.success(reply.toString());

        } catch (IOException e) {
            return ToolResponse.error("Failed to fetch documentation from " + url + ": " + e.getMessage());
        } catch (Exception e) {
            return ToolResponse.error("Failed to convert documentation to markdown: " + e.getMessage());
        }
    }
}
