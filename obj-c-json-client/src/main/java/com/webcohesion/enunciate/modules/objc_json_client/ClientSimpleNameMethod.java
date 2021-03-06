/*
 * Copyright 2006-2008 Web Cohesion
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

package com.webcohesion.enunciate.modules.objc_json_client;

import com.webcohesion.enunciate.modules.jaxb.model.Accessor;
import com.webcohesion.enunciate.modules.jaxb.model.AnyElement;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import java.util.List;
import java.util.Map;

/**
 * Template method used to determine the objective-c "simple name" of an
 * accessor.
 *
 * @author Ryan Heaton
 */
public class ClientSimpleNameMethod implements TemplateMethodModelEx {

  public ClientSimpleNameMethod() {
  }

  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException(
          "The functionIdentifierFor method must have an accessor or type mirror as a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = BeansWrapper.getDefaultInstance().unwrap(from);
    String name;
    if (unwrapped instanceof Accessor) {
      Accessor accessor = (Accessor) unwrapped;
      name = accessor.getClientSimpleName();
      // if (this.clientNames.containsKey(name)) {
      // name = this.clientNames.get(name);
      // }
      name = FieldNameTransfer.transfer(name);
    } else if (unwrapped instanceof AnyElement) {
      AnyElement accessor = (AnyElement) unwrapped;
      name = accessor.getClientSimpleName();
      name = FieldNameTransfer.transfer(name);
      // if (this.clientNames.containsKey(name)) {
      // name = this.clientNames.get(name);
      // }
    } else {
      throw new TemplateModelException(
          "The clientSimpleName method must have an accessor as a parameter.");
    }

    return name;
  }

}