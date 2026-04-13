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
package io.github.jtpadilla.nanocode.lib.config;

import java.util.Optional;

public class Config {
    public static final String GEMINI_KEY = Optional.ofNullable(System.getenv("GOOGLE_AI_GEMINI_API_KEY"))
            .orElse(System.getenv("GEMINI_API_KEY"));
    public static final String MODEL_NAME = Optional.ofNullable(System.getenv("MODEL"))
            .orElse("gemini-3-flash-preview");
}
