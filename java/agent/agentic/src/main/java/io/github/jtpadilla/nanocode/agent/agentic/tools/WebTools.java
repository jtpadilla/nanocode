package io.github.jtpadilla.nanocode.agent.agentic.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import io.github.jtpadilla.nanocode.lib.format.Format;

import static io.github.jtpadilla.nanocode.lib.config.Config.GEMINI_KEY;
import static io.github.jtpadilla.nanocode.lib.config.Config.MODEL_NAME;

public class WebTools {

    @Tool("Search the web using Google Search")
    public String search(@P("The search query") String query) {
        System.out.println("\n" + Format.GREEN + "⏺ WebSearch" + Format.RESET + "(" + Format.DIM + query + Format.RESET + ")");
        var searchModel = GoogleAiGeminiChatModel.builder()
                .apiKey(GEMINI_KEY)
                .modelName(MODEL_NAME)
                .allowGoogleSearch(true)
                .build();
        return searchModel.chat(query);
    }

    @Tool("Fetch the content of a specific URL")
    public String fetch(@P("The URL to fetch") String url) {
        System.out.println("\n" + Format.GREEN + "⏺ WebFetch" + Format.RESET + "(" + Format.DIM + url + Format.RESET + ")");
        var fetchModel = GoogleAiGeminiChatModel.builder()
                .apiKey(GEMINI_KEY)
                .modelName(MODEL_NAME)
                .allowUrlContext(true)
                .build();
        return fetchModel.chat("Please extract and summarize the content of this URL: " + url);
    }
}
