/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.jtpadilla.nanocode.agent.basic.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import io.github.jtpadilla.nanocode.lib.config.Config;
import io.github.jtpadilla.nanocode.lib.format.Format;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tools {

    final private BiFunction<String, Integer, String> previewBifunction;

    public Tools(BiFunction<String, Integer, String> previewBifunction) {
        this.previewBifunction = previewBifunction;
    }

    @Tool("Read file with line numbers (file path, not directory)")
    public String read(@P("Path to the file") String path,
                       @P("Start line (optional)") Integer offset,
                       @P("Number of lines to read (optional)") Integer limit) throws IOException {
        System.out.println("\n" + Format.GREEN + "⏺ Read" + Format.RESET + "(" + Format.DIM + path + Format.RESET + ")");
        var lines = Files.readAllLines(Path.of(path));
        int off = offset != null ? offset : 0;
        int lim = limit != null ? limit : lines.size();
        var sb = new StringBuilder();
        for (int i = off; i < Math.min(off + lim, lines.size()); i++)
            sb.append("%4d| %s%n".formatted(i + 1, lines.get(i)));
        var result = sb.toString();
        System.out.println("  " + Format.DIM + "⎿  " + previewBifunction.apply(result, 60) + Format.RESET);
        return result;
    }

    @Tool("Write content to file")
    public String write(@P("Path to the file") String path,
                        @P("Content to write") String content) throws IOException {
        System.out.println("\n" + Format.GREEN + "⏺ Write" + Format.RESET + "(" + Format.DIM + path + Format.RESET + ")");
        Files.writeString(Path.of(path), content);
        System.out.println("  " + Format.DIM + "⎿  ok" + Format.RESET);
        return "ok";
    }

    @Tool("Replace old with new in file (old must be unique unless all=true)")
    public String edit(@P("Path to the file") String path,
                       @P("Original string to replace") String old,
                       @P("New string") String repl,
                       @P("Replace all occurrences") Boolean all) throws IOException {
        System.out.println("\n" + Format.GREEN + "⏺ Edit" + Format.RESET + "(" + Format.DIM + path + Format.RESET + ")");
        var filePath = Path.of(path);
        var text = Files.readString(filePath);
        if (!text.contains(old)) return "error: old_string not found";

        long count = (text.length() - text.replace(old, "").length()) / old.length();
        boolean replaceAll = all != null && all;
        if (!replaceAll && count > 1)
            return "error: old_string appears " + count + " times, must be unique (use all=true)";

        Files.writeString(filePath, replaceAll
                ? text.replace(old, repl)
                : text.replaceFirst(Pattern.quote(old), Matcher.quoteReplacement(repl)));
        System.out.println("  " + Format.DIM + "⎿  ok" + Format.RESET);
        return "ok";
    }

    @Tool("Find files by pattern, sorted by mtime")
    public String glob(@P("Glob pattern (e.g. **/*.java)") String pat,
                       @P("Base path (optional)") String path) throws IOException {
        System.out.println("\n" + Format.GREEN + "⏺ Glob" + Format.RESET + "(" + Format.DIM + pat + Format.RESET + ")");
        var base = Path.of(path != null ? path : ".");
        var matcher = FileSystems.getDefault().getPathMatcher("glob:" + base + "/" + pat);
        if (!Files.exists(base)) return "none";
        try (var walk = Files.walk(base)) {
            var fileList = walk.filter(Files::isRegularFile).filter(matcher::matches)
                    .sorted((a, b) -> {
                        try { return Files.getLastModifiedTime(b).compareTo(Files.getLastModifiedTime(a)); }
                        catch (IOException e) { return 0; }
                    })
                    .map(Path::toString).toList();
            var result = fileList.isEmpty() ? "none" : String.join("\n", fileList);
            System.out.println("  " + Format.DIM + "⎿  " + previewBifunction.apply(result, 60) + Format.RESET);
            return result;
        }
    }

    @Tool("Search files for regex pattern")
    public String grep(@P("Regex pattern") String pat,
                       @P("Base path (optional)") String path) throws IOException {
        System.out.println("\n" + Format.GREEN + "⏺ Grep" + Format.RESET + "(" + Format.DIM + pat + Format.RESET + ")");
        var pattern = Pattern.compile(pat);
        var base = Path.of(path != null ? path : ".");
        var hits = new ArrayList<String>();
        try (var walk = Files.walk(base)) {
            walk.filter(Files::isRegularFile).takeWhile(ignored -> hits.size() < 50).forEach(file -> {
                try {
                    var lines = Files.readAllLines(file);
                    for (int i = 0; i < lines.size() && hits.size() < 50; i++)
                        if (pattern.matcher(lines.get(i)).find())
                            hits.add(file + ":" + (i + 1) + ":" + lines.get(i));
                } catch (Exception e) { /* skip */ }
            });
        }
        var result = hits.isEmpty() ? "none" : String.join("\n", hits);
        System.out.println("  " + Format.DIM + "⎿  " + previewBifunction.apply(result, 60) + Format.RESET);
        return result;
    }

    @Tool("Searches the web for up-to-date information on a given topic using Google Search")
    public String websearch(@P("The search query") String query) {
        System.out.println("\n" + Format.GREEN + "⏺ WebSearch" + Format.RESET + "(" + Format.DIM + query + Format.RESET + ")");
        var searchModel = dev.langchain4j.model.googleai.GoogleAiGeminiChatModel.builder()
                .apiKey(Config.GEMINI_KEY)
                .modelName(Config.MODEL_NAME)
                .allowGoogleSearch(true)
                .build();
        return searchModel.chat(query);
    }

    @Tool("Fetch the content of a specific URL")
    public String webfetch(@P("The URL to fetch") String url) {
        System.out.println("\n" + Format.GREEN + "⏺ WebFetch" + Format.RESET + "(" + Format.DIM + url + Format.RESET + ")");
        var fetchModel = dev.langchain4j.model.googleai.GoogleAiGeminiChatModel.builder()
                .apiKey(Config.GEMINI_KEY)
                .modelName(Config.MODEL_NAME)
                .allowUrlContext(true)
                .build();
        return fetchModel.chat("Please extract and summarize the content of this URL: " + url);
    }

    @Tool("Run shell command")
    public String bash(@P("Shell command to run") String cmd,
                       @P("Working directory (optional)") String dir) throws Exception {
        System.out.println("\n" + Format.GREEN + "⏺ Bash" + Format.RESET + "(" + Format.DIM + previewBifunction.apply(cmd, 50) + Format.RESET + ")");
        var pb = new ProcessBuilder("sh", "-c", cmd).redirectErrorStream(true);
        if (dir != null && !dir.isBlank()) pb.directory(new File(dir));
        var proc = pb.start();
        var out = new ArrayList<String>();
        try (var r = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
            String line;
            while ((line = r.readLine()) != null) {
                System.out.println("  " + Format.DIM + "│ " + line + Format.RESET);
                out.add(line);
            }
        }
        if (!proc.waitFor(30, TimeUnit.SECONDS)) {
            proc.destroyForcibly();
            out.add("(timed out after 30s)");
        }
        return out.isEmpty() ? "(empty)" : String.join("\n", out);
    }
}
