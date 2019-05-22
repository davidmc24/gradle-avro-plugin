/**
 * Copyright © 2013-2015 Commerce Technologies, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.commercehub.avro.tools.base;

/**
 * Various constants needed by the plugin.
 *
 * <p>The default values from {@code avro-compiler} aren't exposed in a way that's easily accessible, so even default
 * values that we want to match are still reproduced here.</p>
 */
public class Constants {
    public static final String UTF8_ENCODING = "UTF-8";
    static final String SCHEMA_EXTENSION = "avsc";
    public static final String PROTOCOL_EXTENSION = "avpr";
    public static final String IDL_EXTENSION = "avdl";
    public static final String OPTION_FIELD_VISIBILITY = "fieldVisibility";
    public static final String OPTION_STRING_TYPE = "stringType";
}
