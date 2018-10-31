/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ******************************************************************************/
package org.apache.sling.scripting.sightly.extension;

import org.apache.sling.scripting.sightly.render.RenderContext;

import org.osgi.annotation.versioning.ConsumerType;

/**
 * A {@code RuntimeExtension} represents a HTL runtime construct that provides some processing capabilities for the various
 * {@code data-sly-*} block elements.
 */
@ConsumerType
public interface RuntimeExtension {

    /**
     * <p>
     *     The name of the runtime function that will process string
     *     formatting. The function will receive the following parameters:
     * </p>
     * <ol>
     *     <li>the format String (e.g. 'Hello {0}, welcome to {1}')</li>
     *     <li>an array of objects that will replace the format placeholders</li>
     * </ol>
     * <p>
     *     For more details check https://github.com/Adobe-Marketing-Cloud/htl-spec/blob/1.2/SPECIFICATION.md#122-format.
     * </p>
     */
    String FORMAT = "format";

    /**
     * <p>
     *     The name of the runtime function that will process
     *     i18n. The function will receive the following parameters:
     * </p>
     * <ol>
     *     <li>the String to translate</li>
     *     <li>optional: locale information</li>
     *     <li>optional: hint information</li>
     *     <li>optional (not part of the specification): basename information; for more details see
     *     {@link java.util.ResourceBundle#getBundle(String, java.util.Locale)}</li>
     * </ol>
     * <p>
     *     For more details check https://github.com/Adobe-Marketing-Cloud/htl-spec/blob/1.2/SPECIFICATION.md#123-i18n.
     * </p>
     */
    String I18N = "i18n";

    /**
     * <p>
     *     The name of the runtime function that will process
     *     join operations on arrays. The function will receive the following parameters:
     * </p>
     * <ol>
     *     <li>the array of objects to join (e.g. [1, 2, 3])</li>
     *     <li>the join string (e.g. ';')</li>
     * </ol>
     * <p>
     *     For more details check https://github.com/Adobe-Marketing-Cloud/htl-spec/blob/1.2/SPECIFICATION.md#124-array-join.
     * </p>
     */
    String JOIN = "join";

    /**
     * <p>
     *     The name of the runtime function that will provide
     *     URI manipulation support. The function will receive the following parameters:
     * </p>
     * <ol>
     *     <li>optional: a URI string to process</li>
     *     <li>optional: a Map containing URI manipulation options</li>
     * </ol>
     * <p>
     *     For more details check https://github.com/Adobe-Marketing-Cloud/htl-spec/blob/1.2/SPECIFICATION.md#125-uri-manipulation.
     * </p>
     */
    String URI_MANIPULATION = "uriManipulation";

    /**
     * <p>
     *     The name of the runtime function that will provide
     *     XSS escaping and filtering support. The function will receive the following parameters:
     * </p>
     * <ol>
     *     <li>the original string to escape / filter</li>
     *     <li>the context to be applied</li>
     * </ol>
     * <p>
     *     For more details check https://github.com/Adobe-Marketing-Cloud/htl-spec/blob/1.2/SPECIFICATION.md#121-display-context.
     * </p>
     */
    String XSS = "xss";

    /**
     * <p>
     *     The name of the runtime function that will perform
     *     script execution delegation. The function will receive the following parameters:
     * </p>
     * <ol>
     *     <li>optional: the relative or absolute path of the script to execute</li>
     *     <li>optional: a Map of options to perform script include processing</li>
     * </ol>
     * <p>
     *     For more details about the supported options check
     *     https://github.com/Adobe-Marketing-Cloud/htl-spec/blob/1.2/SPECIFICATION.md#228-include.
     * </p>
     */
    String INCLUDE = "include";

    /**
     * <p>
     *     The name of the runtime function that will perform
     *     resource inclusion in the rendering process. The function will receive the following parameters:
     * </p>
     * <ol>
     *     <li>optional: a relative or absolute path of the resource to be included</li>
     *     <li>optional: a Map containing the resource processing options</li>
     * </ol>
     * <p>
     *     For more details about the supported options check
     *     https://github.com/Adobe-Marketing-Cloud/htl-spec/blob/1.2/SPECIFICATION.md#229-resource.
     * </p>
     */
    String RESOURCE = "includeResource";

    /**
     * <p>
     *     The name of the runtime function that will provide
     *     the support for loading Use-API objects. The function will receive the following parameters:
     * </p>
     * <ol>
     *     <li>an identifier that allows to discover the Use-API object that needs to be loaded</li>
     *     <li>optional: a Map of the arguments that are passed to the Use-API object for initialisation or to provide context</li>
     * </ol>
     * <p>
     *     For more details check https://github.com/Adobe-Marketing-Cloud/htl-spec/blob/1.2/SPECIFICATION.md#221-use.
     * </p>
     */
    String USE = "use";

    /**
     * For OSGi environments this is the name of the service registration property indicating the {@code RuntimeExtension} name.
     */
    String NAME = "org.apache.sling.scripting.sightly.extension.name";

    /**
     * Call the {@code RuntimeExtension}
     *
     * @param renderContext the runtime context
     * @param arguments     the call arguments
     * @return an extension instance
     */
    Object call(RenderContext renderContext, Object... arguments);
}
