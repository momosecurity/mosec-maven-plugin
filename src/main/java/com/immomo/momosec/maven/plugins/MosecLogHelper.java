/*
 * Copyright 2020 momosecurity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.immomo.momosec.maven.plugins;

public class MosecLogHelper {
    private static final String YELLOW = "\033[1;33m";
    private static final String LIGHT_RED = "\033[1;31m";
    private static final String LIGHT_GREEN = "\033[1;32m";

    private static final String CANCEL_COLOR = "\033[0m";

    public String strongWarning(String content) {
        return YELLOW + content + CANCEL_COLOR;
    }

    public String strongError(String content) {
        return LIGHT_RED + content + CANCEL_COLOR;
    }

    public String strongInfo(String content) {
        return LIGHT_GREEN + content + CANCEL_COLOR;
    }
}
