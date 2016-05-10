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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Objects;
import com.webcohesion.enunciate.modules.jaxb.model.ComplexTypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.Element;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Template method used to determine the objective-c "simple name" of an
 * accessor.
 *
 * @author Ryan Heaton
 */
public class GetFieldNameTransferMethod implements TemplateMethodModelEx {

  public GetFieldNameTransferMethod() {
  }

  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException(
          "The functionIdentifierFor method must have an accessor or type mirror as a parameter.");
    }

    Map<String, String> nameMapping = new HashMap<String, String>();
    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = BeansWrapper.getDefaultInstance().unwrap(from);
    if (unwrapped instanceof ComplexTypeDefinition) {
      ComplexTypeDefinition type = (ComplexTypeDefinition) unwrapped;
      for (Element ele : type.getElements()) {
        String name = ele.getClientSimpleName();
        String transferName = FieldNameTransfer.transfer(name);
        if (!Objects.equal(name, transferName)) {
          nameMapping.put(name, transferName);
        }
      }
    }
    return nameMapping;
  }

}