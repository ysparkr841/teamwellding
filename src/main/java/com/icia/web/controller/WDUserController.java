package com.icia.web.controller;


import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.icia.common.util.StringUtil;
import com.icia.web.model.Response;
import com.icia.web.model.WDRez;
import com.icia.web.model.WDUser;
import com.icia.web.service.WDCouponService;
import com.icia.web.service.WDRezService;
import com.icia.web.service.WDUserService;
import com.icia.web.util.CookieUtil;
import com.icia.web.util.HttpUtil;
import com.icia.web.util.JsonUtil;

@Controller("WDUserController")
public class WDUserController 
{
	private static Logger logger = LoggerFactory.getLogger(WDUserController.class);
	
	//쿠키명
	@Value("#{env['auth.cookie.name']}")
	private String AUTH_COOKIE_NAME;
	
	@Autowired
	private WDUserService wduserService;
	
	@Autowired
	private WDCouponService wdcouponservice;
	
	@Autowired
	private WDRezService wdRezService;
	
	@RequestMapping(value="/imokay", method=RequestMethod.POST)
	@ResponseBody
	public Response<Object> login(ModelMap model, HttpServletRequest request, HttpServletResponse response)
	{
		String userId = HttpUtil.get(request, "userId");
		String userPwd = HttpUtil.get(request, "userPwd");
		Response<Object> ajaxResponse = new Response<Object>();
		
		String userName = "";
		
		if(!StringUtil.isEmpty(userId) && !StringUtil.isEmpty(userPwd)) 
		{
			WDUser user = wduserService.userSelect(userId);
			
			if(user != null)
			{
				if(StringUtil.equals(user.getUserPwd(), userPwd))
				{
					if(StringUtil.equals(user.getStatus(), "Y"))
					{
						CookieUtil.addCookie(response, "/", -1, AUTH_COOKIE_NAME, CookieUtil.stringToHex(userId));
						userName = user.getUserName();
						model.addAttribute("userName",userName);
						ajaxResponse.setResponse(0, "Success"); // 로그인 성공
					}
					else
					{
						ajaxResponse.setResponse(403, "Not Found"); // 정지된 아이디!
					}
				}
				else
				{
					ajaxResponse.setResponse(-1, "Passwords do not match"); // 비밀번호 불일치
				}
			}
			else
			{
				ajaxResponse.setResponse(404, "Not Found"); // 사용자 정보 없음 (Not Found)
			}
		}
		else
		{
			ajaxResponse.setResponse(400, "Bad Request"); // 파라미터가 올바르지 않음 (Bad Request)
		}
		
		if(logger.isDebugEnabled())
		{
			logger.debug("[WDUserController] /login response\n" + JsonUtil.toJsonPretty(ajaxResponse));
		}
		
		
		return ajaxResponse;		
	}
	
	//로그아웃
	@RequestMapping(value="/loginOut", method=RequestMethod.GET)
	public String loginOut(HttpServletRequest request, HttpServletResponse response)
	{
		if(CookieUtil.getCookie(request, AUTH_COOKIE_NAME) != null)
		{
			CookieUtil.deleteCookie(request, response, "/", AUTH_COOKIE_NAME);
		}
		
		return "redirect:/";
	}
	//회원가입
	@RequestMapping(value="/user/regform", method=RequestMethod.GET)
	public String regform(HttpServletRequest request, HttpServletResponse response)
	{
		String cookieUserId = CookieUtil.getHexValue(request,  AUTH_COOKIE_NAME);
		
		if(StringUtil.isEmpty(cookieUserId))
		{
			return "/user/regform";
		}
		else
		{
			CookieUtil.deleteCookie(request, response,  AUTH_COOKIE_NAME);	
			
			return "redirect:/";
		}
	}
	

	//중복아이디 체크
		@RequestMapping(value="/user/idCheck", method=RequestMethod.POST)
		@ResponseBody
		public Response<Object> idCheck(HttpServletRequest request, HttpServletResponse response)
		{
			String userId = HttpUtil.get(request, "userId");
			Response<Object> ajaxResponse = new Response<Object>();
			
			if(!StringUtil.isEmpty(userId))
			{
				if(wduserService.userSelect(userId) == null)
				{
					
					ajaxResponse.setResponse(0, "Success");
				}
				else
				{
					ajaxResponse.setResponse(100, "Duplicate ID");
				}
			}
			else
			{
				ajaxResponse.setResponse(400, "Bad Request");
			}
			
			return ajaxResponse;
		}
		
	//회원가입
	@RequestMapping(value="/user/regProc", method = {RequestMethod.GET, RequestMethod.POST})
	@ResponseBody
	public Response<Object> regProc(HttpServletRequest request, HttpServletResponse response)
	{
		Response<Object> ajaxResponse = new Response<Object>();
		String userId = HttpUtil.get(request, "id", "");
		String userPwd = HttpUtil.get(request, "pwd1", "");
		String userName = HttpUtil.get(request, "name", "");
		String phone = HttpUtil.get(request, "number", "");
		String year = HttpUtil.get(request, "year", "");
		String month = HttpUtil.get(request, "month", "");
		String day = HttpUtil.get(request, "day", "");
		String marry = year + month + day;
		
		
		String gender = HttpUtil.get(request, "gender", "");
		String nickName = HttpUtil.get(request, "nickname", "");
		String email = HttpUtil.get(request, "email", "");
		int uCheck = HttpUtil.get(request, "uCheck", 0);
		
		
		WDUser wdUser = new WDUser();
		
		wdUser.setUserId(userId);
		wdUser.setUserPwd(userPwd);
		wdUser.setUserName(userName);
		wdUser.setUserNumber(phone);
		wdUser.setMarrytDate(marry);
		wdUser.setUserGender(gender);
		wdUser.setUserNickname(nickName);
		wdUser.setUserEmail(email);
		wdUser.setStatus("Y");
		wdUser.setuCheck(uCheck);
		
		if(!StringUtil.isEmpty(userId) && !StringUtil.isEmpty(userPwd) && !StringUtil.isEmpty(userName) && !StringUtil.isEmpty(phone) &&
			!StringUtil.isEmpty(marry) && !StringUtil.isEmpty(gender) && !StringUtil.isEmpty(nickName) && !StringUtil.isEmpty(email) &&
			!StringUtil.isEmpty(uCheck)) 
		{
			System.out.println("다 들어왔어용 : "+userId );
			System.out.println("체크 값 : "+wdUser.getuCheck());
			if(wduserService.userInsert(wdUser) > 0) {
				ajaxResponse.setResponse(0, "Success");
				wdcouponservice.couponInsert(userId);
			}
			else {
				ajaxResponse.setResponse(500, "Bad Request");
			}
		}
		else {
			ajaxResponse.setResponse(400, "Bad Request");
		}		
		
		return ajaxResponse;
	}
	
	@RequestMapping(value="/user/update")
	@ResponseBody
	public Response<Object> modify(HttpServletRequest request, HttpServletResponse response){
		
		Response<Object> ajaxResponse = new Response<Object>();
		
		
		String cookieUserId = CookieUtil.getHexValue(request,  AUTH_COOKIE_NAME);
		
		String userPwd = HttpUtil.get(request, "pwd1");
		String userName = HttpUtil.get(request, "name");
		String phone = HttpUtil.get(request, "number");
		String year = HttpUtil.get(request, "year");
		String month = HttpUtil.get(request, "month");
		String day = HttpUtil.get(request, "day");
		String marry = year + month + day;
		String nickName = HttpUtil.get(request, "nickname");
		String email = HttpUtil.get(request, "email");
				
		
		WDUser wdUser = null;
		wdUser = wduserService.userSelect(cookieUserId);
		System.out.println("아이디 : "+ wdUser.getUserId());
		
		if(!StringUtil.isEmpty(wdUser.getUserId())) 
		{
			if(!StringUtil.isEmpty(userPwd) && !StringUtil.isEmpty(userName) && !StringUtil.isEmpty(phone) &&
					!StringUtil.isEmpty(marry) && !StringUtil.isEmpty(nickName) && !StringUtil.isEmpty(email)) 
			{
				wdUser.setUserPwd(userPwd);
				wdUser.setUserName(userName);
				wdUser.setUserNumber(phone);
				wdUser.setMarrytDate(marry);
				wdUser.setUserNickname(nickName);
				wdUser.setUserEmail(email);
			
				if(wduserService.userUpdate(wdUser) > 0) 
				{
					ajaxResponse.setResponse(0, "Success");
				}
				else 
				{
					//유저정보 업뎃실패
					ajaxResponse.setResponse(500, "Bad Request");
				}
			}
			else 
			{
				//pw,이름,번호,결혼날짜,닉네임,이메일 중 하나라도 못받아오면 오류
				ajaxResponse.setResponse(400, "Bad Request");
			}	
		}
		else 
		{
			//아이디 못받아옴
			ajaxResponse.setResponse(500, "Bad Request");
		}
		
		return ajaxResponse;
		
	}
	
	@RequestMapping(value="/user/emailCheck")
	@ResponseBody
	public Response<Object> checking(HttpServletRequest request, HttpServletResponse response){
		
		Response<Object> ajaxResponse = new Response<Object>();
		int uCheck = HttpUtil.get(request, "uCheck", 0);
		
		WDUser wduser = null;
		
		wduser = wduserService.checkSelect(uCheck);
		
		if(uCheck == wduser.getuCheck()) {
			ajaxResponse.setResponse(0, "Success");
		}
		else {
			ajaxResponse.setResponse(-1, "Bad Request");
		}
		
		return ajaxResponse;
	}
	
	@RequestMapping(value = "/user/userDrop")
	public String userDrops(HttpServletRequest request, HttpServletResponse response)
	{
		return "/user/userDrop";
	}
	
	@RequestMapping(value = "/user/userDropProc")
	@ResponseBody
	public Response<Object> userDropProc(HttpServletRequest request, HttpServletResponse response)
	{
		Response<Object> ajaxResponse = new Response<Object>();
		
		String cookieUserId = CookieUtil.getHexValue(request,  AUTH_COOKIE_NAME);
		String userPwd = HttpUtil.get(request, "userPwd", "");
		
		WDUser user = wduserService.userSelect(cookieUserId);
		if(user != null) 
		{
			int cnt = wdRezService.checkRezWdate(user.getUserId());
			if(cnt <= 0) 
			{
				System.out.println(cnt);
				//cnt가 0이면, 결제한 결제내역의 결혼예정일이 현재 날짜보다 큰게 없음
				//즉, 결제한 결혼식이 이미 다 진행이 된 것!
				//그럼 삭제 쌉가능
				WDUser wdUser = new WDUser();
				
				wdUser.setUserId(cookieUserId);
				wdUser.setUserPwd(userPwd);
				if(!StringUtil.isEmpty(userPwd)) 
				{
					if(wduserService.userDrop(wdUser) > 0) 
					{
						ajaxResponse.setResponse(0, "Success");
						if(CookieUtil.getCookie(request, AUTH_COOKIE_NAME) != null)
						{
							CookieUtil.deleteCookie(request, response, "/", AUTH_COOKIE_NAME);
						}
					}
					else 
					{
						ajaxResponse.setResponse(400, "Bad parameter");
					}
				}
				else 
				{
					ajaxResponse.setResponse(404, "Bad parameter");
				}
			}
			else 
			{
				//현재 날짜보다 WDATE가 큰 결제내역이 존재함.
				//결혼식을 위해 결제를 했는데 회원을 탈퇴하려고 해? ㄴㄴ, 돈 냈으니까 취소하든 결혼하든 하고 탈퇴해라잉
				ajaxResponse.setResponse(511, "Rez Exist not finished");
			}
		}
		
		return ajaxResponse;
	}
	
	//아이디 찾기
	@RequestMapping(value = "/user/FindingId")
	public String FindingId(HttpServletRequest request, HttpServletResponse response)
	{
		return "/user/FindingId";
	}
	
	//비밀번호 찾기
	@RequestMapping(value="/user/FindingPwd")
	public String FindingPwd(HttpServletRequest request, HttpServletResponse response)
	{
		return "/user/FindingPwd";
	}
	
	
	//이메일로 아이디찾기
	@RequestMapping(value="/user/FindingIdProc")
	@ResponseBody
	public Response<Object> FindingIdEmailCheck(ModelMap model, HttpServletRequest request, HttpServletResponse response)
	{
		Response<Object> ajaxResponse = new Response<Object>();
		
		String userEmail = HttpUtil.get(request, "userEmail", "");
		
		List<WDUser> wduser = null;
		
		String userId = HttpUtil.get(request, "userId", "");
		
		wduser = wduserService.findId(userEmail);
		
		int i;
		for(i =0;i<wduser.size();i++) 
		{
			System.out.println("wduser :"+wduser.get(i).getUserId());
		}
		
		System.out.println("==============" + wduser);
		
			if(wduser.size() > 0)
			{
				ajaxResponse.setResponse(0, "Success",wduser);
				System.out.println("당신의 이메일" + wduser);	
			}
		else
			{
			ajaxResponse.setResponse(100, "fail");
			}
		
		return ajaxResponse;
	}
		

}
