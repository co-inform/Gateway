package eu.coinform.gateway.model.Validation;

import eu.coinform.gateway.model.AccuracyLabel;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;

public class EvaluationValidator implements ConstraintValidator<Evaluation, LinkedHashMap> {

    @Override
    public boolean isValid(LinkedHashMap evaluation, ConstraintValidatorContext constraintValidatorContext) {
        if (evaluation == null) {
            return false;
        }
        String[] fields = {"label", "url", "comment"};
        for (String field : fields) {
            if (!evaluation.containsKey(field) || !(evaluation.get(field) instanceof String)) {
                return false;
            }
            if (field.equals("label")) {
                try {
                    AccuracyLabel.parseString((String) evaluation.get("label"));
                } catch (IllegalArgumentException e) {
                    return false;
                }
            } else if (field.equals("url")) {
                try {
                    URL url = new URL((String) evaluation.get("url"));
                } catch (MalformedURLException ex) {
                    return false;
                }
            }
        }
        return true;
    }
}
