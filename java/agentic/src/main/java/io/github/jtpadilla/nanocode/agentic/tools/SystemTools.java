package io.github.jtpadilla.nanocode.agentic.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import io.github.jtpadilla.nanocode.format.Format;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

public class SystemTools {

    private final BiFunction<String, Integer, String> previewBifunction;

    public SystemTools(BiFunction<String, Integer, String> preview) {
        this.previewBifunction = preview;
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
