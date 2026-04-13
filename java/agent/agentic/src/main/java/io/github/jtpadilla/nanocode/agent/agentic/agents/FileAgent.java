package io.github.jtpadilla.nanocode.agent.agentic.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface FileAgent {
    @Agent(name = "file_specialist", description = "Expert in file operations: read, write, edit, search.")
    @UserMessage("Perform this file task: {{task}}")
    String work(@V("task") String task);
}
