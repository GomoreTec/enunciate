/**
 * 版权所有(C)，上海勾芒信息科技，2016，所有权利保留。
 * 
 * 项目名：	enunciate-obj-c-json-client
 * 文件名：	FieldNameTransfer.java
 * 模块说明：	
 * 修改历史：
 * 2016年5月9日 - Debenson - 创建。
 */
package com.webcohesion.enunciate.modules.objc_json_client;

/**
 * @author Debenson
 * @since 0.1
 */
public class FieldNameTransfer {

  public static String transfer(String fieldName) {
    if ("id".equals(fieldName)) {
      return "fid";
    } else if ("description".equals(fieldName)) {
      return "fdescription";
    } else if (fieldName.startsWith("new")) {
      return "f" + fieldName;
    } else {
      return fieldName;
    }
  }

}
