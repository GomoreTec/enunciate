package com.webcohesion.enunciate.modules.objc_json_client;

import com.webcohesion.enunciate.modules.objc_json_client.ObjCJSONClientModule;

import junit.framework.TestCase;

/**
 * @author Ryan Heaton
 */
public class TestObjCDeploymentModule extends TestCase {

  /**
   * tests scrubbing a c identifie.
   */
  public void testScrubIdentifier() throws Exception {
    assertEquals("hello_me", ObjCJSONClientModule.scrubIdentifier("hello-me"));
  }

  public void testPackageIdentifier() throws Exception {
    String[] subpackages = "com.webcohesion.enunciate.samples.objc.whatever".split("\\.", 9);
    assertEquals("ENUNCIATECOMOBJC", String.format("%3$S%1$S%5$S", subpackages));

  }

}
