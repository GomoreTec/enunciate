/**
 * 版权所有(C)，上海勾芒信息科技，2016，所有权利保留。
 * 
 * 项目名：	enunciate-obj-c-json-client
 * 文件名：	Util.java
 * 模块说明：	
 * 修改历史：
 * 2016年5月9日 - Debenson - 创建。
 */
package com.webcohesion.enunciate.modules.objc_json_client;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.modules.jaxb.model.Accessor;

/**
 * @author Debenson
 * @since 0.1
 */
public class Util {
  private static final List<String> extPrimitives = new ArrayList<String>();
  private static final List<String> extCopyTypes = new ArrayList<String>();

  static {
    extPrimitives.add(Boolean.class.getName());
    extPrimitives.add(Short.class.getName());
    extPrimitives.add(Integer.class.getName());
    extPrimitives.add(Double.class.getName());
    extPrimitives.add(Float.class.getName());

    extCopyTypes.add(String.class.getName());
    extCopyTypes.add(Date.class.getName());
    extCopyTypes.add(Timestamp.class.getName());
  }

  public static boolean isPrimitive(Accessor accessor) {
    DecoratedTypeMirror<?> accessorType = accessor.getAccessorType();
    String accessorTypeStr = accessorType.toString();
    if (accessorType.isPrimitive() || extPrimitives.contains(accessorTypeStr)) {
      return true;
    } else {
      return false;
    }
  }

  public static boolean isCopyType(DecoratedTypeMirror<?> accessorType) {
    return extCopyTypes.contains(accessorType.toString());
  }

}
