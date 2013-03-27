package de.greenrobot.dao.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * add this annotation to a field that will be serialized into db
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface SerializedField {
}
