package com.webcohesion.enunciate.examples.objc_json_client.schema.structures;

import com.webcohesion.enunciate.metadata.qname.XmlQNameEnum;
import com.webcohesion.enunciate.metadata.qname.XmlUnknownQNameEnumValue;

/**
 * @author Ryan Heaton
 */
@XmlQNameEnum
public enum HouseType {

  brick,

  wood,

  mud,

  @XmlUnknownQNameEnumValue
  unknown
}
