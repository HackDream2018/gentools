package com.tools.gentool;

import org.codehaus.plexus.util.StringUtils;
import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.config.Context;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author lushaofeng
 * @version V1.0
 * @Description: ${todo}
 * @date 2018/5/14
 */
public class MyExtraSQLPlugin  extends PluginAdapter {
    private static final String QUERY_BY_CONDITION = "queryByCondition";

    @Override
    public void setContext(Context context) {
        super.setContext(context);
        context.getIntrospectedColumnImpl();
    }

    public boolean validate(List<String> list) {
        return true;
    }

    //不生成 insert
    public boolean clientInsertMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return true;
    }
    public boolean sqlMapInsertElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return true;
    }
    public boolean clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return true;
    }
    public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return true;
    }
    //不生成 selectByPrimaryKey
    public boolean clientSelectByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return true;
    }
    public boolean sqlMapSelectByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean sqlMapGenerated(GeneratedXmlFile sqlMap, IntrospectedTable introspectedTable) {
//        String path = sqlMap.getTargetProject() + File.separator + sqlMap.getTargetPackage().replaceAll("\\.", File.separator) + File.separator + sqlMap.getFileName();
//        new File(path).delete();
        return true;
    }

    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        FullyQualifiedJavaType parameterType;

        parameterType = introspectedTable.getRules().calculateAllFieldsClass();
        String name = parameterType.getShortNameWithoutTypeArguments();
        Method method = new Method();
        method.setReturnType(new FullyQualifiedJavaType("List<" + name + ">"));
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName(QUERY_BY_CONDITION);
        importedTypes.add(parameterType);
        importedTypes.add(new FullyQualifiedJavaType("java.util.List"));
        method.addParameter(new Parameter(parameterType, StringUtils.substring(name, 0, 1).toLowerCase() + StringUtils.substring(name, 1)));
        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
        interfaze.addImportedTypes(importedTypes);
        interfaze.addMethod(method);
        return true;
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {

        String tableName = introspectedTable.getFullyQualifiedTableNameAtRuntime();//数据库表名
        FullyQualifiedJavaType parameterType = introspectedTable.getRules().calculateAllFieldsClass();
        XmlElement xmlElement = document.getRootElement();

        // 产生分页语句前半部分
        XmlElement x = new XmlElement("select");
        x.addAttribute(new Attribute("id", QUERY_BY_CONDITION));
        x.addAttribute(new Attribute("parameterType", parameterType.getFullyQualifiedName()));
        x.addAttribute(new Attribute("resultMap", "BaseResultMap"));
        StringBuilder sb = new StringBuilder();
        sb.append("select <include refid=\"Base_Column_List\"/> from ");
        sb.append(tableName);
        x.addElement(new TextElement(sb.toString()));

        XmlElement insertTrimElement = new XmlElement("where");

        for (IntrospectedColumn introspectedColumn : ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns())) {
            XmlElement ifXmlElement = new XmlElement("if");
            sb.setLength(0);
            sb.append(introspectedColumn.getJavaProperty());
            sb.append(" != null");
            ifXmlElement.addAttribute(new Attribute("test", sb.toString()));

            sb.setLength(0);
            sb.append("and ");
            sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            sb.append(" = ");
            sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn));

            ifXmlElement.addElement(new TextElement(sb.toString()));

            insertTrimElement.addElement(ifXmlElement);
        }

        x.addElement(insertTrimElement);
        xmlElement.addElement(x);


        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }
}