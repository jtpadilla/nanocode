# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & run

Bazel 8.6.0 + Java 21 (toolchain pinned in `.bazelrc` to `remotejdk_21`). Use `bazelisk` if available.

```bash
# Run the single-agent REPL (memory window = 20 messages, tools called directly)
bazel run //java/agent/basic

# Run the multi-agent REPL (Supervisor dispatches to File/System/Web sub-agents)
bazel run //java/agent/agentic

# Build everything
bazel build //...

# Build a single target
bazel build //java/agent/basic
```

Both binaries require `GOOGLE_AI_GEMINI_API_KEY` (or `GEMINI_API_KEY`) in the environment. `MODEL` overrides the default `gemini-3-flash-preview` (see `java/agent/config/.../Config.java`).

There are no tests yet; `bazel test //...` is a no-op.

The disk cache is shared with the IDE at `~/.cache/nanocode-cache` (`.bazelrc`), so rebuilds across IntelliJ/Bazel BSP and CLI hit the same artifacts.

## Architecture

Two parallel entry points share `//java/agent/config` (env-var reading) and `//java/agent/format` (ANSI colors + a tiny markdown-to-ANSI renderer used to print agent responses). Each entry point lives in its own Bazel package under `//java/agent/...`, and each package owns a `tools/` directory of `@Tool`-annotated methods plus an `agents/` directory of LangChain4j `AiServices` interfaces.

**`//java/agent/basic` — single agent.** `NanocodeBasic.main` wires one `GoogleAiGeminiChatModel` to the `Assistant` interface via `AiServices.builder(...).chatMemory(MessageWindowChatMemory).tools(new Tools(...))`. The `Assistant` interface is just `@SystemMessage` + `@UserMessage` — LangChain4j generates the implementation and routes tool calls into `Tools` (read/write/edit/glob/grep/bash/websearch/webfetch). `websearch` and `webfetch` spin up *separate* Gemini models with `allowGoogleSearch` / `allowUrlContext` enabled rather than going through the main model.

**`//java/agent/agentic` — supervisor + specialists.** `NanocodeAgentic.main` builds three sub-agents (`FileAgent`, `SystemAgent`, `WebAgent`) via `AgenticServices.agentBuilder(...)`, each with its own scoped tool class (`FileTools`, `SystemTools`, `WebTools`). A `SupervisorAgent` (built via `AgenticServices.supervisorBuilder()` with `SupervisorResponseStrategy.SUMMARY`) is given all three as `subAgents` and exposes `invoke(userInput)`. The supervisor decides which sub-agent handles each turn. `/c` in this mode does **not** clear a `ChatMemory`; it evicts the supervisor's agentic scope via `((AgenticScopeAccess) supervisor).evictAgenticScope("default")`. Each sub-agent interface uses `@Agent(name=..., description=...)` — those descriptions are what the supervisor reads to route work, so editing them changes routing behavior.

**Tool side-effects print directly to stdout.** Each `@Tool` method emits a `⏺ ToolName(args)` banner and a `⎿  preview` tail using `Format`. The `Tools` constructors accept a `BiFunction<String,Integer,String>` preview function (passed as `Format::preview`) — that lambda is the seam if you ever want to swap how tool output is displayed. `bash` has a hard 30-second timeout and merges stderr into stdout.

**Subtle gotcha — `main_class` paths in BUILD files.** `java/agent/basic/BUILD.bazel` declares `main_class = "io.github.jtpadilla.nanocode.basic.NanocodeBasic"` and the agentic one declares `io.github.jtpadilla.nanocode.agentic.NanocodeAgentic`, but the actual Java packages are `io.github.jtpadilla.nanocode.agent.basic` / `io.github.jtpadilla.nanocode.agent.agentic` (with `.agent.` in the middle). If you change main classes or rename packages, fix both sides.

**`//java/sshdagent`** is an embedded SSH console (Apache MINA SSHD) wired through the Helidon 4.x Service Registry: `@Service.*`-annotated classes (`SshServerService`, `ServicePasswordAuthenticator`, `ServiceShellFactory`, `DemoCredentialService`, `DemoCommandHandler`) are discovered at compile time by the `helidon_codegen` `java_plugin`. The `sshd` library holds the server; the `demo` binary (`io.github.jtpadilla.sshdagent.Main`) boots the registry and listens on port 2222. Not wired into the agents above.

## Dependency management

All Maven artifacts go through `MODULE.bazel` via `rules_jvm_external`. Versions are pinned through two BOMs (`langchain4j-bom:1.13.0`, `slf4j-bom:2.0.17`); only artifacts on a different release cadence (e.g. `langchain4j-agentic` beta) pin their version inline. Add new artifacts to `maven.install(artifacts=...)` and reference them in `BUILD.bazel` as `@maven//:group_artifact` with `.` and `-` rewritten to `_`.
