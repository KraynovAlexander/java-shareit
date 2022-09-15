package ru.practicum.shareit.booking.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ValueOfEnumOrNullValidator implements ConstraintValidator<ValueOfEnum, CharSequence> {
    private List<String> acceptedValues;
    boolean isNullEnabled;

    @Override
    public void initialize(ValueOfEnum annotation) {
        acceptedValues = Stream.of(annotation.enumClass().getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toList());

        if (annotation.isNullEnabled()) isNullEnabled = true;
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null && isNullEnabled) {
            return true;
        }

        assert value != null;
        return acceptedValues.contains(value.toString().toUpperCase());
    }
}
