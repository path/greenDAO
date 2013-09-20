package de.greenrobot.daogenerator;

import java.util.ArrayList;
import java.util.List;

public class EnumProperty {
    protected Property property;
    protected String propertyName;
    protected String className;
    protected List<String> imports = new ArrayList<String>();

    private List<Annotation> setterAnnotations = new ArrayList<Annotation>();
    private List<Annotation> getterAnnotations = new ArrayList<Annotation>();

    public EnumProperty(Property property, String propertyName, String className) {
        if(property.getPropertyType() != PropertyType.Int) {
            throw new RuntimeException("Enum properties can only be constructed on top of int properties");
        }
        this.property = property;
        this.propertyName = propertyName;
        this.className = className;
        property.setEnumarated(this);
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

    public EnumProperty constant() {
        property.setConstant(true);
        return this;
    }

    public boolean isConstant() {
        return property.isConstant();
    }

    public List<Annotation> getSetterAnnotations() {
        return setterAnnotations;
    }

    public List<Annotation> getGetterAnnotations() {
        return getterAnnotations;
    }

    public EnumProperty addSetterAnnotation(Annotation annotation) {
        setterAnnotations.add(annotation);
        return this;
    }

    public EnumProperty addSetterGetterAnnotation(Annotation annotation) {
        setterAnnotations.add(annotation);
        getterAnnotations.add(annotation);
        return this;
    }

    public EnumProperty addGetterAnnotation(Annotation annotation) {
        getterAnnotations.add(annotation);
        return this;
    }

    public EnumProperty addImport(String pack) {
        this.imports.add(pack);
        return this;
    }

    public List<String> getImports() {
        return imports;
    }
}
