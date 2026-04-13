package io.github.jtpadilla.nanocode.agentic.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface WebAgent {
    @Agent(name = "web_specialist", description = "Expert in searching the web and fetching content from URLs.")
    @UserMessage("Perform this web task: {{task}}")
    String work(@V("task") String task);
}
