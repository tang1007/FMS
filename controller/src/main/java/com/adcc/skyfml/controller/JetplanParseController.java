package com.adcc.skyfml.controller;

import java.io.File;
import java.util.*;

import com.adcc.skyfml.dao.CptInfoService;
import com.adcc.skyfml.dao.PlanCptWdService;
import com.adcc.skyfml.model.CptInfo;
import com.adcc.skyfml.model.PlanCptWd;
import com.adcc.skyfml.service.JetplanParseRuleVO;
import com.adcc.skyfml.util.DateUtil;
import com.adcc.skyfml.util.ObjectUtil;
import com.adcc.skyfml.util.SysConstants;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * <P>FileName: ParseFlightPlanService.java
 * @author GuoXY
 * <P>CreateTime: 2014-06-07
 * <P>Description: 解析飞行计划服务
 * <P>Version: v1.0
 * <P>History:
 */
public class JetplanParseController implements IFlyPlanParseController
{
    List cptInfoList = new ArrayList();
    List planCptWdList = new ArrayList();

    JetplanParseRuleVO paramVO = new JetplanParseRuleVO();
    Map<String, String> mapAirlinesCodes = new HashMap<String, String>();

    CptInfoService cptInfoService = null;
    PlanCptWdService planCptWdService = null;

    private static Logger debugLogger = Logger.getLogger("forDebug");
    private static Logger errorLogger = Logger.getLogger("forError");

//	public static void main(String[] args) {
//	}

    // 读取配置文件，加载解析Jetplan所需的参数信息
    public JetplanParseController() {

        //        System.out.println("-----------------\r\n@@@@@@@@@@@@@@@@@@@@@@@@@@\r\n-----------------\r\n");
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationcontext-dao.xml");
        cptInfoService = ctx.getBean("cptInfoService", CptInfoService.class);
        planCptWdService = ctx.getBean("planCptWdService", PlanCptWdService.class);

        java.io.File fParseRule = null;
        fParseRule = new java.io.File("controller/config/JetplanParseRule.xml");

        final SAXReader reader = new SAXReader();
        try {
            final Document paraseRulenDoc = reader.read(fParseRule);

            List airlineCodeList = paraseRulenDoc.selectNodes("/JetplanParseRule/AirlinesCodes/AirlineCode");
            Iterator it = airlineCodeList.iterator();
            while(it.hasNext()) {
                Element airlinesCode = (Element) it.next();
//                System.out.println(airlinesCode.attributeValue("id") + " : "  + airlinesCode.getTextTrim());
                mapAirlinesCodes.put(airlinesCode.attributeValue("id"), airlinesCode.getTextTrim());
//                System.out.println("--" + mapAirlinesCodes.get("CCA") + "--");
            }

            Element uniqueObject = (Element)paraseRulenDoc.selectSingleNode("/JetplanParseRule/uniqueObject/planId");
            paramVO.setPlanIdAttribute(uniqueObject.attributeValue("attributeName"));
            paramVO.setPlanIdPath(uniqueObject.getTextTrim());
            uniqueObject = (Element)paraseRulenDoc.selectSingleNode("/JetplanParseRule/uniqueObject/planDate");
            paramVO.setPlanDateAttribute(uniqueObject.attributeValue("attributeName"));
            paramVO.setPlanDatePath(uniqueObject.getTextTrim());

            uniqueObject = (Element)paraseRulenDoc.selectSingleNode("/JetplanParseRule/uniqueObject/flightId");
            paramVO.setFlightIdAttribute(uniqueObject.attributeValue("attributeName"));
            paramVO.setFlightIdPath(uniqueObject.getTextTrim());

            uniqueObject = (Element)paraseRulenDoc.selectSingleNode("/JetplanParseRule/uniqueObject/aircraftId");
            paramVO.setAircraftIdAttribute(uniqueObject.attributeValue("attributeName"));
            paramVO.setAircraftIdPath(uniqueObject.getTextTrim());
            uniqueObject = (Element)paraseRulenDoc.selectSingleNode("/JetplanParseRule/uniqueObject/dep");
            paramVO.setDepAttribute(uniqueObject.attributeValue("attributeName"));
            paramVO.setDepPath(uniqueObject.getTextTrim());
            uniqueObject = (Element)paraseRulenDoc.selectSingleNode("/JetplanParseRule/uniqueObject/arr");
            paramVO.setArrAttribute(uniqueObject.attributeValue("attributeName"));
            paramVO.setArrPath(uniqueObject.getTextTrim());

            Element cptPosInfo = (Element)paraseRulenDoc.selectSingleNode("/JetplanParseRule/cptPosInfo");
            paramVO.setCptPosAttribute(cptPosInfo.attributeValue("attributeName"));
            paramVO.setCptPosPath(cptPosInfo.attributeValue("prefixPath"));
            cptPosInfo = (Element)paraseRulenDoc.selectSingleNode("/JetplanParseRule/cptPosInfo/lon");
            paramVO.setLonAttribute(cptPosInfo.attributeValue("attributeName"));
            paramVO.setLonPath(cptPosInfo.getTextTrim());
            cptPosInfo = (Element)paraseRulenDoc.selectSingleNode("/JetplanParseRule/cptPosInfo/lat");
            paramVO.setLatAttribute(cptPosInfo.attributeValue("attributeName"));
            paramVO.setLatPath(cptPosInfo.getTextTrim());

            Element cptWindTempInfo = (Element)paraseRulenDoc.selectSingleNode("/JetplanParseRule/cptWindTempInfo");
            paramVO.setCptWindTempAttribute(cptWindTempInfo.attributeValue("attributeName"));
            paramVO.setCptWindTempPath(cptWindTempInfo.attributeValue("prefixPath"));
            cptWindTempInfo = (Element)paraseRulenDoc.selectSingleNode("/JetplanParseRule/cptWindTempInfo/windDir");
            paramVO.setWindDirAttribute(cptWindTempInfo.attributeValue("attributeName"));
            paramVO.setWindDirPath(cptWindTempInfo.getTextTrim());
            cptWindTempInfo = (Element)paraseRulenDoc.selectSingleNode("/JetplanParseRule/cptWindTempInfo/windVel");
            paramVO.setWindVelAttribute(cptWindTempInfo.attributeValue("attributeName"));
            paramVO.setWindVelPath(cptWindTempInfo.getTextTrim());
            cptWindTempInfo = (Element)paraseRulenDoc.selectSingleNode("/JetplanParseRule/cptWindTempInfo/windTep");
            paramVO.setWindTepAttribute(cptWindTempInfo.attributeValue("attributeName"));
            paramVO.setWindTepPath(cptWindTempInfo.getTextTrim());
            cptWindTempInfo = (Element)paraseRulenDoc.selectSingleNode("/JetplanParseRule/cptWindTempInfo/alt");
            paramVO.setAltAttribute(cptWindTempInfo.attributeValue("attributeName"));
            paramVO.setAltPath(cptWindTempInfo.getTextTrim());
        } catch (final DocumentException e) {
            // TODO Auto-generated catch block
//			e.printStackTrace();
            errorLogger.error(e.getMessage());
        }
    }

    @Override
    public boolean parseDataSource(File fDataSource) {
        // TODO Auto-generated method stub
        SAXReader readerParseRule = new SAXReader();
        try {
            Document jetplanDoc = readerParseRule.read(fDataSource);

			/*生成CPT_INFO数据*/
            // 飞行计划、计划生成时间在相同的节点内
            Element flyPlan = (Element)jetplanDoc.selectSingleNode(paramVO.getPlanIdPath());
//			System.out.println(flyPlan.getName() + "-->" + flyPlan.attributeValue(paramVO.getPlanIdAttribute()));
//            flyPlan = (Element)jetplanDoc.selectSingleNode(paramVO.getPlanDatePath());
            String planCreateTime = flyPlan.attributeValue(paramVO.getPlanDateAttribute());
            planCreateTime = planCreateTime.replace('T', ' ');
            planCreateTime = planCreateTime.replace('Z', ' ');
//			System.out.println(flyPlan.getName() + "-->" + planCreateTime);

            // 航班号、机尾号、起飞机场、落地机场在相同的节点内
            Element flyInfo = (Element)jetplanDoc.selectSingleNode(paramVO.getFlightIdPath());
//			System.out.println(flyInfo.getName() + "-->" + flyInfo.attributeValue(paramVO.getFlightIdAttribute()));
//            flyInfo = (Element)jetplanDoc.selectSingleNode(paramVO.getAircraftIdPath());
//			System.out.println(flyInfo.getName() + "-->" + flyInfo.attributeValue(paramVO.getAircraftIdAttribute()));
//            flyInfo = (Element)jetplanDoc.selectSingleNode(paramVO.getDepPath());
//			System.out.println(flyInfo.getName() + "-->" + flyInfo.attributeValue(paramVO.getDepAttribute()));
//            flyInfo = (Element)jetplanDoc.selectSingleNode(paramVO.getArrPath());
//			System.out.println(flyInfo.getName() + "-->" + flyInfo.attributeValue(paramVO.getArrAttribute()));

            // 航路点、经度、纬度
            List cptList = jetplanDoc.selectNodes(paramVO.getCptPosPath());
            Iterator it = cptList.iterator();
            while(it.hasNext()){
                Element cptPosNode = (Element)it.next();

//				System.out.println(element.getName() + "-->" + element.attributeValue(paramVO.getCptPosAttribute()));
                Element latPos = (Element) cptPosNode.selectSingleNode(paramVO.getLatPath());
                Float latValue = Float.valueOf(latPos.attributeValue(paramVO.getLatAttribute().split("\\.")[0])
                        + "." + latPos.attributeValue(paramVO.getLatAttribute().split("\\.")[1]));
//				System.out.println( "  " + latPos.getName() + "-->" + latValue);

                Element lonPos = (Element) cptPosNode.selectSingleNode(paramVO.getLonPath());
                Float lonValue = Float.valueOf(lonPos.attributeValue(paramVO.getLonAttribute().split("\\.")[0])
                        + "." + lonPos.attributeValue(paramVO.getLonAttribute().split("\\.")[1]));
//				System.out.println( "  " + lonPos.getName() + "-->" + lonValue);

                CptInfo cptInfoRecord = new CptInfo();
                cptInfoRecord.setPlanId(flyPlan.attributeValue(paramVO.getPlanIdAttribute()));
                cptInfoRecord.setPlanDate(DateUtil.StrToDate(planCreateTime, SysConstants.FORMAT_DATETIME_FULL) );
                cptInfoRecord.setAircraftId(flyInfo.attributeValue(paramVO.getAircraftIdAttribute()));
                cptInfoRecord.setFlightId(flyInfo.attributeValue(paramVO.getFlightIdAttribute()));
                // 从Map（解析规则配置文件）中获取航空公司三字码对应的二字码
                String airline2Code = mapAirlinesCodes.get(cptInfoRecord.getFlightId().substring(0,3));
                if (ObjectUtil.isBlank(airline2Code)) {
                    errorLogger.error("解析规则配置文件中不存在三字码‘" + cptInfoRecord.getFlightId().substring(0,3) + "’所对应的二字码！");
                    return false;
                }
                cptInfoRecord.setAirlines(airline2Code);
                cptInfoRecord.setCptName(cptPosNode.attributeValue(paramVO.getCptPosAttribute()));
                cptInfoRecord.setDep(flyInfo.attributeValue(paramVO.getDepAttribute()));
                cptInfoRecord.setArr(flyInfo.attributeValue(paramVO.getArrAttribute()));
                cptInfoRecord.setLat(latValue);
                cptInfoRecord.setLon(lonValue);

                cptInfoList.add(cptInfoRecord);
            }

			/*生成PLAN_CPT_WD数据*/
            // 风向、风速、风温、高度层
            cptList = jetplanDoc.selectNodes(paramVO.getCptWindTempPath());
            it = cptList.iterator();
            while(it.hasNext()){
                Element cptWdNode = (Element)it.next();
//				System.out.println(cptWdNode.getName() + "-@@@->" + cptWdNode.attributeValue(paramVO.getCptWindTempAttribute()));

                List<Element> subList = cptWdNode.elements();
                for(Element flightLevelNode : subList){
                    PlanCptWd cptWdRecord = new PlanCptWd();
                    //遍历得到每个元素的名字和内容
//					System.out.println( " " + e.getName() + "-->" +  + "   " +  + "   " +  + "   "  + e.attributeValue());
                    cptWdRecord.setPlanId(flyPlan.attributeValue(paramVO.getPlanIdAttribute()));
                    cptWdRecord.setAircraftId(flyInfo.attributeValue(paramVO.getAircraftIdAttribute()));
                    cptWdRecord.setFlightId(flyInfo.attributeValue(paramVO.getFlightIdAttribute()));
                    // 从Map（解析规则配置文件）中获取航空公司三字码对应的二字码
                    String airline2Code = mapAirlinesCodes.get(cptWdRecord.getFlightId().substring(0,3));
                    if (ObjectUtil.isBlank(airline2Code)) {
                        errorLogger.error("解析规则配置文件中不存在三字码‘" + cptWdRecord.getFlightId().substring(0,3) + "’所对应的二字码！");
                        return false;
                    }
                    cptWdRecord.setAirlines(airline2Code);
                    cptWdRecord.setCptName(cptWdNode.attributeValue(paramVO.getCptWindTempAttribute()));
                    cptWdRecord.setWindDir( Float.valueOf(flightLevelNode.attributeValue(paramVO.getWindDirAttribute())) );
                    cptWdRecord.setPlanDate(DateUtil.StrToDate(planCreateTime, SysConstants.FORMAT_DATETIME_FULL) );

                    String cptFlyLevTemp = flightLevelNode.attributeValue(paramVO.getWindTepAttribute());
                    if ('P' == cptFlyLevTemp.charAt(0)) {
                        cptWdRecord.setWindTep( Float.valueOf(cptFlyLevTemp.substring(1)) );
                    } else if ('M' == cptFlyLevTemp.charAt(0)) {
                        cptWdRecord.setWindTep( Float.valueOf("-" + cptFlyLevTemp.substring(1)) );
                    } else {
                        cptWdRecord.setWindTep( Float.valueOf(cptFlyLevTemp) );
                    }
                    cptWdRecord.setWindVel( Float.valueOf(flightLevelNode.attributeValue(paramVO.getWindVelAttribute())) );
                    cptWdRecord.setAlt( Float.valueOf(flightLevelNode.attributeValue(paramVO.getAltAttribute())) );
                    planCptWdList.add(cptWdRecord);
                }
            }
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            errorLogger.error(e.getMessage());
        }
        return true;
    }

    @Override
    public void saveToDB() {
        // TODO Auto-generated method stub

//        System.out.println(cptInfoList.size());
//        Iterator it = cptInfoList.iterator();
//        while(it.hasNext()) {
//            CptInfo cptPosNode = (CptInfo) it.next();
//            System.out.println(cptPosNode);
//        }

//        System.out.println(planCptWdList.size());
//        Iterator it = planCptWdList.iterator();
//        while(it.hasNext()) {
//            PlanCptWd planCptWd = (PlanCptWd) it.next();
//            System.out.println(planCptWd);
//        }

        // CPT_INFO 数据入库
        cptInfoService.save(cptInfoList);
        // PLAN_CPT_WD 数据入库
        planCptWdService.save(planCptWdList);
    }

//    public static void parserOnenoteXml(String fileName)
//    {
//        File inputXml = new File(fileName);
//        SAXReader saxReader = new SAXReader();
//        try
//        {
//            Document document = saxReader.read(inputXml);
//            Element root = document.getRootElement();
//	    	Element subNote = null;
//
//	    	List noteList = root.elements("Checkpoints");
//	    	System.out.println( noteList.size() );
//	    	for (int i = 0; i < noteList.size(); i++) {
//		    	subNote = (Element) noteList.get(i);
//		    	System.out.println( subNote.getName() );
////		    	List listfile = subNote .elements("title ");
////		    	String a=((Element) listfile.get(0)).getTextTrim();
////		    	String b=((Element) listfile.get(1)).getTextTrim();
//	    	}
//        }
//        catch (DocumentException e)
//        {
//            System.out.println(e.getMessage());
//        }
//    }
//
//    public static void createXml(String fileName)
//    {
//        Document document = DocumentHelper.createDocument();
//        Element employees = document.addElement("employees");
//        Element employee = employees.addElement("employee");
//        Element name = employee.addElement("name");
//        name.setText("ddvip");
//        Element sex = employee.addElement("sex");
//        sex.setText("m");
//        Element age = employee.addElement("age");
//        age.setText("29");
//        try
//        {
//            Writer fileWriter = new FileWriter(fileName);
//            XMLWriter xmlWriter = new XMLWriter(fileWriter);
//            xmlWriter.write(document);
//            xmlWriter.close();
//        }
//        catch (IOException e)
//        {
//            System.out.println(e.getMessage());
//        }
//    }
//
//    public static void parserFullXml(String fileName)
//    {
//        File inputXml = new File(fileName);
//        SAXReader saxReader = new SAXReader();
//        try
//        {
//            Document document = saxReader.read(inputXml);
//            Element root = document.getRootElement();
//            for(Iterator i = root.elementIterator(); i.hasNext();)
//            {
//                Element oneNode = (Element) i.next();
//                for(Iterator j = oneNode.elementIterator(); j.hasNext();)
//                {
//                    Element node = (Element) j.next();
//                    System.out.println(node.getName() + ":" + node.getText());
//                }
//            }
//        }
//        catch (DocumentException e)
//        {
//            System.out.println(e.getMessage());
//        }
//    }

}