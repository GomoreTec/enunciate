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

import java.util.List;

import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.model.Accessor;
import com.webcohesion.enunciate.modules.jaxb.model.EnumTypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.SchemaInfo;
import com.webcohesion.enunciate.modules.jaxb.model.TypeDefinition;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * get enum variables for element
 *
 * @author Debenson
 */
public class GetEnumVariablesMethod implements TemplateMethodModelEx {

  private EnunciateJaxbContext jaxbContext;

  public GetEnumVariablesMethod(EnunciateJaxbContext jaxbContext) {
    super();
    this.jaxbContext = jaxbContext;
  }

  @SuppressWarnings({
      "deprecation", "rawtypes" })
  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException(
          "The function memory acction type  method must have an element as a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = BeansWrapper.getDefaultInstance().unwrap(from);
    if (unwrapped instanceof Accessor) {
      Accessor accessor = (Accessor) unwrapped;
      if (!accessor.getAccessorType().isEnum()) {
        throw new TemplateModelException(
            "The GetEnumVariablesMethod must have an enum accessor as a parameter.");
      }

      EnumTypeDefinition typeDef = getEnumTypeDefinition(accessor.getAccessorType().toString());
      if (typeDef == null) {
        throw new TemplateModelException(
            "The GetEnumVariablesMethod must have an enum accessor as a parameter.");
      }

      return typeDef.getEnumValues();
    } else {
      throw new TemplateModelException(
          "The GetEnumVariablesMethod must have an enum accessor as a parameter.");
    }
  }

  private EnumTypeDefinition getEnumTypeDefinition(String qualifiedName) {
    for (SchemaInfo schemaInfo : jaxbContext.getSchemas().values()) {
      for (TypeDefinition typeDefinition : schemaInfo.getTypeDefinitions()) {
        if (typeDefinition.isEnum()
            && typeDefinition.getQualifiedName().toString().equals(qualifiedName)) {
          return (EnumTypeDefinition) typeDefinition;
        }
      }
    }
    return null;
  }

}