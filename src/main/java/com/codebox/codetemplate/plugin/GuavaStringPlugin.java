//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.codebox.codetemplate.plugin;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.IntrospectedTable.TargetRuntime;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class GuavaStringPlugin extends PluginAdapter {

    public GuavaStringPlugin() {
    }

    public void setProperties(Properties properties) {
        super.setProperties(properties);
    }

    public boolean validate(List<String> warnings) {
        return true;
    }

    private void addAnnotations(TopLevelClass topLevelClass) {
        topLevelClass.addImportedType("com.google.common.base.MoreObjects");
    }

    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.addAnnotations(topLevelClass);
        this.generateToString(introspectedTable, topLevelClass);
        return true;
    }

    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.generateToString(introspectedTable, topLevelClass);
        return true;
    }

    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.generateToString(introspectedTable, topLevelClass);
        return true;
    }

    private void generateToString(IntrospectedTable introspectedTable, TopLevelClass topLevelClass) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getStringInstance());
        method.setName("toString");
        if (introspectedTable.isJava5Targeted()) {
            method.addAnnotation("@Override");
        }

        if (introspectedTable.getTargetRuntime() == TargetRuntime.MYBATIS3_DSQL) {
            this.context.getCommentGenerator().addGeneralMethodAnnotation(method, introspectedTable, topLevelClass.getImportedTypes());
        } else {
            this.context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
        }

        method.addBodyLine("return MoreObjects.toStringHelper(this)");
        StringBuilder sb = new StringBuilder();
        Iterator var5 = topLevelClass.getFields().stream().filter(o -> !o.getName().equals("serialVersionUID")).iterator();

        while (var5.hasNext()) {
            Field field = (Field) var5.next();
            String property = field.getName();
            sb.setLength(0);
            sb.append("        .add(\"").append(property).append("\"").append(", ").append(property).append(")");
            method.addBodyLine(sb.toString());
        }

        method.addBodyLine("        .toString();");

        topLevelClass.addMethod(method);
    }
}
