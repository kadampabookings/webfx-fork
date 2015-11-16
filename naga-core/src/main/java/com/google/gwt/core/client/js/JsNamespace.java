/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.core.client.js;

import java.lang.annotation.*;

/**
 * Provides a default namespace for @JsExport annotations which don't specify a value. The
 * computed fully qualified export symbol will be a combination of the nearest enclosing
 * \@JsNamespace and the Java name of the method or field @JsExport is applied to. If applied to
 * package-info.java, applies to all types in a package.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PACKAGE})
@Documented
public @interface JsNamespace {
  String value() default "";
}
