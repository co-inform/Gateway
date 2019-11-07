package eu.coinform.gateway.model.Validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = EvaluationValidator.class)
@Documented
public @interface Evaluation {
    String message() default "the evaluation object must contain a correct label, url and comment";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
