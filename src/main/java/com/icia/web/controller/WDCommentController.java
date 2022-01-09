package com.icia.web.controller;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.icia.common.util.StringUtil;
import com.icia.web.model.Response;
import com.icia.web.model.WDComment;
import com.icia.web.service.WDCommentService;
import com.icia.web.util.CookieUtil;
import com.icia.web.util.HttpUtil;

@Controller("WDCommentController")
public class WDCommentController {

	private static Logger logger = LoggerFactory.getLogger(WDCommentController.class);
	
	@Value("#{env['auth.cookie.name']}")
	private String AUTH_COOKIE_NAME;
	
	@Autowired
	private WDCommentService wdCommentService;
	

	//댓글 삭제
	@RequestMapping(value="/board/commentDelete", method=RequestMethod.POST)
	public Response<Object> commentDelete(MultipartHttpServletRequest request, HttpServletResponse response){
		
		Response<Object> ajaxResponse = new Response<Object>();
		String cookieUserId = CookieUtil.getHexValue(request, AUTH_COOKIE_NAME);
		long parentSeq = HttpUtil.get(request, "bSeq", (long)0);
		long commentSeq = HttpUtil.get(request, "cSeq", (long)0);
		
		WDComment wdComment = new WDComment();
		
		wdComment.setParentSeq(parentSeq);
		wdComment.setCommentSeq(commentSeq);
		if(!StringUtil.isEmpty(cookieUserId)) {
			if(wdComment.getParentSeq() != 0 && wdComment.getCommentSeq() != 0) {
				if(wdCommentService.commentDelete(wdComment) > 0) {
					ajaxResponse.setResponse(0, "Success");
				}
				else {
					ajaxResponse.setResponse(-1, "Internal Server Error");
				}
			}
			else {
				ajaxResponse.setResponse(404, "Bad Request: No parameter");
			}
		}
		else {
			ajaxResponse.setResponse(400, "Bad Request");
		}
		
	
		return ajaxResponse;
	}
}
