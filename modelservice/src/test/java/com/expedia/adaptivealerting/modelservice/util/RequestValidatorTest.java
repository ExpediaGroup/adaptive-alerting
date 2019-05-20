package com.expedia.adaptivealerting.modelservice.util;


import com.expedia.adaptivealerting.modelservice.model.Detector;
import com.expedia.adaptivealerting.modelservice.model.Expression;
import com.expedia.adaptivealerting.modelservice.model.Field;
import com.expedia.adaptivealerting.modelservice.model.Operand;
import com.expedia.adaptivealerting.modelservice.model.Operator;
import com.expedia.adaptivealerting.modelservice.model.User;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class RequestValidatorTest {

    @Mock
    private User user;

    @InjectMocks
    private RequestValidator requestValidatorUndertest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testValidateUser_successful() {
        User user = new User("test-user");
        RequestValidator.validateUser(user);
    }

    @Test(expected = RuntimeException.class)
    public void testValidateUser_idempty() throws IllegalArgumentException {
        User user = new User("");
        RequestValidator.validateUser(user);
    }

    @Test(expected = RuntimeException.class)
    public void testValidateUser_idcontainswhitespaces() throws IllegalArgumentException {
        User user = new User("test user");
        RequestValidator.validateUser(user);
    }

    @Test
    public void testValidateExpression_successful() {
        Expression expression = new Expression();
        expression.setOperator(Operator.AND);
        List<Operand> operandsList = new ArrayList<>();
        Operand operand1 = new Operand();
        Operand operand2 = new Operand();
        Operand operand3 = new Operand();
        Field field1 = new Field();
        field1.setKey("name");
        field1.setValue("sample-app");
        Field field2 = new Field();
        field2.setKey("env");
        field2.setValue("prod");
        Field field3 = new Field();
        field3.setKey("type");
        field3.setValue("gauge");
        operand1.setField(field1);
        operand2.setField(field2);
        operand3.setField(field3);
        operandsList.add(operand1);
        operandsList.add(operand2);
        expression.setOperands(operandsList);
        RequestValidator.validateExpression(expression);
    }

    @Test
    public void testValidateDetector_successful() {
        Detector detector = new Detector(UUID.fromString("aeb4d849-847a-45c0-8312-dc0fcf22b639"));
        RequestValidator.validateDetector(detector);
    }

    @Test(expected = RuntimeException.class)
    public void testValidateOperand_keyempty() {
        Expression expression = new Expression();
        expression.setOperator(Operator.AND);
        List<Operand> operandsList = new ArrayList<>();
        Operand testOperand = new Operand();
        testOperand.setField(new Field("", "sample-app"));
        operandsList.add(testOperand);
        expression.setOperands(operandsList);
        RequestValidator.validateExpression(expression);
    }

    @Test(expected = RuntimeException.class)
    public void testValidateOperand_valueempty() {
        Expression expression = new Expression();
        expression.setOperator(Operator.AND);
        List<Operand> operandsList = new ArrayList<>();
        Operand testoperand = new Operand();
        testoperand.setField(new Field("name", ""));
        operandsList.add(testoperand);
        expression.setOperands(operandsList);
        RequestValidator.validateExpression(expression);
    }

    @Test(expected = RuntimeException.class)
    public void testValidateOperand_keystartswithDetectorMappingEntityAAPREFIX() {
        Expression expression = new Expression();
        expression.setOperator(Operator.AND);
        List<Operand> operandsList = new ArrayList<>();
        Operand testOperand = new Operand();
        testOperand.setField(new Field("aa_name", "sample-app"));
        operandsList.add(testOperand);
        expression.setOperands(operandsList);
        RequestValidator.validateExpression(expression);
    }

}
