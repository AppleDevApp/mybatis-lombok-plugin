package com.codebox.codetemplate.plugin;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class GuavaHashPlugin extends PluginAdapter {

    public GuavaHashPlugin() {
    }

    public void setProperties(Properties properties) {
        super.setProperties(properties);
    }

    public boolean validate(List<String> warnings) {
        return true;
    }

    private void addAnnotations(TopLevelClass topLevelClass) {
        topLevelClass.addImportedType("com.google.common.base.Objects");
    }

    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.addAnnotations(topLevelClass);
        List columns;
        if (introspectedTable.getRules().generateRecordWithBLOBsClass()) {
            columns = introspectedTable.getNonBLOBColumns();
        } else {
            columns = introspectedTable.getAllColumns();
        }

        this.generateEquals(topLevelClass, columns, introspectedTable);
        this.generateHashCode(topLevelClass, columns, introspectedTable);
        return true;
    }

    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.generateEquals(topLevelClass, introspectedTable.getPrimaryKeyColumns(), introspectedTable);
        this.generateHashCode(topLevelClass, introspectedTable.getPrimaryKeyColumns(), introspectedTable);
        return true;
    }

    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.generateEquals(topLevelClass, introspectedTable.getAllColumns(), introspectedTable);
        this.generateHashCode(topLevelClass, introspectedTable.getAllColumns(), introspectedTable);
        return true;
    }

    protected void generateEquals(TopLevelClass topLevelClass, List<IntrospectedColumn> introspectedColumns, IntrospectedTable introspectedTable) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getBooleanPrimitiveInstance());
        method.setName("equals");
        method.addParameter(new Parameter(FullyQualifiedJavaType.getObjectInstance(), "o"));
        if (introspectedTable.isJava5Targeted()) {
            method.addAnnotation("@Override");
        }

        if (introspectedTable.getTargetRuntime() == IntrospectedTable.TargetRuntime.MYBATIS3_DSQL) {
            this.context.getCommentGenerator().addGeneralMethodAnnotation(method, introspectedTable, topLevelClass.getImportedTypes());
        } else {
            this.context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
        }

        method.addBodyLine("if (this == o) return true;");
        method.addBodyLine("if (o == null || getClass() != o.getClass()) return false;");

        String className = topLevelClass.getType().getShortName();
        String targetName = decapitalize(className);

        StringBuilder sb = new StringBuilder();
        sb.append(topLevelClass.getType().getShortName()).append(" ").append(targetName);
        sb.append(" = (").append(className).append(") o;");
        method.addBodyLine(sb.toString());

        Iterator iter = introspectedColumns.stream().filter(o -> !o.getJavaProperty().equals("serialVersionUID")).iterator();

        int fieldCount = 0;
        List<IntrospectedColumn> primitiveTypeList = new ArrayList<>();
        List<IntrospectedColumn> objectTypeList = new ArrayList<>();
        while (iter.hasNext()) {
            IntrospectedColumn introspectedColumn = (IntrospectedColumn) iter.next();
            FullyQualifiedJavaType fqjt = introspectedColumn.getFullyQualifiedJavaType();

            fieldCount++;

            if (fqjt.isPrimitive()) {
                primitiveTypeList.add(introspectedColumn);
            } else {
                objectTypeList.add(introspectedColumn);
            }
        }

        List<IntrospectedColumn> sortColumns = new ArrayList<>();
        sortColumns.addAll(primitiveTypeList);
        sortColumns.addAll(objectTypeList);

        //此处是为了使用跟guava生成算法一致，基础类型先，然后再对象类型，基础类型速度比对象快
        int nowCount = 0;
        iter = sortColumns.iterator();
        while (iter.hasNext()) {
            IntrospectedColumn introspectedColumn = (IntrospectedColumn) iter.next();
            FullyQualifiedJavaType fqjt = introspectedColumn.getFullyQualifiedJavaType();
            String property = introspectedColumn.getJavaProperty();
            sb.setLength(0);

            nowCount++;

            if (nowCount == 1) {
                sb.append("return ");
            } else {
                sb.append("        ");
            }

            if (fqjt.isPrimitive()) {
                if ("double".equals(fqjt.getFullyQualifiedName())) {
                    sb.append("Double.compare(").append(targetName).append(".").append(property).append(", ").append(property).append(") == 0");
                } else if ("float".equals(fqjt.getFullyQualifiedName())) {
                    sb.append("Float.compare(").append(targetName).append(".").append(property).append(", ").append(property).append(") == 0");
                } else {
                    sb.append(property).append(" == ").append(targetName).append(".").append(property);
                }
            } else {
                sb.append("Objects.equal(").append(property).append(", ").append(targetName).append(".").append(property).append(")");
            }

            if (nowCount < fieldCount) {
                sb.append(" &&");
            } else {
                sb.append(";");
            }

            method.addBodyLine(sb.toString());
        }

        topLevelClass.addMethod(method);
    }

    protected void generateHashCode(TopLevelClass topLevelClass, List<IntrospectedColumn> introspectedColumns, IntrospectedTable introspectedTable) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setName("hashCode");
        if (introspectedTable.isJava5Targeted()) {
            method.addAnnotation("@Override");
        }

        if (introspectedTable.getTargetRuntime() == IntrospectedTable.TargetRuntime.MYBATIS3_DSQL) {
            this.context.getCommentGenerator().addGeneralMethodAnnotation(method, introspectedTable, topLevelClass.getImportedTypes());
        } else {
            this.context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("return Objects.hashCode(");

        Iterator iter = topLevelClass.getFields().stream().filter(o -> !o.getName().equals("serialVersionUID")).iterator();

        while (iter.hasNext()) {
            Field field = (Field) iter.next();
            String property = field.getName();
            sb.append(property);

            if (iter.hasNext()) {
                sb.append(", ");
            }
        }

        sb.append(");");

        method.addBodyLine(sb.toString());
        topLevelClass.addMethod(method);
    }

    private static String decapitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }

        //如果发现类的前两个字符都是大写，则直接返回类名
        if (name.length() > 1 && Character.isUpperCase(name.charAt(1)) &&
                Character.isUpperCase(name.charAt(0))) {
            return name;
        }

        // 将类名的第一个字母转成小写，然后返回
        char chars[] = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }
}
