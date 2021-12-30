package com.dominico966.util.vo.fixedString.annotation.handler.interfaces;

import com.dominico966.util.vo.fixedString.annotation.exception.LengthDefineFailedException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionException;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

@FunctionalInterface
public interface LengthDefiner {
    int calculate(Object contextObject, String fixedStringLength) throws LengthDefineFailedException;

    LengthDefiner DEFAULT = (Object contextObject, String fixedStringLength) -> {
        try {
            ExpressionParser expressionParser = new SpelExpressionParser();
            Expression expression = expressionParser.parseExpression(fixedStringLength);
            EvaluationContext context = new StandardEvaluationContext(contextObject);

            Integer ret = expression.getValue(context, Integer.class);
            assert ret != null;

            return ret;
        } catch (ExpressionException e) {
            return Integer.parseInt(fixedStringLength);
        } catch (NullPointerException e) {
            throw new LengthDefineFailedException();
        }
    };
}
