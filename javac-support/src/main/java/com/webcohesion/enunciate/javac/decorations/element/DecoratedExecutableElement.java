/*
 * Copyright 2006 Ryan Heaton
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
package com.webcohesion.enunciate.javac.decorations.element;

import com.webcohesion.enunciate.javac.decorations.ElementDecorator;
import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedReferenceType;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.javac.javadoc.JavaDocTagHandler;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import java.beans.Introspector;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Ryan Heaton
 */
@SuppressWarnings ( "unchecked" )
public class DecoratedExecutableElement extends DecoratedElement<ExecutableElement> implements ExecutableElement {

  public static final Pattern INHERITDOC_PATTERN = Pattern.compile("^[ \\t]*\\{@inheritDoc.*?\\}[ \\t]*");

  protected final List<? extends VariableElement> parameters;
  protected final List<? extends TypeMirror> thrownTypes;
  protected final List<? extends TypeParameterElement> typeParameters;
  protected final TypeMirror typeMirror;

  public DecoratedExecutableElement(ExecutableElement delegate, ProcessingEnvironment env) {
    super(delegate, env);

    this.parameters = loadDecoratedParameters();
    this.thrownTypes = loadDecoratedThrownTypes(delegate);
    this.typeParameters = ElementDecorator.decorate(delegate.getTypeParameters(), env);
    this.typeMirror = TypeMirrorDecorator.decorate(delegate.getReturnType(), env);
  }

  protected DecoratedExecutableElement(DecoratedExecutableElement copy) {
    super(copy.delegate, copy.env);
    this.parameters = copy.parameters;
    this.thrownTypes = copy.thrownTypes;
    this.typeParameters = copy.typeParameters;
    this.typeMirror = copy.typeMirror;
  }

  protected List<? extends TypeMirror> loadDecoratedThrownTypes(ExecutableElement delegate) {
    HashMap<String, String> throwsComments = new HashMap<String, String>();
    ArrayList<String> allThrowsComments = new ArrayList<String>();
    if (this.javaDoc.get("throws") != null) {
      allThrowsComments.addAll(this.javaDoc.get("throws"));
    }

    if (this.javaDoc.get("exception") != null) {
      allThrowsComments.addAll(this.javaDoc.get("exception"));
    }

    for (String throwsDoc : allThrowsComments) {
      int spaceIndex = throwsDoc.indexOf(' ');
      if (spaceIndex == -1) {
        spaceIndex = throwsDoc.length();
      }

      String exception = throwsDoc.substring(0, spaceIndex);
      String throwsComment = "";
      if ((spaceIndex + 1) < throwsDoc.length()) {
        throwsComment = throwsDoc.substring(spaceIndex + 1);
      }

      throwsComments.put(exception, throwsComment);
    }

    List<? extends TypeMirror> thrownTypes = TypeMirrorDecorator.decorate(delegate.getThrownTypes(), env);

    if (thrownTypes != null) {
      for (TypeMirror thrownType : thrownTypes) {
        String fullyQualifiedThrownTypeName = String.valueOf(thrownType);
        String throwsComment = throwsComments.get(fullyQualifiedThrownTypeName);
        if (throwsComment == null) {
          //try keying off the simple name in case that is how it is referenced in the javadocs.
          throwsComment = throwsComments.get(fullyQualifiedThrownTypeName.substring(fullyQualifiedThrownTypeName.lastIndexOf('.') + 1));
        }
        ((DecoratedReferenceType)thrownType).setDocComment(throwsComment);
      }
    }
    return thrownTypes;
  }

  protected List<? extends VariableElement> loadDecoratedParameters() {
    HashMap<String, String> paramsComments = new HashMap<String, String>();
    if (this.javaDoc.get("param") != null) {
      for (String paramDoc : this.javaDoc.get("param")) {
        paramDoc = paramDoc.replaceAll("\\s", " ");
        int spaceIndex = paramDoc.indexOf(' ');
        if (spaceIndex == -1) {
          spaceIndex = paramDoc.length();
        }

        String param = paramDoc.substring(0, spaceIndex);
        String paramComment = "";
        if ((spaceIndex + 1) < paramDoc.length()) {
          paramComment = paramDoc.substring(spaceIndex + 1);
        }

        paramsComments.put(param, paramComment);
      }
    }

    List<? extends VariableElement> parameters = ElementDecorator.decorate(((ExecutableElement) this.delegate).getParameters(), this.env);
    if (parameters != null) {
      for (VariableElement param : parameters) {
        if (paramsComments.get(param.getSimpleName().toString()) != null) {
          ((DecoratedVariableElement)param).setDocComment(paramsComments.get(param.getSimpleName().toString()));
        }
      }
    }
    return parameters;
  }

  @Override
  protected JavaDoc constructJavaDoc(String docComment, JavaDocTagHandler handler) {
    if (docComment == null || "".equals(docComment.trim()) || INHERITDOC_PATTERN.matcher(docComment).find()) {
      if (docComment == null) {
        docComment = "";
      }
      docComment = replaceDocInheritance(docComment);
    }

    return super.constructJavaDoc(docComment, handler);
  }

  private String replaceDocInheritance(String currentComment) {
    return replaceDocInheritance(new TreeSet<String>(), currentComment, (TypeElement) this.delegate.getEnclosingElement());
  }

  private String replaceDocInheritance(Set<String> visitedDecls, String currentComment, TypeElement declaringType) {
    if (declaringType != null && commentNeedsReplacement(currentComment)) {
      List<TypeMirror> supers = new ArrayList<TypeMirror>(declaringType.getInterfaces());

      if (declaringType.getSuperclass() != null) {
        supers.add(declaringType.getSuperclass());
      }

      Elements declarations = this.env.getElementUtils();
      Collection<TypeElement> decls = new ArrayList<TypeElement>(supers.size());
      for (TypeMirror inherited : supers) {
        if (inherited instanceof DeclaredType) {
          TypeElement decl = (TypeElement) ((DeclaredType)inherited).asElement();
          if (decl != null && !visitedDecls.contains(decl.getQualifiedName().toString())) {
            visitedDecls.add(decl.getQualifiedName().toString());
            for (ExecutableElement methodDeclaration : ElementFilter.methodsIn(decl.getEnclosedElements())) {
              if (declarations.hides(this.delegate, methodDeclaration)) {
                currentComment = doReplace(currentComment, declarations.getDocComment(methodDeclaration));
                if (!commentNeedsReplacement(currentComment)) {
                  return currentComment;
                }
              }
            }
            decls.add(decl);
          }
        }
      }

      for (TypeElement decl : decls) {
        currentComment = replaceDocInheritance(visitedDecls, currentComment, decl);
        if (!commentNeedsReplacement(currentComment)) {
          return currentComment;
        }
      }
    }

    return currentComment;
  }

  private String doReplace(String currentComment, String replacement) {
    if (replacement == null) {
      replacement = "";
    }

    if ("".equals(currentComment)) {
      return replacement.trim();
    }
    else {
      return INHERITDOC_PATTERN.matcher(currentComment).replaceAll(replacement);
    }
  }

  protected boolean commentNeedsReplacement(String currentComment) {
    return (currentComment == null || "".equals(currentComment.trim()) || INHERITDOC_PATTERN.matcher(currentComment).find());
  }

  @Override
  public List<? extends TypeParameterElement> getTypeParameters() {
    return this.typeParameters;
  }

  @Override
  public TypeMirror getReturnType() {
    return this.typeMirror;
  }

  @Override
  public boolean isVarArgs() {
    return this.delegate.isVarArgs();
  }

  @Override
  public AnnotationValue getDefaultValue() {
    return this.delegate.getDefaultValue();
  }

  public List<? extends VariableElement> getParameters() {
    return this.parameters;
  }

  public List<? extends TypeMirror> getThrownTypes() {
    return this.thrownTypes;
  }

  public boolean isGetter() {
    return ((getSimpleName().toString().startsWith("get") || isIs()) && getParameters().isEmpty());
  }

  private boolean isIs() {
    return getSimpleName().toString().startsWith("is") && getReturnType().getKind() == TypeKind.BOOLEAN;
  }

  public boolean isSetter() {
    return (getSimpleName().toString().startsWith("set") && getParameters().size() == 1);
  }

  public String getPropertyName() {
    String propertyName = null;

    if (isIs()) {
      propertyName = Introspector.decapitalize(getSimpleName().toString().substring(2));
    }
    else if (isGetter() || (isSetter())) {
      propertyName = Introspector.decapitalize(getSimpleName().toString().substring(3));
    }

    return propertyName;
  }

  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitExecutable(this, p);
  }
}
