package com.expedia.adaptivealerting.modelservice.util;

import com.expedia.adaptivealerting.modelservice.dto.detectormapping.Detector;
import com.expedia.adaptivealerting.modelservice.dto.detectormapping.Expression;
import com.expedia.adaptivealerting.modelservice.dto.detectormapping.Field;
import com.expedia.adaptivealerting.modelservice.dto.detectormapping.Operand;
import com.expedia.adaptivealerting.modelservice.dto.detectormapping.Operator;
import com.expedia.adaptivealerting.modelservice.dto.detectormapping.User;
import com.expedia.adaptivealerting.modelservice.test.ObjectMother;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class RequestValidatorTest {
    private com.expedia.adaptivealerting.modelservice.entity.Detector illegalParamsDetector;
    private com.expedia.adaptivealerting.modelservice.entity.Detector legalParamsDetector;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
    }

    private void initTestObjects() {
        val mom = ObjectMother.instance();
        illegalParamsDetector = mom.getIllegalParamsDetector();
        legalParamsDetector = mom.getDetector();
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
    public void testValidateMappingDetector_successful() {
        Detector detector = new Detector(UUID.fromString("aeb4d849-847a-45c0-8312-dc0fcf22b639"));
        RequestValidator.validateMappingDetector(detector);
    }

    @Test
    public void testValidateDetector_successful() {
        RequestValidator.validateDetector(legalParamsDetector);
    }

    @Test
    public void testValidateUser_successful() {
        User user = new User("test-user");
        RequestValidator.validateUser(user);
    }

    @Test(expected = RuntimeException.class)
    public void testValidateUser_id_empty() throws IllegalArgumentException {
        User user = new User("");
        RequestValidator.validateUser(user);
    }

    @Test(expected = RuntimeException.class)
    public void testValidateUser_idContainsWhitespaces() throws IllegalArgumentException {
        User user = new User("test user");
        RequestValidator.validateUser(user);
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

    @Test(expected = IllegalArgumentException.class)
    public void testValidateInvalidDetector_illegal_params() {
        RequestValidator.validateDetector(illegalParamsDetector);
    }
}

