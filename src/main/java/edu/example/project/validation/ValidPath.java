package edu.example.project.validation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { PathValidator.class })
public @interface ValidPath {

    String message() default "Invalid path";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};


}
