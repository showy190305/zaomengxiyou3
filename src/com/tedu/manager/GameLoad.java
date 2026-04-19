package com.tedu.manager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.ImageIcon;

import com.tedu.element.ElementObj;

/**
 * 游戏资源加载器工具类
 * 提供静态方法用于加载地图、图片、玩家等游戏资源
 * 通过配置文件实现游戏资源的动态加载和管理
 * @author renjj
 */
public class GameLoad {
	/**
	 * 游戏元素管理器单例实例
	 * 用于管理游戏中所有元素对象的添加和维护
	 */
	private static ElementManager em=ElementManager.getManager();
	
	/**
	 * 图片资源映射表
	 * 键为图片标识符，值为对应的ImageIcon对象
	 */
	public static Map<String,ImageIcon> imgMap = new HashMap<>();
	
	/**
	 * 多帧图片序列映射表
	 * 键为图片标识符，值为ImageIcon列表（用于动画）
	 */
	public static Map<String,List<ImageIcon>> imgMaps;

	/**
	 * 属性文件读取器
	 * 用于读取和解析配置文件
	 */
	private static Properties pro =new Properties();	
	/**
	 * 根据地图ID加载对应的地图配置文件
	 * 从配置文件中读取地图元素信息并创建相应的游戏对象
	 * @param mapId 地图编号，用于确定配置文件名称
	 */
	public static void MapLoad(int mapId) {
		// 根据地图ID构建配置文件路径
		String mapName="com/tedu/text/"+mapId+".map";
		// 获取类加载器，用于读取资源文件
		ClassLoader classLoader = GameLoad.class.getClassLoader();
		// 从类路径下获取地图配置文件的输入流
		InputStream maps = classLoader.getResourceAsStream(mapName);
		// 检查配置文件是否存在
		if(maps ==null) {
			System.out.println("配置文件读取异常,请重新安装");
			return;
		}
		try {
			// 清空之前的配置信息
			pro.clear();
			// 从输入流加载属性配置
			pro.load(maps);
			// 获取所有属性键的枚举
			Enumeration<?> names = pro.propertyNames();
			// 遍历所有属性键值对
			while(names.hasMoreElements()) {
				// 获取当前属性键
				String key=names.nextElement().toString();
				System.out.println(pro.getProperty(key));
				// 解析属性值，按分号分割多个地图元素
				String [] arrs=pro.getProperty(key).split(";");
				// 遍历所有地图元素数据
				for(int i=0;i<arrs.length;i++) {
					// 获取地图元素对象工厂
					ElementObj obj=getObj("map");  
					// 创建具体的地图元素对象
					ElementObj element = obj.createElement(key+","+arrs[i]);
					System.out.println(element);
					// 将创建的地图元素添加到元素管理器中
					em.addElement(element, GameElement.MAPS);
				}
			}	
		} catch (IOException e) {
			// 捕获并打印IO异常
			e.printStackTrace();
		}
	}
	/**
	 * 加载游戏所需的所有图片资源
	 * 从配置文件中读取图片路径并创建ImageIcon对象缓存
	 */
	public static void loadImg() {
		// 可以带参数，因为不同的关卡也可能需要不一样的图片资源
		// 定义图片资源配置文件路径
		String texturl="com/tedu/text/GameData.pro";
		// 获取类加载器用于读取资源文件
		ClassLoader classLoader = GameLoad.class.getClassLoader();
		// 获取图片资源配置文件的输入流
		InputStream texts = classLoader.getResourceAsStream(texturl);
		// 清空之前的配置信息
		pro.clear();
		try {
			// 从输入流加载图片路径配置
			pro.load(texts);
			// 获取所有图片标识符的集合
			Set<Object> set = pro.keySet();
			// 遍历所有图片标识符
			for(Object o:set) {
				// 获取当前图片的文件路径
				String url=pro.getProperty(o.toString());
				// 创建ImageIcon对象并存储到图片映射表中
				imgMap.put(o.toString(), new ImageIcon(url));
			}
			
		} catch (IOException e) {
			// 捕获并打印IO异常
			e.printStackTrace();
		}
	}
	/**
	 * 加载玩家对象
	 * 创建初始玩家对象并添加到游戏元素管理器中
	 */
	public static void loadPlay() {
		// 加载对象映射关系
		loadObj();
		// 定义玩家初始位置和类型的字符串参数（暂时硬编码，未放入配置文件）
		String playStr="200,200,paopao";
		// 通过字符串标识获取对应的元素对象工厂
		ElementObj obj=getObj("paopao");
		// 这个字符串是key，也是唯一id，相当于为每个类起了一个唯一的id名称
		// 这个字符串名称一定要和obj.pro中的key相同
		// 根据字符串参数创建具体的玩家对象
		ElementObj play = obj.createElement(playStr);
		// ElementObj play = new Play().createElement(playStr);
		// 解耦，降低代码和代码之间的耦合度，可以通过接口或抽象父类获取实体对象
		// 通过配置文件的耦合，降低代码的耦合度
		// 将创建的玩家对象添加到元素管理器中
		em.addElement(play, GameElement.PLAY);
	}
	
	/**
	 * 根据字符串标识获取对应的元素对象实例
	 * 使用反射机制创建指定类型的ElementObj实例
	 * @param str 元素类型标识符
	 * @return 对应的ElementObj实例，如果创建失败则返回null
	 */
	public static ElementObj getObj(String str) {
		try {
			// 从对象映射表中获取指定标识符对应的类
			Class<?> class1 = objMap.get(str);
			// 使用反射创建类的新实例
			Object newInstance = class1.newInstance();
			// 检查创建的对象是否为ElementObj的实例
			if(newInstance instanceof ElementObj) {
				// 返回转换后的ElementObj实例
				return (ElementObj)newInstance;   //这个对象就和 new Play()等价
//				新建立啦一个叫  GamePlay的类
			}
		} catch (InstantiationException e) {
			// 捕获并打印实例化异常
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// 捕获并打印非法访问异常
			e.printStackTrace();
		}
		// 如果创建失败，返回null
		return null;
	}
	
	/**
	 * 对象类型映射表
	 * 键为对象标识符字符串，值为对应的Class对象
	 * 用于通过字符串标识符动态创建对象实例
	 */
	private static Map<String,Class<?>> objMap=new HashMap<>();
	
	/**
	 * 加载对象类型映射关系
	 * 从配置文件中读取类名映射，建立字符串标识符与Class对象的关联
	 */
	public static void loadObj() {
		// 定义对象类型配置文件路径
		String texturl="com/tedu/text/obj.pro";
		// 获取类加载器用于读取配置文件
		ClassLoader classLoader = GameLoad.class.getClassLoader();
		// 获取对象类型配置文件的输入流
		InputStream texts = classLoader.getResourceAsStream(texturl);
		// 清空之前的配置信息
		pro.clear();
		try {
			// 从输入流加载对象类型配置
			pro.load(texts);
			// 获取所有对象标识符的集合
			Set<Object> set = pro.keySet();
			// 遍历所有对象标识符
			for(Object o:set) {
				// 获取当前标识符对应的类全名
				String classUrl=pro.getProperty(o.toString());
//				使用反射的方式直接将 类进行获取
				// 使用反射加载指定的类
				Class<?> forName = Class.forName(classUrl);
				// 将标识符与Class对象的映射关系存储到映射表中
				objMap.put(o.toString(), forName);
			}
			
		} catch (IOException e) {
			// 捕获并打印IO异常
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// 捕获并打印类找不到异常
			e.printStackTrace();
		}
	}
	
	
	
//	用于测试
	public static void main(String[] args) {
		MapLoad(5);
		
		
		try {
//			通过类路径名称， com.tedu.Play
			Class<?> forName = Class.forName("");
//			通过类名  可以直接访问到这个类
			Class<?> forName1=GameLoad.class;
//			通过实体对象 获取 反射对象
			GameLoad gameLoad = new GameLoad();
			Class<? extends GameLoad> class1 = gameLoad.getClass();
			
			
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	
	
	
	
	
	
	
}