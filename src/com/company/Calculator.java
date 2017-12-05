package com.company;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @Author:LUJIPENG
 * @Description:
 * @Date:Created in 18:36 2017/11/27
 * @Modified By:
 */
public class Calculator {
    private String inputString;
    private String resultString;
    private LinkedList<String> numbersLinkedList;
    private LinkedList<String> operationalLinkedList;

    /**
     * 接收用户输入的运算表达式,如：1*（2+5*6）/85-56*8
     *
     * */
    public void input(){
        System.out.println("请输入运算式：");
        inputString = new Scanner(System.in).nextLine();
        try {
            adjustInputString();
        }catch (LuException le){
            le.showExceptionToUser();
            input();
        }
    }

    /**
     * 对用户的一些不当但是正确的输入进行矫正。
     * 如：中文字符的（）,带有空格的表达式：1 + 1，
     * 第一个字符是+*号：+2+1
     * *和÷前面不能是+-，如：1-*2这种情况是错误的抛出异常
     * +和-不能重复出现，如：1+++++1
     * 如果表达式中存在非法字符如：a+1，1+1、抛出异常
     * 如果左右括号不匹配抛出异常
     * */
    private void adjustInputString(){
        if (inputString.contains("（") || inputString.contains("）")){
            inputString = (inputString.replace('（','(')).replace('）',')');
        } //将中文字符（）换成()
        inputString = inputString.replaceAll("\\s*",""); //利用正则表达式将运算表达式中的空格去除
        if (Pattern.compile("[\\+\\*/]").matcher(inputString).lookingAt()){
            inputString = inputString.substring(1,inputString.length());
        }//用户输入运算表达式中第一个是+，如：+1+1的时候将第一个+消除
        if (Pattern.compile(".*[\\+\\*-/]$").matcher(inputString).matches()){
            inputString = inputString.substring(0,inputString.length()-1);
        }//用户输入运算符表达式中最后一个是运算符，如：1+1+的时候消除最后一个
        if (Pattern.compile("[^\\d][\\*/]").matcher(inputString).find()){
            throw new LuException("*÷前面带了符号，请你认真检查！");
        }
        if (Pattern.compile("[\\+-]{2,}").matcher(inputString).find()){
            throw new LuException("+,-重复出现，请你认真检查");
        }
        if (Pattern.compile("[^\\*\\+\\d-/()]").matcher(inputString).find()){  //利用正则表达式判断字符串中是否含字母
            throw new LuException("表达式中存在非法字符，请你认真检查！");
        }
        if (getCharNumber("(") != getCharNumber(")")){
            throw new LuException("括号不匹配啊！");
        }
    }

    /**
     * 得到输入字符串中，字符s的个数
     * */
    private int getCharNumber(String s){
        return  inputString.length()-inputString.replace(s,"").length();
    }

    /**
     * 按照()将运算表达式分成若干个Unit，计算一个Unit以后就将其重新放入resultString中
     * */
    public String getResult(){
        resultString = inputString;
        String part;
        String partResult;
        int indexLeft;
        int indexRight;
        do {
            indexLeft = resultString.lastIndexOf("(");
            indexRight = resultString.indexOf(")");
            part = getUnitString(indexLeft,indexRight);
            partResult = processUnit(part);
            adjustResultString(partResult,indexLeft,indexRight);

        }while (indexLeft != -1);


       return resultString;
    }

    /**
     * 得到一个Unit。一个表达式中可能有很多的Unit
     * 根据()来确定一个Unit
     * */
    private String getUnitString(int indexLeft,int indexRight){

        if (indexLeft != -1 && indexRight != -1){
            return resultString.substring(indexLeft+1,indexRight);
        }
        return resultString;
    }

    /**
     * 得到一个Unit的计算结果以后，就将计算结果添加到result中,如果indexleft为-1，说明已经没括号了
     * 此时result就是partresult了
     * */
    private void adjustResultString(String partResult,int indexLeft,int indexRight){
        if (indexLeft != -1){
            StringBuffer stringBuffer = new StringBuffer(resultString);
            stringBuffer.delete(indexLeft,indexRight+1);
            stringBuffer.insert(indexLeft,partResult);
            resultString = stringBuffer.toString();
        }else {
            resultString = partResult;
        }

    }

    /**
     * 此方法用来计算一个单元的结果。我们将一个不带括号的表达式称之为一个单元。比如：上面注释中的哪个运算式子中2+5*6就是一个单元
     * 通过将此字符串进行拆分，获得操作数部分与运算符部分然后确定运算顺序
     * */
    private String processUnit(String unit){
        getNumberAndOperationLinkedList(unit);
        int operateIndex;
        while (operationalLinkedList.size() > 0){
            operateIndex = getProcessOperateIndex(0);
            getPartResult(operateIndex);
        }
        return numbersLinkedList.get(0);
    }

    /**
     * 获得Unit的运算符部分与操作数部分
     * 难点在于负数的存在。负数可能出现在首位如-1+2，或者1*-1这种。如果不加区别对待一定会出现一些bug的
     * 因此必须进行处理。
     * */
    private void getNumberAndOperationLinkedList(String unit){
        String[] numbers = unit.split("[\\+|\\*|/|-]");
        String[] operations = unit.split("\\d+");
        operationalLinkedList = new LinkedList<>(Arrays.asList(operations));
        //注意此时拆分出来的运算符由于split方法的原因。第一个子串会是空串。
        numbersLinkedList = new LinkedList<>(Arrays.asList(numbers));

        int offset = 0;
        for (int i = 0; i < numbers.length; i++) {
            if (numbers[i].equals("")){
                numbersLinkedList.remove(i-offset);
                numbersLinkedList.remove(i-offset);
                numbersLinkedList.add(i-offset,String.valueOf(-Integer.parseInt(numbers[i+1])));
                if (i != 0){
                    operationalLinkedList.remove(i-offset);
                    operationalLinkedList.add(i-offset,String.valueOf(operations[i-offset].charAt(0)));
                }
                offset++;
            }
        }
        //通过实验发现，在split以后的数组中只要出现“”的时候后面那个数字就是负数，同时我们可以将“”去除。一举两得。到这里数字已经没问题了开始处理操作符

        //当首位是正数的时候OperaList的第一位会是“”，当是负数的时候第一位是“-”。不管什么情况都需删除
        if (!operationalLinkedList.isEmpty())  //之所以加这个是因为split处理如：1这种表达式的时候Operation里面会是空
        operationalLinkedList.removeFirst();
    }

    /**
     * 因为+-*\/的优先级并不相同，所以计算的顺序不可能是从左到右，当possibleIndex处的运算符是+-的时候
     * 需判断possibleIndex+1处的运算符是不是+-；如果是就返回possibleIndex，否则返回possibleindex+1
     * 不过这里需要判断possibleIndex是不是已经是最后一个了，如果是的话就直接返回possibleIndex。否则possibleIndex+1就数组越界
     * */
    private int getProcessOperateIndex(int possibleIndex){
        int trueIndex = possibleIndex;
        try {
            String possible1 = operationalLinkedList.get(possibleIndex);
            String possible2 = operationalLinkedList.get(possibleIndex+1);
            if ((possible1.equals("+")  || possible1.equals("-")) && (possible2.equals("*") || possible2.equals("/"))){
                trueIndex = possibleIndex+1;
            }//对+-运算符的后面一个运算符是*或者除的时候运算顺序就会改变，此时trueIndex将会变成possibleIndex+1
        }catch (IndexOutOfBoundsException e){
            return possibleIndex;
        }
        return trueIndex;
    }

    /**
     * 选择好了进行哪一个操作就开始选取该操作符前后的number进行该运算符的操作
     * 并对相应结构进行改变
     * */
    private void getPartResult(int operateIndex){
        int number1 = Integer.parseInt(numbersLinkedList.get(operateIndex));
        int number2 = Integer.parseInt(numbersLinkedList.get(operateIndex+1));
        String operationalString = operationalLinkedList.get(operateIndex);

        int result = 0;
        if (operationalString.equals("+") ){
            result = number1 + number2;
        }else if(operationalString.equals("-")){
            result = number1 - number2;
        }else if(operationalString.equals("*")){
            result = number1 * number2;
        }else if (operationalString.equals("/")){
            result = number1 / number2;
        }
        //得到了计算结果以后，将已经使用了的运算符，操作数移除。并进结果插入到number
        operationalLinkedList.remove(operateIndex);
        numbersLinkedList.remove(operateIndex);
        numbersLinkedList.remove(operateIndex);  //本应该删除operateIndex后面的哪一个操作数，但是删除一个以后后面那个数字会向前移
        numbersLinkedList.add(operateIndex,String.valueOf(result));
    }
}
