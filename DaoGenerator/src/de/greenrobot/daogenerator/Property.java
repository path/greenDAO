/*
 * Copyright (C) 2011 Markus Junginger, greenrobot (http://greenrobot.de)
 *
 * This file is part of greenDAO Generator.
 *
 * greenDAO Generator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * greenDAO Generator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with greenDAO Generator.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.greenrobot.daogenerator;

import java.util.ArrayList;
import java.util.List;

/** Model class for an entity's property: a Java property mapped to a data base column. */
public class Property {

    public void setConstant(boolean constant) {
        this.constant = constant;
    }

    public static class PropertyBuilder {
        private final Property property;

        public PropertyBuilder(Schema schema, Entity entity, PropertyType propertyType, String propertyName) {
            property = new Property(schema, entity, propertyType, propertyName);
        }

        public PropertyBuilder(Schema schema, Entity entity, PropertyType propertyType, String className, String propertyName) {
            property = new Property(schema, entity, propertyType, className, propertyName);
        }

        public PropertyBuilder columnName(String columnName) {
            property.columnName = columnName;
            return this;
        }

        public PropertyBuilder columnType(String columnType) {
            property.columnType = columnType;
            return this;
        }

        public PropertyBuilder primaryKey() {
            property.primaryKey = true;
            return this;
        }

        public PropertyBuilder primaryKeyAsc() {
            property.primaryKey = true;
            property.pkAsc = true;
            return this;
        }

        public PropertyBuilder primaryKeyDesc() {
            property.primaryKey = true;
            property.pkDesc = true;
            return this;
        }

        public PropertyBuilder addFieldAnnotation(Annotation annotation) {
            property.fieldAnnotations.add(annotation);
            return this;
        }

        public PropertyBuilder addSetterAnnotation(Annotation annotation) {
            property.setterAnnotations.add(annotation);
            return this;
        }

        public PropertyBuilder addSetterGetterAnnotation(Annotation annotation) {
            property.setterAnnotations.add(annotation);
            property.getterAnnotations.add(annotation);
            return this;
        }

        public PropertyBuilder addGetterAnnotation(Annotation annotation) {
            property.getterAnnotations.add(annotation);
            return this;
        }

        public PropertyBuilder autoincrement() {
            if (!property.primaryKey || property.propertyType != PropertyType.Long) {
                throw new RuntimeException(
                        "AUTOINCREMENT is only available to primary key properties of type long/Long");
            }
            property.pkAutoincrement = true;
            return this;
        }

        public PropertyBuilder unique() {
            property.unique = true;
            return this;
        }

        public PropertyBuilder notNull() {
            property.notNull = true;
            return this;
        }

        public PropertyBuilder constant() {
            property.constant = true;
            return this;
        }

        public PropertyBuilder index() {
            Index index = new Index();
            index.addProperty(property);
            property.entity.addIndex(index);
            return this;
        }

        public PropertyBuilder indexAsc(String indexNameOrNull, boolean isUnique) {
            Index index = new Index();
            index.addPropertyAsc(property);
            if (isUnique) {
                index.makeUnique();
            }
            index.setName(indexNameOrNull);
            property.entity.addIndex(index);
            return this;
        }

        public PropertyBuilder indexDesc(String indexNameOrNull, boolean isUnique) {
            Index index = new Index();
            index.addPropertyDesc(property);
            if (isUnique) {
                index.makeUnique();
            }
            index.setName(indexNameOrNull);
            property.entity.addIndex(index);
            return this;
        }

        public PropertyBuilder useBean() {
            property.useBean = true;
            return this;
        }

        public PropertyBuilder addImport(String imp) {
            property.imports.add(imp);
            return this;
        }

        public PropertyBuilder setConcreteClassName(String className) {
            property.concreteClassName = className;
            return this;
        }

        public Property getProperty() {
            return property;
        }
    }

    private final Schema schema;
    private final Entity entity;
    private PropertyType propertyType;
    private final String propertyName;

    private String columnName;
    private String columnType;
    private String className;
    private String concreteClassName;

    private boolean primaryKey;
    private boolean pkAsc;
    private boolean pkDesc;
    private boolean pkAutoincrement;

    private boolean unique;
    private boolean notNull;
    //means this field does never change. helpful for creating much more efficient updateNotNulls
    private boolean constant;
    private SerializedProperty serialized;
    private EnumProperty enumarated;
    private boolean useBean = false;

    private Entity backingEntity;

    private List<Annotation> fieldAnnotations = new ArrayList<Annotation>();
    private List<Annotation> setterAnnotations = new ArrayList<Annotation>();
    private List<Annotation> getterAnnotations = new ArrayList<Annotation>();
    private List<String> imports = new ArrayList<String>();

    /** Initialized in 2nd pass */
    private String constraints;

    private int ordinal;

    private String javaType;

    public Property(Schema schema, Entity entity, PropertyType propertyType, String propertyName) {
        this(schema, entity, propertyType, null, propertyName);
    }

    public Property(Schema schema, Entity entity, PropertyType propertyType, String className, String propertyName) {
        this.schema = schema;
        this.entity = entity;
        this.propertyName = propertyName;
        this.propertyType = propertyType;
        this.className = className;
        this.concreteClassName = className;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public PropertyType getPropertyType() {
        return propertyType;
    }

    public SerializedProperty getSerialized() {
        return serialized;
    }

    public void setSerialized(SerializedProperty serialized) {
        this.serialized = serialized;
    }

    public EnumProperty getEnumarated() {
        return enumarated;
    }

    public void setEnumarated(EnumProperty enumarated) {
        this.enumarated = enumarated;
    }

    public void setPropertyType(PropertyType propertyType) {
        this.propertyType = propertyType;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public boolean isAutoincrement() {
        return pkAutoincrement;
    }

    public String getConstraints() {
        return constraints;
    }

    public boolean isUnique() {
        return unique;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public boolean isConstant() {
        return constant;
    }

    public boolean isUseBean() { return useBean; }

    public String getJavaType() {
        return javaType;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public Entity getEntity() {
        return entity;
    }

    public String getClassName() {
        return className;
    }

    public String getConcreteClassName() {
        return concreteClassName;
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

    public Entity getBackingEntity() {
        return backingEntity;
    }

    public void setBackingEntity(Entity backingEntity, String propertyName) {
        if(propertyType != PropertyType.ByteArray) {
            throw new RuntimeException("can only serialize byte array properties");
        }
        backingEntity.implementsSerializable();
        this.backingEntity = backingEntity;
    }

    public List<String> getImports() {
        return imports;
    }

    void init2ndPass() {
        initConstraint();
        if (columnType == null) {
            columnType = schema.mapToDbType(propertyType);
        }
        if (columnName == null) {
            columnName = DaoUtil.dbName(propertyName);
        }

        if (propertyType == PropertyType.Enum || propertyType == PropertyType.Class) {
            javaType = className;
        } else {
            if (notNull) {
                javaType = schema.mapToJavaTypeNotNull(propertyType);
            } else {
                javaType = schema.mapToJavaTypeNullable(propertyType);
            }
        }
    }

    private void initConstraint() {
        StringBuilder constraintBuilder = new StringBuilder();
        if (primaryKey) {
            constraintBuilder.append("PRIMARY KEY");
            if (pkAsc) {
                constraintBuilder.append(" ASC");
            }
            if (pkDesc) {
                constraintBuilder.append(" DESC");
            }
            if (pkAutoincrement) {
                constraintBuilder.append(" AUTOINCREMENT");
            }
        }
        // Always have String PKs NOT NULL because SQLite is pretty strange in this respect:
        // One could insert multiple rows with NULL PKs
        if (notNull || (primaryKey && propertyType == PropertyType.String)) {
            constraintBuilder.append(" NOT NULL");
        }
        if (unique) {
            constraintBuilder.append(" UNIQUE");
        }
        String newContraints = constraintBuilder.toString().trim();
        if (constraintBuilder.length() > 0) {
            constraints = newContraints;
        }
    }

    void init3ndPass() {
        // Nothing to do so far
    }

    @Override
    public String toString() {
        return "Property " + propertyName + " of " + entity.getClassName();
    }

}
