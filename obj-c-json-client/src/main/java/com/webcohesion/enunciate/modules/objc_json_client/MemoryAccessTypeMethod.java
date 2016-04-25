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

import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.modules.jaxb.model.Accessor;
import com.webcohesion.enunciate.modules.jaxb.model.AnyElement;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * get memory access type for element
 *
 * @author Debenson
 */
public class MemoryAccessTypeMethod implements TemplateMethodModelEx {

  @SuppressWarnings({
      "deprecation", "rawtypes" })
  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException(
          "The function memory acction type  method must have an element as a parameter.");
    }

    String memoryType = "copy";
    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = BeansWrapper.getDefaultInstance().unwrap(from);
    if (unwrapped instanceof Accessor) {
      Accessor accessor = (Accessor) unwrapped;
      DecoratedTypeMirror accessorType = accessor.getAccessorType();
      if (accessor.isCollectionType()) {
        memoryType = "retain";
      } else if (accessorType.isPrimitive()) {
        memoryType = "assign";
      } else if (accessorType.isEnum()) {
        memoryType = "copy";
      } else {
        if (String.class.getName().equals(accessorType.toString())) {
          memoryType = "copy";
        } else {
          memoryType = "retain";
        }
      }
    } else if (unwrapped instanceof AnyElement) {
      memoryType = "retain";
    } else {
      throw new TemplateModelException(
          "The memoryAccessTypeFor method must have an accessor as a parameter.");
    }

    return memoryType;
  }
}