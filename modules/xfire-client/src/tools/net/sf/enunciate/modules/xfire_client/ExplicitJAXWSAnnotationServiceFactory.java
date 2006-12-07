package net.sf.enunciate.modules.xfire_client;

import net.sf.enunciate.modules.xfire_client.annotations.RequestWrapperAnnotation;
import net.sf.enunciate.modules.xfire_client.annotations.ResponseWrapperAnnotation;
import net.sf.enunciate.modules.xfire_client.annotations.WebFaultAnnotation;
import net.sf.enunciate.modules.xfire_client.annotations.XmlRootElementAnnotation;
import org.codehaus.xfire.XFireRuntimeException;
import org.codehaus.xfire.annotations.AnnotationServiceFactory;
import org.codehaus.xfire.annotations.WebAnnotations;
import org.codehaus.xfire.annotations.WebMethodAnnotation;
import org.codehaus.xfire.annotations.WebServiceAnnotation;
import org.codehaus.xfire.annotations.soap.SOAPBindingAnnotation;
import org.codehaus.xfire.exchange.MessageSerializer;
import org.codehaus.xfire.fault.XFireFault;
import org.codehaus.xfire.service.FaultInfo;
import org.codehaus.xfire.service.OperationInfo;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.soap.AbstractSoapBinding;
import org.codehaus.xfire.transport.TransportManager;
import org.codehaus.xfire.util.ClassLoaderUtils;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.HashMap;
import java.lang.reflect.Method;

/**
 * The explicit annotation service factory is based on JAXWS metatdata that is explicitly supplied
 * (as opposed to looked-up at runtime).  The metadata is assumed to exist in the form of a resource
 * on the classpath, identified by a typeset id.
 *
 * @author Ryan Heaton
 * @see org.codehaus.xfire.jaxws.JAXWSServiceFactory
 */
public class ExplicitJAXWSAnnotationServiceFactory extends AnnotationServiceFactory {

  private final ExplicitWebAnnotations annotations;

  /**
   * Construct a new explicit annotation service factory.  The explicit annotations will be loaded from
   * a file on the classpath identified by the supplied typeset id.  The file should be located at
   * "/[typeSetId].annotations".  There should also be a list of types in the form of a resource located at
   * "/[typeSetId].types".
   *
   * @param typeSetId        The typeset id.
   * @param transportManager The transport manager.
   */
  public ExplicitJAXWSAnnotationServiceFactory(String typeSetId, TransportManager transportManager) throws IOException, ClassNotFoundException {
    this(
      ExplicitWebAnnotations.readFrom(ExplicitJAXWSAnnotationServiceFactory.class.getResourceAsStream("/" + typeSetId + ".annotations")),
      transportManager,
      new EnunciatedClientBindingProvider(new IntrospectingTypeRegistry(typeSetId))
    );
  }

  /**
   * Construct a new explicit annotation service factory.
   *
   * @param annotations      The annotations.
   * @param transportManager The transport manager.
   * @param bindingProvider  The binding provider.
   */
  protected ExplicitJAXWSAnnotationServiceFactory(ExplicitWebAnnotations annotations, TransportManager transportManager, EnunciatedClientBindingProvider bindingProvider) {
    super(annotations, transportManager, bindingProvider);
    this.annotations = annotations;
  }

  /**
   * The class for which to create a service.  By default, this service factory allows interfaces.
   *
   * @param clazz The class.
   * @return The service for the specified class.
   */
  public Service create(Class clazz) {
    HashMap properties = new HashMap();
    properties.put(ALLOW_INTERFACE, Boolean.TRUE);
    return super.create(clazz, properties);
  }

  /**
   * The annotations.
   *
   * @return The annotations.
   */
  protected WebAnnotations getAnnotations() {
    return annotations;
  }

  /**
   * The message serializer.  The message serializer for the explicit annotation service factory is an
   * {@link net.sf.enunciate.modules.xfire_client.EnunciatedClientMessageBinding}.
   *
   * @param binding The binding for which to get the serializer.
   * @return The message serializer.
   */
  protected MessageSerializer getSerializer(AbstractSoapBinding binding) {
    return new EnunciatedClientMessageBinding(annotations);
  }

  /**
   * Create a binding operation for the specified operation.
   *
   * @param service The service.
   * @param binding The binding.
   * @param op      The operation.
   */
  public void createBindingOperation(Service service, AbstractSoapBinding binding, OperationInfo op) {
    super.createBindingOperation(service, binding, op);

    try {
      binding.setSerializer(op, new EnunciatedClientOperationBinding(annotations, op));
    }
    catch (XFireFault e) {
      throw new XFireRuntimeException("Error setting the serializer on the operation binding.", e);
    }
  }

  /**
   * Add a fault to the operation. The fault info is populated with the explicit annotation metadata.
   *
   * @param service    The service.
   * @param op         The operation.
   * @param faultClass The fault class.
   * @return The fault info.
   */
  protected FaultInfo addFault(Service service, OperationInfo op, Class faultClass) {
    WebFaultAnnotation faultInfo = annotations.getWebFaultAnnotation(faultClass);
    if (faultInfo != null) {
      QName name = new QName(faultInfo.targetNamespace(), faultInfo.name());
      FaultInfo info = op.addFault(name.getLocalPart());
      info.setExceptionClass(faultClass);
      try {
        info.addMessagePart(name, ClassLoaderUtils.loadClass(faultInfo.faultBean(), faultClass));
      }
      catch (ClassNotFoundException e) {
        throw new XFireRuntimeException("Unable to load fault bean.", e);
      }

      return info;
    }

    throw new XFireRuntimeException("Unknown web fault: " + faultClass.getName());
  }

  /**
   * Whether the specified class is a fault info class.
   *
   * @param faultClass The fault class.
   * @return Whether the fault specified an explicit fault bean, or whether the bean is implied.
   */
  protected boolean isFaultInfoClass(Class faultClass) {
    WebFaultAnnotation faultInfo = annotations.getWebFaultAnnotation(faultClass);
    return ((faultInfo != null) && (!faultInfo.implicitFaultBean()));
  }

  /**
   * Create the input message name of the given operation.  The information is obtained from the
   * explicit annotations.
   *
   * @param op The operation
   * @return The input message name.
   */
  protected QName createInputMessageName(final OperationInfo op) {
    Method method = op.getMethod();
    SOAPBindingAnnotation soapBinding = annotations.getSOAPBindingAnnotation(method);
    if (soapBinding == null) {
      throw new XFireRuntimeException("Unable to create the input message name: no SOAP binding metadata about " + method);
    }

    if (soapBinding.getStyle() == SOAPBindingAnnotation.STYLE_RPC) {
      WebServiceAnnotation webServiceAnnotation = annotations.getWebServiceAnnotation(method.getDeclaringClass());
      if (webServiceAnnotation == null) {
        throw new XFireRuntimeException("Unable to create the input message name: no metadata was found about " + method.getDeclaringClass().getName());
      }

      WebMethodAnnotation webMethodAnnotation = annotations.getWebMethodAnnotation(method);
      if (webMethodAnnotation == null) {
        throw new XFireRuntimeException("Unable to create the input message name: no metadata found for " + method);
      }

      return new QName(webServiceAnnotation.getTargetNamespace(), webMethodAnnotation.getOperationName());
    }
    else if (soapBinding.getParameterStyle() == SOAPBindingAnnotation.PARAMETER_STYLE_BARE) {
      //document/literal bare
      XmlRootElementAnnotation rootElementAnnotation = annotations.getXmlRootElementAnnotation(method.getParameterTypes()[0]);

      if (rootElementAnnotation == null) {
        throw new XFireRuntimeException("Unable to create the input message name: no root element metadata about " + method.getParameterTypes()[0].getName());
      }

      return new QName(rootElementAnnotation.namespace(), rootElementAnnotation.name());
    }
    else {
      //document/literal wrapped.
      RequestWrapperAnnotation requestWrapper = annotations.getRequestWrapperAnnotation(method);

      if (requestWrapper == null) {
        throw new XFireRuntimeException("Unable to create the input message name: no request wrapper metadata about " + method);
      }

      return new QName(requestWrapper.targetNamespace(), requestWrapper.localName());
    }
  }

  /**
   * Create the output message name of the given operation.  The information is obtained from the
   * explicit annotations.
   *
   * @param op The operation
   * @return The output message name.
   */
  protected QName createOutputMessageName(final OperationInfo op) {
    Method method = op.getMethod();
    SOAPBindingAnnotation soapBinding = annotations.getSOAPBindingAnnotation(method);
    if (soapBinding == null) {
      throw new XFireRuntimeException("Unable to create the output message name: no SOAP binding metadata about " + method);
    }

    if (soapBinding.getStyle() == SOAPBindingAnnotation.STYLE_RPC) {
      WebServiceAnnotation webServiceAnnotation = annotations.getWebServiceAnnotation(method.getDeclaringClass());
      if (webServiceAnnotation == null) {
        throw new XFireRuntimeException("Unable to create the output message name: no metadata was found about " + method.getDeclaringClass().getName());
      }

      WebMethodAnnotation webMethodAnnotation = annotations.getWebMethodAnnotation(method);
      if (webMethodAnnotation == null) {
        throw new XFireRuntimeException("Unable to create the output message name: no metadata found for " + method);
      }

      return new QName(webServiceAnnotation.getTargetNamespace(), webMethodAnnotation.getOperationName());
    }
    else if (soapBinding.getParameterStyle() == SOAPBindingAnnotation.PARAMETER_STYLE_BARE) {
      //document/literal bare
      XmlRootElementAnnotation rootElementAnnotation = annotations.getXmlRootElementAnnotation(method.getReturnType());

      if (rootElementAnnotation == null) {
        throw new XFireRuntimeException("Unable to create the output message name: no root element metadata about " + method.getReturnType().getName());
      }

      return new QName(rootElementAnnotation.namespace(), rootElementAnnotation.name());
    }
    else {
      //document/literal wrapped.
      ResponseWrapperAnnotation responseWrapper = annotations.getResponseWrapperAnnotation(method);

      if (responseWrapper == null) {
        throw new XFireRuntimeException("Unable to create the output message name: no response wrapper metadata about " + method);
      }

      return new QName(responseWrapper.targetNamespace(), responseWrapper.localName());
    }
  }
}