package com.signove.health.service;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.annotation.SuppressLint;

import com.yixin.monitors.sdk.api.IDataParser;
import com.yixin.monitors.sdk.model.PackageModel;
import com.yixin.monitors.sdk.model.SignDataModel;

/**
 * 欧姆龙蓝牙数据解析
 * 
 * @author ChenRui
 */
public class OmronXmlParser implements IDataParser {
	
	@SuppressLint("SimpleDateFormat")
	public static String getCurrentDateTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date curDate = new Date(System.currentTimeMillis());
		return formatter.format(curDate);
	}
	
	@Override
	public PackageModel parse(byte[] data) {
		PackageModel mPackageData = new PackageModel();
		List<SignDataModel> listDataModels = new ArrayList<SignDataModel>();	// 当前解析的数据列表
		SignDataModel currentDataModel = null;							// 当前解析的数据
		
		try {
			// 开始解析
			XmlPullParser p = XmlPullParserFactory.newInstance().newPullParser();
			String xml = new String(data);
			StringReader reader = new StringReader(xml);
			p.setInput(reader);
			int type = p.getEventType();
			String name = ""; // 标签名称
			String attrVal = "";// 属性值
			while (type != XmlPullParser.END_DOCUMENT) {
				switch (type) {
					case XmlPullParser.START_TAG:
						name = p.getName();
						attrVal = p.getAttributeValue(null, "name");
						// <meta name="metric-id">18949</meta>
						if ("meta".equals(name) && "metric-id".equals(attrVal)) {
							currentDataModel = new SignDataModel(); // 实例化当前解析数据对象
						}
						break;
					case XmlPullParser.TEXT:
						String text = p.getText().trim();
						if (currentDataModel == null || text.length() <= 0) {
							break;
						}
						else if ("18949".equals(text)) {
							currentDataModel.setDataName("收缩压"); // 高压
							currentDataModel.setUnit("mmHg");
						}
						else if ("18950".equals(text)) {
							currentDataModel.setDataName("舒张压"); // 低压
							currentDataModel.setUnit("mmHg");
						}
						else if ("18474".equals(text)) {
							currentDataModel.setDataName("脉搏");
							currentDataModel.setUnit("博/分");
						}
						else if ("value".equals(name)) {
							int val = (int) Float.parseFloat(text);
							currentDataModel.setValue(String.valueOf(val)); // 设置体征值
							listDataModels.add(currentDataModel);
							currentDataModel = null;
						}
						else if ("18951".equals(text) || "61458".equals(text)) {
							currentDataModel = null;
						}
						
						break;
					default:
						break;
				}
				
				type = p.next();
			}
		}
		catch (XmlPullParserException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			mPackageData.setSignDatas(listDataModels);
		}
		
		return mPackageData;
	}
	
}
