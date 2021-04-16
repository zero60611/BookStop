package com.example.demo.aop;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.demo.controller.CommonController;
import com.example.demo.dao.SystemLogDao;
import com.example.demo.model.SystemLog;

/**
 * 
 * execution：最主要的表示式，用來匹配方法執行的 Join Point。 within：必須是指定的型態，可用來取代某些 execution 模式。
 * this：代理物件必須是指定的型態，常用於 Advice 中要取得代理物件之時。 target：目標物件必須是指定的型態，常用於 Advice
 * 中要取得目標物件之時。 args：引數必須是指定的型態，常用於 Advice 中要取得方法引數之時。。
 * 
 * @target：目標物件必須擁有指定的標型，常用於 Advice 中要取得標註之時。
 * @args：引數必須擁有指定的標註，常用於 Advice 中要取得標註之時。
 * @within：必須擁有指定的標註，常用於 Advice 中要取得標註之時。
 * @annotation：方法上必須擁有指定的標註，常用於 Advice 中要取得標註之時。 execution(*
 *                              cc.openhome.model.AccountDAO.*(..))
 *                              為例，它表示沒有限定權限修飾，也就是 public、protected、private 或
 *                              default 方法都可以， 第一個 *
 *                              設定傳回型態（ret-type-pattern）可以是任何型態，cc.openhome.model.AccountDAO.*
 *                              指定了名稱模式（name-pattern），其中的 * 表示任何方法，.. 表示任意數量引數。
 */

@Aspect
//@SessionAttributes(value = { "username", "mail", "password" })
public class AopLogging {
	@Autowired
	private SystemLog log;

	@Autowired
	private SystemLogDao dao;

	public AopLogging() {
	}
	// ======財稅案的還沒解決===
//	private final Environment env;
	// ======財稅案的還沒解決===
//	public AopLogging(Environment env) {
//		this.env = env;
//	}

// ======財稅案的還沒解決============================================================================================

//	@Pointcut("execution (* com.example.demo.controller.*.*(..)) && ")
//	@Pointcut("within(@org.springframework.stereotype.Repository *)"
//			+ " || within(@org.springframework.stereotype.Service *)"
//			+ " || within(@org.springframework.web.bind.annotation.RestController *)")
//	public void springBeanPointcut() {
//
//	}
	// ==================================================================================================

	@Pointcut("execution (* com.example.demo.controller.*.*(..))")
	public void applicationPackagePointcut() {

	}

	@Before("applicationPackagePointcut()")
	public void daAround(JoinPoint joinPoint) {
		System.out.println("=====controller @Before通知=====");
		System.out.println("============AOP 分界線測試開始=============");

//==================================================================================================

		Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass().getName());
		logger.info("before methods");
		try {
			// 現在時間
			Date today = new Date();
			// 得到request的相關資訊
			// 請求環境的獲取
			/**
			 * RequestAttributes:Abstraction for accessing attribute objects associated with
			 * a request. Supports access to request-scoped attributes as well as to
			 * session-scoped attributes, with the optional notion of a "global session".
			 * 通過RequestContextHolder直接獲取HttpServletRequest物件
			 * https://my.oschina.net/ojeta/blog/801640
			 */
			RequestAttributes ra = RequestContextHolder.getRequestAttributes();
			ServletRequestAttributes sra = (ServletRequestAttributes) ra;
			// assert 斷言
			assert sra != null;
			// 請求環境去獲取請求
			System.out.println("============AOP 分界線測試截止=============");

			HttpServletRequest request = sra.getRequest();
//			System.out.println(request);
//			String aa = request.toString();
//			System.out.println(aa);

//			Enumeration<String> data = request.getAttributeNames();

			System.out.println("開始取出request內的東西==============>");
			System.out.println(request.getHeader("user-agent"));
//			=========================test=========================

//			Map<String, String> map = new HashMap<String, String>();
//			Enumeration headerNames = request.getHeaderNames();
//			while (headerNames.hasMoreElements()) {
//				String key = (String) headerNames.nextElement();
//				String value = request.getHeader(key);
//				map.put(key, value);
//				System.out.println(map);
//			}

//			=========================test=========================
			// 利用枚舉
			Enumeration headerNames = request.getHeaderNames();
			while (headerNames.hasMoreElements()) {
				System.out.println(
						headerNames.nextElement() + ":" + request.getHeader((String) headerNames.nextElement()));
			}

//			while(data.hasMoreElements()) {
//				System.out.println("--");
//				System.out.println(data.nextElement());
//			}

//			HttpSession session = (HttpSession) RequestContextHolder.currentRequestAttributes()
//					.resolveReference(RequestAttributes.REFERENCE_SESSION);
			// 再從前端請求去得到相關訊息
			// 取得使用者名稱
//			String account = (String) session.getAttribute("account");
			String user = request.getUserPrincipal().getName();
//			System.out.println("sessionsession"+session);

			// 取得使用者訪問路徑
			String url = request.getRequestURL().toString();
			// ip位置
			String ip = request.getRemoteAddr();
//			long startTime = System.currentTimeMillis();

			// 獲取角色資訊

			// 預設角色是null
			String role = null;
			@SuppressWarnings("unchecked")
			Collection<SimpleGrantedAuthority> authorities = (Collection<SimpleGrantedAuthority>) SecurityContextHolder
					.getContext().getAuthentication().getAuthorities();
			for (GrantedAuthority authority : authorities) {
				role = authority.getAuthority();
			}

			if (CommonController.isNullOrSpace(role)) {
				return;

			} else {
				HashMap<String, String> m = new HashMap<>();
				m.put("role", role);
			}

//======================驗證資料=======================================================================================

//			System.out.println("使用者的角色是:" + role);
//			System.out.println("使用者從哪邊訪問過來:" + url  + "\n使用者使用時間:" + today);
//			System.out.println("使用者名稱:" + user);
//			System.out.println("ip位置:" + ip);

			// 獲取連線點目標類名
			String targetName = joinPoint.getTarget().getClass().getName(); // 成功
			// 獲取連線點簽名的方法名
			String methodName = joinPoint.getSignature().getName(); // 寫在BEFORE 寫出日誌//成功
			// 根據連線點類的名字獲取指定類
			@SuppressWarnings("rawtypes")
			Class targetClass = Class.forName(targetName);
			// 獲取類裡面的方法
			Method[] methods = targetClass.getMethods();
//			System.out.println("method[]--->" + methods);
			for (Method method : methods) {
				if (method.getName().equals(methodName)) {
//					System.out.print("請求方法:" + (joinPoint.getTarget().getClass().getName()));
//					System.out.print("請求方法:" + (targetName));
//					System.out.println("-->" + method.getName());
				}
			}
//			=========================test=========================
//			System.out.println("--準備進資料庫");
			// *========存進資料庫=========*//
			log.setMethod(methodName);
			log.setRequestIp(ip);
			log.setCreateBy(user);
			log.setCreateDate(today);
			log.setRole(role);
			log.setCreateDate(today);
			dao.save(log);
//			System.out.println("=====controller處理完存進資料庫=====");
			System.out.println("============分界線測試截止=============");
//	========================================================================================================
		} catch (Exception e) {
			// 紀錄本地異常狀態
			logger.error("訪客操作中");

		}
	}
//	========================================================================================================

//	以下為財稅案寫法(還沒參透)=====================================================================================
//	private Logger logger(JoinPoint joinPoint) {
//		return LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringTypeName());
//	}

//	@AfterThrowing(pointcut = "applicationPackagePointcut() && springBeanPointcut()")
//	public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
//		if (env.acceptsProfiles(Profiles.of(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT))) {
//			logger(joinPoint)
//			.error(
//					"Exception in {}() with cause = \'{}\' and exception = \'{}\'",
//					joinPoint.getSignature().getName(), 
//					e.getCause() != null ? e.getCause() : "NULL", 
//					e.getMessage(), e
//			);
//		}else {
//			 logger(joinPoint)
//             .error(
//                 "Exception in {}() with cause = {}",
//                 joinPoint.getSignature().getName(),
//                 e.getCause() != null ? e.getCause() : "NULL"
//             );
//		}
//	}
//	@Around("applicationPackagePointcut() && springBeanPointcut()")
//	public Object LogAround(ProceedingJoinPoint joinPoint) throws Throwable{
//		Logger log = logger(joinPoint);
//		if(log.isDebugEnabled()) {
//			log.debug("Enter: {}() with argument[s] = {}", joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()));
//		}
//		try {
//			Object result = joinPoint.proceed();
//			if(log.isDebugEnabled()) {
//				  log.debug("Exit: {}() with result = {}", joinPoint.getSignature().getName(), result);
//				  System.out.println("hello AOP");
//			}
//			return result;
//		}catch(IllegalArgumentException e){
//			log.error("Illegal argument: {} in {}()", Arrays.toString(joinPoint.getArgs()), joinPoint.getSignature().getName());
//			throw e;
//		}
//		
//		
//	}

}
