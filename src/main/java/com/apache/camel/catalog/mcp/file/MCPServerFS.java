package com.apache.camel.catalog.mcp.file;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkiverse.mcp.server.McpLog;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolCallException;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.nio.file.Files.exists;

public class MCPServerFS {

    @Inject
    FSUtil util;

    private ObjectMapper mapper;

    public MCPServerFS(
            ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Tool(description = "Read the complete contents of a file from the file system. Handles various text encodings and provides detailed error messages if the file cannot be read. Use this tool when you need to examine the contents of a single file.")
    String read_file(@ToolArg(description = "Path to the file to read") String path) {
        Log.infof("Tool invoked: read_file(path=%s)", path);
        Path resolvedPath = util.validateAndResolvePath(path);
        if (!exists(resolvedPath)) {
            throw new ToolCallException("Path does not exist: " + path, null);
        }
        try {
            return Files.readString(resolvedPath);
        } catch (IOException e) {
            throw new ToolCallException("Failed to read file: " + e.getMessage(), e);
        }
    }

    String valueAsString(Object o) {
        try {
            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new ToolCallException("Failed to serialize object: " + e.getMessage(), e);
        }
    }

    @Tool(description = "Read the contents of multiple files simultaneously. This is more efficient than reading files one by one when you need to analyze or compare multiple files. Each file's content is returned with its path as a reference. Failed reads for individual files won't stop the entire operation.")
    String read_multiple_files(@ToolArg(description = "List of file paths to read") List<String> paths) {
        Log.infof("Tool invoked: read_multiple_files(paths=%s)", paths);

        Map<String, String> result = new HashMap<>();

        try {
            for (String path : paths) {
                var realpath = util.validateAndResolvePath(path);
                result.put(path, Files.readString(realpath));
            }
        } catch (IOException e) {
            throw new ToolCallException("Failed to read file: " + e.getMessage(), e);
        }
        return valueAsString(result);
    }

    @Tool(description = "Get a detailed listing of all files and directories in a specified path. Results clearly distinguish between files and directories with [FILE] and [DIR] prefixes. This tool is essential for understanding directory structure and finding specific files within a directory.")
    String list_directory(@ToolArg(description = "Path to list contents of") String path, McpLog logger) {
        logger.info("Tool invoked: list_directory(path=%s)", path);
        Path resolvedPath = util.validateAndResolvePath(path);
        if (!exists(resolvedPath)) {
            throw new ToolCallException("Path does not exist: " + path, null);
        }
        if (!Files.isDirectory(resolvedPath)) {
            throw new ToolCallException("Path is not a directory: " + path, null);
        }

        try {
            StringBuilder listing = new StringBuilder();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(resolvedPath)) {
                for (Path entry : stream) {
                    String prefix = Files.isDirectory(entry) ? "[DIR]  " : "[FILE] ";
                    listing.append(prefix).append(entry.getFileName()).append("\n");
                }
            }
            return listing.toString();
        } catch (IOException e) {
            throw new ToolCallException("Failed to list directory: " + e.getMessage(), e);
        }
    }

    @Tool(description = "Get a recursive tree view of files and directories as a JSON structure. Each entry includes 'name', 'type' (file/directory), and 'children' for directories. Files have no children array, while directories always have a children array (which may be empty). The output is formatted with 2-space indentation for readability.")
    String directory_tree(@ToolArg(description = "Root path to create tree from") String path) {
        Log.infof("Tool invoked: directory_tree(path=%s)", path);
        Path resolvedPath = util.validateAndResolvePath(path);
        if (!exists(resolvedPath)) {
            throw new ToolCallException("Path does not exist: " + path, null);
        }

        record TreeEntry(String name, String type, List<TreeEntry> children) {
        }

        try {
            var buildTree = new Function<Path, TreeEntry>() {
                @Override
                public TreeEntry apply(Path path) {
                    try {
                        String name = path.getFileName().toString();
                        if (Files.isDirectory(path)) {
                            List<TreeEntry> children = new ArrayList<>();
                            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                                for (Path child : stream) {
                                    children.add(this.apply(child));
                                }
                            }
                            return new TreeEntry(name, "directory", children);
                        } else {
                            return new TreeEntry(name, "file", null);
                        }
                    } catch (IOException e) {
                        throw new ToolCallException("Failed to build directory tree: " + e.getMessage(), e);
                    }
                }
            };

            TreeEntry root = buildTree.apply(resolvedPath);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (Exception e) {
            Throwable cause = e instanceof ToolCallException ? e : e.getCause();
            throw new ToolCallException("Failed to build directory tree: " + cause.getMessage(), cause);
        }
    }

    @Tool(description = "Recursively search for files and directories matching a pattern. Searches through all subdirectories from the starting path. The search is case-insensitive and matches partial names. Returns full paths to all matching items. Great for finding files when you don't know their exact location.")
    String search_files(@ToolArg(description = "Starting path for search") String path,
                        @ToolArg(description = "Pattern to search for") String pattern) {
        Log.infof("Tool invoked: search_files(path=%s, pattern=%s)", path, pattern);
        Path resolvedPath = util.validateAndResolvePath(path);
        if (!exists(resolvedPath)) {
            throw new ToolCallException("Path does not exist: " + path, null);
        }
        if (!Files.isDirectory(resolvedPath)) {
            throw new ToolCallException("Path is not a directory: " + path, null);
        }

        List<String> matches = new ArrayList<>();
        String lowerPattern = pattern.toLowerCase();

        try {
            searchRecursive(resolvedPath, lowerPattern, matches);
            return valueAsString(matches);
        } catch (IOException e) {
            throw new ToolCallException("Failed to search files: " + e.getMessage(), e);
        }
    }

    private void searchRecursive(Path directory, String lowerPattern, List<String> matches) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path entry : stream) {
                String fileName = entry.getFileName().toString().toLowerCase();
                if (fileName.contains(lowerPattern)) {
                    matches.add(entry.toString());
                }
                if (Files.isDirectory(entry)) {
                    searchRecursive(entry, lowerPattern, matches);
                }
            }
        }
    }

    @Tool(description = "Retrieve detailed metadata about a file or directory. Returns comprehensive information including size, creation time, last modified time, permissions, and type. This tool is perfect for understanding file characteristics without reading the actual content.")
    String get_file_info(@ToolArg(description = "Path to get info for") String path) {
        Log.infof("Tool invoked: get_file_info(path=%s)", path);
        Path resolvedPath = util.validateAndResolvePath(path);
        if (!exists(resolvedPath)) {
            throw new ToolCallException("Path does not exist: " + path, null);
        }

        try {
            BasicFileAttributes attrs = Files.readAttributes(resolvedPath, BasicFileAttributes.class);
            Map<String, Object> info = new HashMap<>();

            info.put("path", resolvedPath.toString());
            info.put("name", resolvedPath.getFileName().toString());
            info.put("type", attrs.isDirectory() ? "directory" : "file");
            info.put("size", attrs.size());
            info.put("creationTime", attrs.creationTime().toString());
            info.put("lastModifiedTime", attrs.lastModifiedTime().toString());
            info.put("lastAccessTime", attrs.lastAccessTime().toString());
            info.put("isReadable", Files.isReadable(resolvedPath));
            info.put("isWritable", Files.isWritable(resolvedPath));
            info.put("isExecutable", Files.isExecutable(resolvedPath));
            info.put("isSymbolicLink", attrs.isSymbolicLink());

            return valueAsString(info);
        } catch (IOException e) {
            throw new ToolCallException("Failed to get file info: " + e.getMessage(), e);
        }
    }
}
