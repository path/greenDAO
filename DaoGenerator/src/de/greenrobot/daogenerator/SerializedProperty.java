package de.greenrobot.daogenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This can be used to auto-generate serialization-deserialization code
 * User: yigit
 */
public class SerializedProperty {
    protected Property property;
    protected String propertyName;
    protected String className;
    protected String genericClassName;
    protected List<String> imports = new ArrayList<String>();

    private List<Annotation> fieldAnnotations = new ArrayList<Annotation>();
    private List<Annotation> setterAnnotations = new ArrayList<Annotation>();
    private List<Annotation> getterAnnotations = new ArrayList<Annotation>();

    public SerializedProperty(Property property, String propertyName, String className) {
        this.property = property;
        this.propertyName = propertyName;
        this.className = className;
        setGenericClassNameFromClassName(className);
        property.setSerialized(this);
        fieldAnnotations.add(new Annotation("SerializedField").withPackage("de.greenrobot.dao.annotations.SerializedField"));
    }

    private static Map<String, String> genericMapping = new HashMap<String, String>();
    static {
        genericMapping.put("Map", "java.util.HashMap");
        genericMapping.put("List", "java.util.ArrayList");
    }
    private static String getGenericClassNameFromClassName(String className) {
        int index = className.indexOf("<");
        if(index == -1) {
            return className;
        }
        String generic = className.substring(0, index);
        //if it has java util, strip it
        if(generic.startsWith("java.util.")) {
            generic = generic.substring("java.util.".length(), generic.length());
        }
        String container = genericMapping.get(generic);
        if(container == null) {
            throw new RuntimeException("cannot find container for " + className);
        }
        return container;
    }

    private void setGenericClassNameFromClassName(String className) {
        genericClassName = getGenericClassNameFromClassName(className);
    }

    public String getGenericClassName() {
        return genericClassName;
    }

    public Property getProperty() {
        return property;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getClassName() {
        return className;
    }

    public SerializedProperty constant() {
        property.setConstant(true);
        return this;
    }

    public boolean isConstant() {
        return property.isConstant();
    }

    public List<Annotation> getFieldAnnotations() {
        return fieldAnnotations;
    }

    public List<Annotation> getSetterAnnotations() {
        return setterAnnotations;
    }

    public List<Annotation> getGetterAnnotations() {
        return getterAnnotations;
    }

    public SerializedProperty addSetterAnnotation(Annotation annotation) {
        setterAnnotations.add(annotation);
        return this;
    }

    public SerializedProperty addSetterGetterAnnotation(Annotation annotation) {
        setterAnnotations.add(annotation);
        getterAnnotations.add(annotation);
        return this;
    }

    public SerializedProperty addGetterAnnotation(Annotation annotation) {
        getterAnnotations.add(annotation);
        return this;
    }

    public SerializedProperty addImport(String pack) {
        this.imports.add(pack);
        return this;
    }

    public List<String> getImports() {
        return imports;
    }
}
