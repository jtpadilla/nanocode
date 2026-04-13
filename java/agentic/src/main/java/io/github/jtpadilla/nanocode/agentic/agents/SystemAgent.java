package io.github.jtpadilla.nanocode.agentic.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface SystemAgent {
    @Agent(name = "system_specialist", description = "Expert in executing shell commands and managing the environment.")
    @UserMessage("Perform this system task: {{task}}")
    String work(@V("task") String task);
}
